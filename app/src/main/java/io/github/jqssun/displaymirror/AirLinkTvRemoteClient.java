package io.github.jqssun.displaymirror;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.*;
import org.json.JSONObject;

/** Sends commands only to a user-selected, private IPv4 TV on the local network. */
public final class AirLinkTvRemoteClient {
 public interface Status {void onStatus(String message,boolean ready);}
 public static final int ROKU=0,LG=1,SAMSUNG=2;
 private final Context context;private final Status listener;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 private final OkHttpClient http=new OkHttpClient.Builder().connectTimeout(6,TimeUnit.SECONDS)
  .readTimeout(6,TimeUnit.SECONDS).build();
 private String host="";private int vendor=-1;private WebSocket socket,pointer;
 private volatile boolean ready=false;private volatile String status="Disconnected";
 private boolean muteState;
 public AirLinkTvRemoteClient(Context c,Status cb){context=c.getApplicationContext();listener=cb;}
 private void notifyStatus(String value,boolean canSend){
  status=value;ready=canSend;main.post(()->listener.onStatus(value,canSend));
 }
 public static boolean localIpv4(String raw){
  if(raw==null||!raw.matches("[0-9.]{7,15}"))return false;
  String[] t=raw.split("\\.");if(t.length!=4)return false;int[] v=new int[4];
  try{for(int i=0;i<4;i++){v[i]=Integer.parseInt(t[i]);if(v[i]<0||v[i]>255)return false;}}
  catch(Exception e){return false;}
  return v[0]==10 || (v[0]==172&&v[1]>=16&&v[1]<=31)
      || (v[0]==192&&v[1]==168) || (v[0]==169&&v[1]==254);
 }
 public void connect(int kind,String ip){
  close();if(!localIpv4(ip)){notifyStatus("Enter a private LAN IPv4 address (for example 192.168.1.20).",false);return;}
  vendor=kind;host=ip;notifyStatus("Connecting to "+ip+"…",false);
  if(kind==ROKU) {
   io.execute(()->{
    try {
     HttpURLConnection conn=(HttpURLConnection)new URL("http://"+host+":8060/query/device-info").openConnection();
     conn.setConnectTimeout(4500);conn.setReadTimeout(4500);conn.setRequestMethod("GET");
     int code=conn.getResponseCode();conn.disconnect();
     notifyStatus(code==200?"Roku connected — remote ready.":"Roku responded with HTTP "+code,code==200);
    }catch(Exception e){notifyStatus("Roku connection failed. Check 'Control by mobile apps' and Wi-Fi.",false);}
   });
  } else if(kind==LG) connectLg(); else if(kind==SAMSUNG)connectSamsung();
 }
 private void connectSamsung(){
  String encoded=Base64.encodeToString("Eaton AirLink".getBytes(java.nio.charset.StandardCharsets.UTF_8),Base64.NO_WRAP);
  Request req=new Request.Builder().url("ws://"+host+":8001/api/v2/channels/samsung.remote.control?name="+encoded).build();
  socket=http.newWebSocket(req,new WebSocketListener(){
   @Override public void onOpen(WebSocket s,Response response){
    notifyStatus("Samsung contacted. Approve the remote on your TV if prompted.",false);
   }
   @Override public void onMessage(WebSocket s,String body){
    if(body.contains("ms.channel.connect"))notifyStatus("Samsung remote connected.",true);
    if(body.contains("ms.channel.unauthorized"))notifyStatus("Samsung rejected remote pairing.",false);
   }
   @Override public void onFailure(WebSocket s,Throwable t,Response response){
    notifyStatus("Samsung remote unavailable. Try the SmartThings app or TV network-remote settings.",false);
   }
   @Override public void onClosed(WebSocket s,int code,String reason){notifyStatus("Samsung remote disconnected.",false);}
  });
 }
 private void connectLg(){
  Request req=new Request.Builder().url("ws://"+host+":3000").build();
  socket=http.newWebSocket(req,new WebSocketListener(){
   @Override public void onOpen(WebSocket s,Response response){
    try{
     JSONObject manifest=new JSONObject();
     manifest.put("manifestVersion",1);
     manifest.put("appVersion","1.0");
     manifest.put("signed",new JSONObject().put("created","2026-01-01").put("appId","com.eaton.airlink"));
     manifest.put("permissions",new org.json.JSONArray(Arrays.asList(
      "CONTROL_AUDIO","CONTROL_POWER","CONTROL_MOUSE_AND_KEYBOARD","CONTROL_INPUT_JOYSTICK","READ_CURRENT_CHANNEL")));
     JSONObject payload=new JSONObject().put("pairingType","PROMPT").put("manifest",manifest);
     String key=context.getSharedPreferences("airlink_tv_remote",0).getString("lg_"+host,"");
     if(!key.isEmpty())payload.put("client-key",key);
     s.send(new JSONObject().put("type","register").put("id","airlink-register").put("payload",payload).toString());
     notifyStatus("LG pairing requested. Accept the prompt on your TV.",false);
    }catch(Exception e){notifyStatus("LG pairing request failed.",false);}
   }
   @Override public void onMessage(WebSocket s,String body){
    try{
     JSONObject j=new JSONObject(body);String type=j.optString("type");
     if(type.equals("registered")){
      String key=j.optJSONObject("payload")!=null?j.optJSONObject("payload").optString("client-key"):"";
      if(!key.isEmpty())context.getSharedPreferences("airlink_tv_remote",0).edit().putString("lg_"+host,key).apply();
      notifyStatus("LG paired. Audio keys ready; requesting navigation controls…",true);
      JSONObject request=new JSONObject().put("type","request").put("id","airlink-pointer")
       .put("uri","ssap://com.webos.service.networkinput/getPointerInputSocket");
      s.send(request.toString());
     }else if(j.optString("id").equals("airlink-pointer")){
      String path=j.optJSONObject("payload")!=null?j.optJSONObject("payload").optString("socketPath"):"";
      if(path.startsWith("ws://")||path.startsWith("wss://")) {
       // wss devices use the normal trust store; never bypass SSL certificates.
       pointer=http.newWebSocket(new Request.Builder().url(path).build(),new WebSocketListener(){
        @Override public void onOpen(WebSocket p,Response response){notifyStatus("LG remote ready.",true);}
        @Override public void onFailure(WebSocket p,Throwable t,Response response){
         notifyStatus("LG audio controls ready; navigation requires a trusted pointer channel.",true);
        }
       });
      }
     }else if(type.equals("error"))notifyStatus("LG rejected a remote request or pairing.",false);
    }catch(Exception ignored){}
   }
   @Override public void onFailure(WebSocket s,Throwable e,Response response){
    notifyStatus("LG connection failed. Ensure mobile TV control is enabled.",false);
   }
   @Override public void onClosed(WebSocket s,int code,String reason){notifyStatus("LG remote disconnected.",false);}
  });
 }
 public void press(String action){
  if(!ready){notifyStatus("Connect to a supported TV first.",false);return;}
  final String h=host;final int k=vendor;
  if(k==ROKU){
   String key=rokuKey(action);if(key==null){notifyStatus("This key is unavailable on this Roku model.",true);return;}
   io.execute(()->{
    try{
     HttpURLConnection conn=(HttpURLConnection)new URL("http://"+h+":8060/keypress/"+key).openConnection();
     conn.setRequestMethod("POST");conn.setDoOutput(true);
     conn.setConnectTimeout(4500);conn.setReadTimeout(4500);
     conn.getOutputStream().close();
     int result=conn.getResponseCode();conn.disconnect();
     if(result!=200&&result!=204)notifyStatus("TV rejected button ("+result+").",true);
    }catch(Exception ex){notifyStatus("Roku keypress failed. Check TV connection.",false);}
   });
  }else if(k==SAMSUNG && socket!=null){
   try{
    String key=samsungKey(action);if(key==null)return;
    JSONObject params=new JSONObject().put("Cmd","Click").put("DataOfCmd",key)
     .put("Option","false").put("TypeOfRemote","SendRemoteKey");
    socket.send(new JSONObject().put("method","ms.remote.control").put("params",params).toString());
   }catch(Exception ignored){}
  }else if(k==LG && socket!=null) {
   if(action.equals("volup")||action.equals("voldown")||action.equals("mute")||action.equals("power")){
    String uri=action.equals("volup")?"ssap://audio/volumeUp":action.equals("voldown")?"ssap://audio/volumeDown":
     action.equals("power")?"ssap://system/turnOff":"ssap://audio/setMute";
    try{
     JSONObject command=new JSONObject().put("type","request").put("id","airlink-control")
      .put("uri",uri);
     if(action.equals("mute")){muteState=!muteState;command.put("payload",new JSONObject().put("mute",muteState));}
     socket.send(command.toString());
    }catch(Exception ignored){}
   } else if(pointer!=null){
    String key=lgKey(action);
    if(key!=null)pointer.send("type:button\nname:"+key+"\n\n");
   }else notifyStatus("LG navigation requires the TV's pointer channel (unavailable).",true);
  }
 }
 private String rokuKey(String s){
  switch(s){case "up":return"Up";case "down":return"Down";case "left":return"Left";
   case "right":return"Right";case "ok":return"Select";case "home":return"Home";
   case "back":return"Back";case "play":return"Play";case "rew":return"Rev";
   case "ff":return"Fwd";case "volup":return"VolumeUp";case "voldown":return"VolumeDown";
   case "mute":return"VolumeMute";case "power":return"PowerOff";default:return null;}
 }
 private String samsungKey(String s){
  switch(s){case "up":return"KEY_UP";case "down":return"KEY_DOWN";
   case "left":return"KEY_LEFT";case "right":return"KEY_RIGHT";
   case "ok":return"KEY_ENTER";case "home":return"KEY_HOME";case "back":return"KEY_RETURN";
   case "play":return"KEY_PLAY";case "rew":return"KEY_REWIND";case "ff":return"KEY_FF";
   case "volup":return"KEY_VOLUP";case "voldown":return"KEY_VOLDOWN";
   case "mute":return"KEY_MUTE";case "power":return"KEY_POWEROFF";default:return null;}
 }
 private String lgKey(String s){
  switch(s){case "up":return"UP";case "down":return"DOWN";case "left":return"LEFT";
   case "right":return"RIGHT";case "ok":return"ENTER";case "home":return"HOME";
   case "back":return"BACK";case "play":return"PLAY";default:return null;}
 }
 public void close(){ready=false;if(socket!=null){socket.close(1000,"Close");socket=null;}
  if(pointer!=null){pointer.close(1000,"Close");pointer=null;}}
 public void destroy(){close();io.shutdownNow();}
}