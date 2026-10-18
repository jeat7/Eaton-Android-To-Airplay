package io.github.jqssun.displaymirror;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import java.util.*;
/** Optional programmable 32-bit NEC infrared control; requires a real Android IR emitter. */
public final class AirLinkIr {
 private AirLinkIr(){}
 public static boolean available(Context c){
  ConsumerIrManager m=(ConsumerIrManager)c.getSystemService(Context.CONSUMER_IR_SERVICE);
  return m!=null&&m.hasIrEmitter();
 }
 public static String codes(Context c){return c.getSharedPreferences("airlink_ir",0).getString("codes","");}
 public static void save(Context c,String code){c.getSharedPreferences("airlink_ir",0).edit().putString("codes",code).apply();}
 public static String press(Context c,String action){
  if(!available(c))return "No supported infrared transmitter on this phone.";
  Map<String,String> codes=new HashMap<>();int frequency=38000;
  for(String row:codes(c).split("\\r?\\n")){
   String[] p=row.trim().split("=",2);
   if(p.length!=2)continue;
   if(p[0].trim().equalsIgnoreCase("frequency")){
    try{frequency=Integer.parseInt(p[1].trim());}catch(NumberFormatException ignored){}
   } else codes.put(p[0].trim().toLowerCase(Locale.ROOT),p[1].trim().replaceFirst("(?i)^0x",""));
  }
  String hex=codes.get(action);
  if(hex==null||!hex.matches("[0-9a-fA-F]{8}"))return "Configure an 8-digit NEC code for "+action+".";
  if(frequency<30000||frequency>60000)return "Carrier frequency must be 30000–60000 Hz.";
  long bits=Long.parseLong(hex,16);
  int[] pattern=new int[67];pattern[0]=9000;pattern[1]=4500;pattern[66]=560;
  for(int k=0;k<32;k++){pattern[2+k*2]=560;pattern[3+k*2]=((bits>>>k)&1)==1?1690:560;}
  try {
   ((ConsumerIrManager)c.getSystemService(Context.CONSUMER_IR_SERVICE)).transmit(frequency,pattern);
   return null;
  }catch(Exception e){return "IR sending failed: unsupported hardware or code.";}
 }
}