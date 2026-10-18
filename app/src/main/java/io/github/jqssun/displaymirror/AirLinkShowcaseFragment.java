package io.github.jqssun.displaymirror;
import android.content.*;import android.net.Uri;import android.os.*;
import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import java.util.*;

/** Local photos and video, shown on the phone and captured by an active mirror session. */
public class AirLinkShowcaseFragment extends Fragment {
 private final List<Uri> media=new ArrayList<>();
 private int index;private LinearLayout container;
 private ImageView picture;private VideoView video;private TextView caption;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private boolean slideshow;
 private final Runnable autoNext=new Runnable(){@Override public void run(){
  if(slideshow&&isAdded()&&!media.isEmpty()){index=(index+1)%media.size();showCurrent();}}
 };
 private final ActivityResultLauncher<String[]> selectFiles=registerForActivityResult(
  new ActivityResultContracts.OpenMultipleDocuments(),uris->{
   if(uris==null||uris.isEmpty())return;
   media.clear();media.addAll(uris);index=0;slideshow=false;showCurrent();
  });
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"Media showcase","Display your chosen local photos and videos while mirroring your phone. Nothing is uploaded.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle state){
  LinearLayout root=AirLinkUi.body(v);
  AirLinkUi.button(root,"Choose photos or videos",b->selectFiles.launch(new String[]{"image/*","video/*"}));
  LinearLayout panel=AirLinkUi.panel(root);container=panel;
  picture=new ImageView(requireContext());picture.setAdjustViewBounds(true);
  picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
  picture.setMaxHeight(AirLinkUi.dp(requireContext(),440));
  panel.addView(picture,new LinearLayout.LayoutParams(-1,AirLinkUi.dp(requireContext(),340)));
  video=new VideoView(requireContext());video.setVisibility(View.GONE);
  panel.addView(video,new LinearLayout.LayoutParams(-1,AirLinkUi.dp(requireContext(),300)));
  caption=AirLinkUi.text(requireContext(),"Choose media above to get started.",14,false,true);panel.addView(caption);
  AirLinkUi.button(root,"Previous",b->move(-1));
  AirLinkUi.button(root,"Next",b->move(1));
  AirLinkUi.button(root,"Start / stop photo slideshow",b->{
   slideshow=!slideshow;handler.removeCallbacks(autoNext);
   if(slideshow&&media.size()>1)handler.postDelayed(autoNext,5000);
   Toast.makeText(requireContext(),slideshow?"Slideshow started":"Slideshow stopped",0).show();
  });
  AirLinkUi.info(root,"Privacy and playback","Media stays on your device. Turn on AirPlay mirroring first. Video playback depends on codecs and Android capture rules; DRM-protected content may display black.");
 }
 private void move(int delta){
  if(media.isEmpty())return;index=(index+delta+media.size())%media.size();showCurrent();
 }
 private boolean isVideo(Uri uri){
  String type=requireContext().getContentResolver().getType(uri);
  return type!=null&&type.startsWith("video/");
 }
 private void showCurrent(){
  handler.removeCallbacks(autoNext);
  if(media.isEmpty()||video==null||!isAdded())return;
  video.stopPlayback();
  Uri uri=media.get(index);
  caption.setText((index+1)+" / "+media.size()+(isVideo(uri)?" · Video":" · Photo"));
  if(isVideo(uri)){
   picture.setVisibility(View.GONE);video.setVisibility(View.VISIBLE);
   video.setVideoURI(uri);video.setOnPreparedListener(mp->mp.setLooping(false));
   video.start();
  }else{
   video.setVisibility(View.GONE);picture.setVisibility(View.VISIBLE);picture.setImageURI(uri);
   if(slideshow&&media.size()>1)handler.postDelayed(autoNext,5000);
  }
 }
 @Override public void onPause(){handler.removeCallbacks(autoNext);slideshow=false;
  if(video!=null)video.pause();super.onPause();}
 @Override public void onDestroyView(){handler.removeCallbacks(autoNext);
  if(video!=null)video.stopPlayback();video=null;picture=null;caption=null;container=null;
  super.onDestroyView();}
}