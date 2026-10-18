package io.github.jqssun.displaymirror;
import android.Manifest;import android.content.*;import android.content.pm.PackageManager;
import android.net.*;import android.os.*;import android.provider.Settings;
import android.view.*;import android.widget.*;import androidx.annotation.*;
import androidx.core.content.ContextCompat;import androidx.fragment.app.Fragment;
import io.github.jqssun.displaymirror.shizuku.ShizukuUtils;
import io.github.jqssun.displaymirror.airplay.AirPlayService;
public class AirLinkDiagnosticsFragment extends Fragment {
 private LinearLayout root;
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Connection checker","A private, local check of common screen-mirroring problems.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){
  root=AirLinkUi.body(v);runChecks();
  AirLinkUi.button(root,"Check again",b->runChecks());
 }
 private void runChecks(){
  if(root.getChildCount()>2)root.removeViews(2,root.getChildCount()-2);
  ConnectivityManager cm=(ConnectivityManager)requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
  NetworkCapabilities cap=cm.getNetworkCapabilities(cm.getActiveNetwork());
  boolean wifi=cap!=null&&cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
  AirLinkUi.info(root,"Wi-Fi",wifi?"Connected. Ensure the TV uses the same LAN.":"Not active. Join the TV's Wi-Fi network or use wired LAN with the same network.");
  boolean audio=ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
  AirLinkUi.info(root,"Mirrored audio",audio?"Microphone permission available. Some media apps still block capture.":"Not enabled. Android may ask when you start audio capture.");
  AirLinkUi.info(root,"Floating controls",Settings.canDrawOverlays(requireContext())?"Permission enabled.":"Optional: enable 'Display over other apps' to use the floating control panel.");
  AirLinkUi.info(root,"Shizuku",ShizukuUtils.hasPermission()?"Ready for advanced tools.":"Optional for basic AirPlay. Required only for certain advanced display/input options.");
  AirLinkUi.info(root,"AirPlay",AirPlayService.getInstance().isConnected()?"Receiver connected.":"Not connected. Open Connect and scan for receivers.");
 }
}