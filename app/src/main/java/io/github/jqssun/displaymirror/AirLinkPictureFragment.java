package io.github.jqssun.displaymirror;
import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;
import com.google.android.material.materialswitch.MaterialSwitch;
public class AirLinkPictureFragment extends Fragment {
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Picture & screen fit","Quality presets apply on your next AirPlay connection. Your TV may negotiate a different resolution.");
 }
 @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state){
  LinearLayout root=AirLinkUi.body(view);
  AirLinkUi.title(root,"Quality presets");
  preset(root,"Smooth · 1280×720 · 30 fps",1280,720,30);
  preset(root,"Balanced · 1920×1080 · 30 fps",1920,1080,30);
  preset(root,"Best picture · 1920×1080 · 60 fps",1920,1080,60);
  AirLinkUi.title(root,"Screen fitting");
  switcher(root,"Rotate with content","Match your phone orientation when supported.",
     Pref.KEY_ROTATE_WITH_CONTENT,Pref.getRotateWithContent());
  switcher(root,"Crop black borders","Remove unnecessary black edges when supported.",
     Pref.KEY_CROP_BLACK_BORDERS,Pref.getCropBlackBorders());
  switcher(root,"Mirror current phone screen","Avoid creating a separate virtual display.",
     Pref.KEY_MIRROR_ONLY,Pref.getMirrorOnly());
  AirLinkUi.info(root,"Advanced display behaviour","Some fitting modes require Shizuku, and protected apps may not permit mirroring. Resolution and frame rate changes may require reconnecting.");
 }
 private void preset(LinearLayout root,String name,int w,int h,int fps){
  AirLinkUi.button(root,name,v->{
   Pref.getPreferences().edit().putInt(Pref.KEY_AIRPLAY_WIDTH,w)
    .putInt(Pref.KEY_AIRPLAY_HEIGHT,h).putInt(Pref.KEY_AIRPLAY_REFRESH_RATE,fps).apply();
   android.widget.Toast.makeText(requireContext(),"Saved for next connection",0).show();
  });
 }
 private void switcher(LinearLayout root,String name,String desc,String key,boolean value){
  LinearLayout p=AirLinkUi.panel(root);
  p.addView(AirLinkUi.text(requireContext(),name,16,true,false));
  p.addView(AirLinkUi.text(requireContext(),desc,13,false,true));
  MaterialSwitch sw=new MaterialSwitch(requireContext());sw.setChecked(value);
  sw.setOnCheckedChangeListener((button,checked)->Pref.getPreferences().edit().putBoolean(key,checked).apply());
  p.addView(sw);
 }
}