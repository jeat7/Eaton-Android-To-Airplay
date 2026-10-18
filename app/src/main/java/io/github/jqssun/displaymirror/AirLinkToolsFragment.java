package io.github.jqssun.displaymirror;
import android.os.Bundle;import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
public class AirLinkToolsFragment extends Fragment {
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle b){
  return AirLinkUi.page(requireContext(),"Eaton AirLink Tools","All features in one place. Original Mirror options remain available under Connect and Settings.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle b){
  LinearLayout r=AirLinkUi.body(v);
  link(r,"Favourite TVs","Device profiles and quick reconnection",R.id.profiles_fragment);
  link(r,"Connected display","Mute, stop sharing, controls and quality",R.id.session_fragment);
  link(r,"TV remote","Control compatible Roku, LG and Samsung smart TVs",R.id.remote_fragment);
  link(r,"Picture and screen fit","Picture presets, resolution and cropping",R.id.picture_fragment);
  link(r,"Photo and video showcase","Present media while screen mirroring",R.id.showcase_fragment);
  link(r,"Connection health","See network strength and session status",R.id.health_fragment);
  link(r,"Connection checker","Find and fix common setup issues",R.id.diagnostics_fragment);
  link(r,"More receiver types","Android Cast, DLNA and Miracast options",R.id.receivers_fragment);
  AirLinkUi.info(r,"Quick connect","Add the Eaton AirLink Quick Settings tile or home-screen widget. Android screen-capture approval is still required.");
 }
 private void link(LinearLayout l,String t,String s,int id){AirLinkUi.link(l,t,s,v->Navigation.findNavController(v).navigate(id));}
}