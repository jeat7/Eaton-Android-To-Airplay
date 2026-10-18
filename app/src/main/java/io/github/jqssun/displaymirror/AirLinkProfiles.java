package io.github.jqssun.displaymirror;
import android.content.*;import org.json.*;import java.util.*;
/** Saved locally on-device. No cloud account or scanning required. */
public final class AirLinkProfiles {
 private static final String DB="airlink_profiles_v1",KEY="devices";
 public static final class Profile {
  public String name,ip; public int port,width,height,fps;
  public Profile(String n,String i,int p,int w,int h,int f){
   name=n;ip=i;port=p;width=w;height=h;fps=f;
  }
 }
 private AirLinkProfiles(){}
 private static SharedPreferences prefs(Context c){return c.getSharedPreferences(DB,0);}
 public static List<Profile> all(Context c){
  List<Profile> list=new ArrayList<>();
  try{JSONArray a=new JSONArray(prefs(c).getString(KEY,"[]"));
   for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);
    list.add(new Profile(o.getString("name"),o.getString("ip"),o.getInt("port"),o.getInt("width"),o.getInt("height"),o.getInt("fps")));
   }
  }catch(Exception ignored){}
  return list;
 }
 public static void save(Context c,Profile p){
  List<Profile> a=all(c);a.removeIf(x->x.ip.equals(p.ip)&&x.port==p.port);
  a.add(0,p);while(a.size()>30)a.remove(a.size()-1);write(c,a);
 }
 public static void remove(Context c,Profile p){
  List<Profile> a=all(c);a.removeIf(x->x.ip.equals(p.ip)&&x.port==p.port);write(c,a);
 }
 private static void write(Context c,List<Profile>a){
  JSONArray j=new JSONArray();
  for(Profile p:a){JSONObject x=new JSONObject();try{
   x.put("name",p.name);x.put("ip",p.ip);x.put("port",p.port);
   x.put("width",p.width);x.put("height",p.height);x.put("fps",p.fps);j.put(x);
  }catch(Exception ignored){}}
  prefs(c).edit().putString(KEY,j.toString()).apply();
 }
}