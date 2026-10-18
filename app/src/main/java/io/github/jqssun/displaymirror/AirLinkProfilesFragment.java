package io.github.jqssun.displaymirror;
import android.content.SharedPreferences;import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;import androidx.navigation.Navigation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
public class AirLinkProfilesFragment extends Fragment {
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Favourite TVs","Save your best settings for every TV. Tap Connect to launch the usual AirPlay pairing and screen-capture flow.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){show(v);}
 private void show(View v){
  LinearLayout root=AirLinkUi.body(v);if(root.getChildCount()>2)root.removeViews(2,root.getChildCount()-2);
  SharedPreferences last=requireContext().getSharedPreferences(Pref.PREF_NAME,0);
  String ip=last.getString("airlink_last_device_ip","");
  String name=last.getString("airlink_last_device_name","");
  int port=last.getInt("airlink_last_device_port",7000);
  if(!ip.isEmpty())AirLinkUi.button(root,"Save last TV: "+name,b->{
   AirLinkProfiles.save(requireContext(),new AirLinkProfiles.Profile(name,ip,port,
    Pref.getAirPlayWidth(),Pref.getAirPlayHeight(),Pref.getAirPlayRefreshRate()));show(v);
  });
  if(AirLinkProfiles.all(requireContext()).isEmpty())
   AirLinkUi.info(root,"No favourites yet","Connect to a TV first, then come back to save it.");
  for(AirLinkProfiles.Profile x:AirLinkProfiles.all(requireContext())){
   LinearLayout p=AirLinkUi.panel(root);
   p.addView(AirLinkUi.text(requireContext(),x.name,18,true,false));
   p.addView(AirLinkUi.text(requireContext(),x.ip+" · "+x.width+" x "+x.height+" · "+x.fps+" fps",13,false,true));
   AirLinkUi.button(p,"Connect",b->{
    Pref.getPreferences().edit().putInt(Pref.KEY_AIRPLAY_WIDTH,x.width)
     .putInt(Pref.KEY_AIRPLAY_HEIGHT,x.height).putInt(Pref.KEY_AIRPLAY_REFRESH_RATE,x.fps).apply();
    Bundle arg=new Bundle();arg.putString("airlink_connect_host",x.ip);
    arg.putString("airlink_connect_name",x.name);arg.putInt("airlink_connect_port",x.port);
    Navigation.findNavController(v).navigate(R.id.airplay_fragment,arg);
   });
   AirLinkUi.button(p,"Remove",b->new MaterialAlertDialogBuilder(requireContext())
    .setTitle("Remove "+x.name+"?").setPositiveButton("Remove",(d,w)->{
     AirLinkProfiles.remove(requireContext(),x);show(v);
    }).setNegativeButton("Cancel",null).show());
  }
 }
}