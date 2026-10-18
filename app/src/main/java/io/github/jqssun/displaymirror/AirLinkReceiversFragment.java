package io.github.jqssun.displaymirror;
import android.content.Intent;import android.os.Bundle;import android.provider.Settings;
import android.view.*;import android.widget.*;import androidx.annotation.*;
import androidx.fragment.app.Fragment;
public class AirLinkReceiversFragment extends Fragment {
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Additional receivers","Different TV technologies require different senders. We only enable controls the device supports.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){
  LinearLayout root=AirLinkUi.body(v);
  AirLinkUi.info(root,"Google Cast / Miracast","Try the Android system's Cast screen. It only discovers receivers compatible with your phone and Android version.");
  AirLinkUi.button(root,"Open Android Cast settings",b->{
   try{startActivity(new Intent("android.settings.CAST_SETTINGS"));}
   catch(Exception e){android.widget.Toast.makeText(requireContext(),"Cast settings are not available on this phone.",0).show();}
  });
  AirLinkUi.info(root,"DLNA discovery","Find media renderers on your network. Discovery is available, but the app does not yet send media to DLNA receivers.");
  AirLinkUi.button(root,"Search for network media devices",button->{
   button.setEnabled(false);
   AirLinkSsdp.scan(found->{
    if(!isAdded())return;
    button.setEnabled(true);
    String result=found.isEmpty()?"No compatible SSDP media devices responded.":
     android.text.TextUtils.join("\\n",found);
    AirLinkUi.info(root,"Scan results",result);
   });
  });
  AirLinkUi.info(root,"Roku / LG / Samsung","Network TV remote support is accessible from Tools → TV remote. Pairing and capability vary by model.");
  AirLinkUi.info(root,"Sky Glass / Fire TV","AirPlay can mirror to some Sky TVs, but that doesn't automatically provide remote commands. Use an appropriate brand remote where direct control is unavailable.");
 }
}