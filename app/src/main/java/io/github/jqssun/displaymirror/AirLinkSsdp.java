package io.github.jqssun.displaymirror;
import android.os.Handler;import android.os.Looper;
import java.net.*;import java.nio.charset.StandardCharsets;import java.util.*;
/** Read-only SSDP scan, explicitly initiated by the user. No device is modified. */
public final class AirLinkSsdp {
 public interface Callback {void finished(List<String> found);}
 private AirLinkSsdp(){}
 public static void scan(Callback callback){
  new Thread(()->{
   Set<String> devices=new LinkedHashSet<>();
   DatagramSocket socket=null;
   try{
    socket=new DatagramSocket();socket.setSoTimeout(600);
    String request="M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\n"+
     "MAN: \"ssdp:discover\"\r\nMX: 2\r\nST: ssdp:all\r\n\r\n";
    byte[] data=request.getBytes(StandardCharsets.UTF_8);
    socket.send(new DatagramPacket(data,data.length,InetAddress.getByName("239.255.255.250"),1900));
    long until=System.currentTimeMillis()+2700;
    while(System.currentTimeMillis()<until){
     byte[] buffer=new byte[4096];DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
     try{
      socket.receive(packet);
      String response=new String(packet.getData(),0,packet.getLength(),StandardCharsets.UTF_8);
      String server="",type="";
      for(String line:response.split("\\r?\\n")){
       String lower=line.toLowerCase(Locale.ROOT);
       if(lower.startsWith("server:"))server=line.substring(7).trim();
       if(lower.startsWith("st:"))type=line.substring(3).trim();
      }
      if(response.toLowerCase(Locale.ROOT).contains("mediarenderer")
         || response.toLowerCase(Locale.ROOT).contains("roku:ecp")
         || response.toLowerCase(Locale.ROOT).contains("dial")){
       devices.add(packet.getAddress().getHostAddress()+" · "+(server.isEmpty()?type:server));
      }
     }catch(java.net.SocketTimeoutException expected){}
    }
   }catch(Exception ignored){}finally{if(socket!=null)socket.close();}
   List<String> found=new ArrayList<>(devices);
   new Handler(Looper.getMainLooper()).post(()->callback.finished(found));
  },"AirLinkSsdpDiscovery").start();
 }
}