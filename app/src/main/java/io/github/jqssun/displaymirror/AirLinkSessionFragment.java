package io.github.jqssun.displaymirror;
import android.content.*;import android.net.Uri;import android.os.*;
import android.provider.Settings;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.github.jqssun.displaymirror.airplay.AirPlayService;

/** Audio/privacy controls. Audio changes outgoing PCM; never TV hardware volume. */
public class AirLinkSessionFragment extends Fragment {
 private TextView status;private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable updater=new Runnable(){public void run(){update();handler.postDelayed(this,1500);}};
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Connected display","Control your AirPlay session. TV speaker volume stays managed by the TV itself.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle state){
  LinearLayout root=AirLinkUi.body(v);
  LinearLayout card=AirLinkUi.panel(root);status=AirLinkUi.text(requireContext(),"Checking connection…",18,true,false);card.addView(status);
  AirLinkUi.button(card,"Open AirPlay connections",b->Navigation.findNavController(v).navigate(R.id.airplay_fragment));
  AirLinkUi.title(root,"Privacy and floating controls");
  AirLinkUi.button(root,"Stop sharing now",b->{
   AirLinkFloatingControls.hide();AirPlayService.getInstance().disconnect();update();
   Toast.makeText(requireContext(),"Screen sharing stopped.",Toast.LENGTH_SHORT).show();
  });
  AirLinkUi.info(root,"Privacy note","Stopping the stream prevents further frames being sent. The TV may briefly retain the last displayed image before showing its own home screen.");
  AirLinkUi.button(root,"Show floating controls",b->{
   if(!Settings.canDrawOverlays(requireContext())){
    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
     Uri.parse("package:"+requireContext().getPackageName())));
   }else{
    try{AirLinkFloatingControls.show(requireContext());}
    catch(Exception e){Toast.makeText(requireContext(),"Unable to display controls.",0).show();}
   }
  });
  AirLinkUi.title(root,"Sound");
  addToggle(root,"Mute mirrored audio","Silences only captured audio sent to the AirPlay receiver.",
   Pref.KEY_AIRPLAY_STREAM_MUTED,Pref.getAirPlayStreamMuted());
  addToggle(root,"Follow phone media volume","Adjusts outgoing audio frames, not the TV speaker setting.",
   Pref.KEY_AIRPLAY_FOLLOW_PHONE_VOLUME,Pref.getAirPlayFollowPhoneVolume());
  AirLinkUi.title(root,"Picture");
  AirLinkUi.button(root,"Quality and screen fit",b->
    Navigation.findNavController(v).navigate(R.id.picture_fragment));
  AirLinkUi.button(root,"Connection health",b->
    Navigation.findNavController(v).navigate(R.id.health_fragment));
 }
 private void addToggle(LinearLayout root,String title,String desc,String key,boolean current){
  LinearLayout p=AirLinkUi.panel(root);
  p.addView(AirLinkUi.text(requireContext(),title,16,true,false));
  p.addView(AirLinkUi.text(requireContext(),desc,13,false,true));
  MaterialSwitch sw=new MaterialSwitch(requireContext());sw.setChecked(current);p.addView(sw);
  sw.setOnCheckedChangeListener((b,checked)->Pref.getPreferences().edit().putBoolean(key,checked).apply());
 }
 private void update(){
  if(status!=null&&isAdded())status.setText(AirPlayService.getInstance().isConnected()
   ?"Connected · "+Pref.getAirPlayWidth()+" x "+Pref.getAirPlayHeight()+" preferred"
   :"No AirPlay TV connected");
 }
 @Override public void onResume(){super.onResume();handler.post(updater);}
 @Override public void onPause(){handler.removeCallbacks(updater);super.onPause();}
}