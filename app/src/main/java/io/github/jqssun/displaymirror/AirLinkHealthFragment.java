package io.github.jqssun.displaymirror;
import android.content.*;import android.net.*;import android.net.wifi.WifiManager;
import android.os.*;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;
import io.github.jqssun.displaymirror.airplay.AirPlayService;
public class AirLinkHealthFragment extends Fragment {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private TextView value;private final Runnable refresh=new Runnable(){
  @Override public void run(){update();handler.postDelayed(this,2500);}
 };
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Connection health","Live connection and network information. No data is uploaded.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){
  LinearLayout root=AirLinkUi.body(v);LinearLayout panel=AirLinkUi.panel(root);
  value=AirLinkUi.text(requireContext(),"Checking…",16,false,false);panel.addView(value);
  AirLinkUi.info(root,"Accuracy","Wi-Fi signal and connection state come from Android. Actual encoder frame drops and AirPlay latency aren't reported by the current streaming engine.");
 }
 @Override public void onResume(){super.onResume();handler.post(refresh);}
 @Override public void onPause(){handler.removeCallbacks(refresh);super.onPause();}
 private void update(){
  if(value==null||!isAdded())return;
  ConnectivityManager cm=(ConnectivityManager)requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
  Network net=cm.getActiveNetwork();NetworkCapabilities caps=cm.getNetworkCapabilities(net);
  boolean wifi=caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
  WifiManager wm=(WifiManager)requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  String signal="Unavailable";
  if(wifi && wm!=null){
   android.net.wifi.WifiInfo info=wm.getConnectionInfo();
   if(info!=null)signal=WifiManager.calculateSignalLevel(info.getRssi(),5)+"/5";
  }
  AirPlayService service=AirPlayService.getInstance();
  String text="AirPlay: "+(service.isConnected()?"Connected":"Not connected")
   +"\nWi-Fi network: "+(wifi?"Connected":"Not in use")
   +"\nWi-Fi strength: "+signal
   +"\nPreferred output: "+Pref.getAirPlayWidth()+"×"+Pref.getAirPlayHeight()+" at "+Pref.getAirPlayRefreshRate()+" fps";
  value.setText(text);
 }
}