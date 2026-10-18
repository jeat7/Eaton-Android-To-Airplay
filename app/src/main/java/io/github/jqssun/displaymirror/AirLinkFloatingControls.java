package io.github.jqssun.displaymirror;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import io.github.jqssun.displaymirror.airplay.AirPlayService;

/** User-initiated overlay. Only runs while this app process is alive. */
public final class AirLinkFloatingControls {
 private static View root;
 private static WindowManager window;
 private static WindowManager.LayoutParams params;
 private AirLinkFloatingControls(){}
 public static synchronized void show(Context context){
  if(root!=null)return;
  Context app=context.getApplicationContext();
  if(!Settings.canDrawOverlays(app))throw new SecurityException("Floating control permission required");
  window=(WindowManager)app.getSystemService(Context.WINDOW_SERVICE);
  LinearLayout box=new LinearLayout(app);box.setOrientation(LinearLayout.VERTICAL);
  box.setPadding(AirLinkUi.dp(app,12),AirLinkUi.dp(app,8),AirLinkUi.dp(app,12),AirLinkUi.dp(app,8));
  GradientDrawable bg=new GradientDrawable();bg.setColor(app.getColor(R.color.eaton_panel2));
  bg.setCornerRadius(AirLinkUi.dp(app,18));bg.setStroke(AirLinkUi.dp(app,1),app.getColor(R.color.eaton_teal));box.setBackground(bg);
  TextView label=AirLinkUi.text(app,"Eaton AirLink · controls",13,true,false);box.addView(label);
  add(app,box,"Mute / Unmute",()->Pref.getPreferences().edit()
      .putBoolean(Pref.KEY_AIRPLAY_STREAM_MUTED,!Pref.getAirPlayStreamMuted()).apply());
  add(app,box,"Stop sharing (privacy)",()->{
    AirPlayService.getInstance().disconnect();hide();});
  add(app,box,"Close controls",AirLinkFloatingControls::hide);
  params=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
      PixelFormat.TRANSLUCENT);
  params.gravity=Gravity.TOP|Gravity.END;params.x=AirLinkUi.dp(app,10);params.y=AirLinkUi.dp(app,110);
  root=box;
  try{window.addView(box,params);}
  catch(RuntimeException e){root=null;throw e;}
 }
 private static void add(Context c,LinearLayout parent,String txt,Runnable action){
  TextView t=AirLinkUi.text(c,txt,14,false,false);
  t.setPadding(AirLinkUi.dp(c,6),AirLinkUi.dp(c,12),AirLinkUi.dp(c,6),AirLinkUi.dp(c,12));
  t.setOnClickListener(v->action.run());parent.addView(t);
 }
 public static synchronized void hide(){
  if(root!=null&&window!=null){try{window.removeView(root);}catch(Exception ignored){}}
  root=null;window=null;params=null;
 }
}