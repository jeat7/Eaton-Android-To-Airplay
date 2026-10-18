package io.github.jqssun.displaymirror;
import android.os.Bundle;import android.content.*;import android.net.Uri;
import android.view.*;import android.widget.*;
import androidx.annotation.*;import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;

/** Brand-specific remote, never promises universal support without an actual protocol. */
public class AirLinkRemoteFragment extends Fragment {
 private AirLinkTvRemoteClient remote;private TextView status;
 private Spinner brand;private EditText ip;private LinearLayout keys;
 @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup p,@Nullable Bundle s){
  return AirLinkUi.page(requireContext(),"TV remote","Select your TV type. Roku supports network keys; LG and Samsung pairing is experimental. Other TVs require compatible software or hardware.");
 }
 @Override public void onViewCreated(@NonNull View v,@Nullable Bundle s){
  LinearLayout root=AirLinkUi.body(v);
  remote=new AirLinkTvRemoteClient(requireContext(),(msg,ready)->{
   if(status!=null&&isAdded())status.setText(msg);
   if(keys!=null)keys.setVisibility(ready?View.VISIBLE:View.GONE);
  });
  LinearLayout setup=AirLinkUi.panel(root);
  setup.addView(AirLinkUi.text(requireContext(),"TV platform",16,true,false));
  brand=new Spinner(requireContext());
  String[] vendors={"Roku / Roku TV","LG webOS (experimental)","Samsung Tizen (experimental)",
    "Google / Android TV (use official remote)","Amazon Fire TV (use official remote)",
    "Sky Glass (TV remote needed)","Other TV (compatibility guide)","Custom IR codes (NEC-compatible)"};
  ArrayAdapter<String> adapter=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_dropdown_item,vendors);
  brand.setAdapter(adapter);setup.addView(brand);
  setup.addView(AirLinkUi.text(requireContext(),"TV local IPv4 address",15,true,false));
  ip=new EditText(requireContext());ip.setSingleLine(true);ip.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
  ip.setHint("192.168.1.50");setup.addView(ip);
  SharedPreferences pref=requireContext().getSharedPreferences("airlink_tv_remote",0);
  ip.setText(pref.getString("ip",""));brand.setSelection(pref.getInt("vendor",0));
  AirLinkUi.button(setup,"Configure infrared codes",b->configureInfrared());
  AirLinkUi.button(setup,"Connect / Pair",b->{
   int selected=brand.getSelectedItemPosition();
   if(selected==7){
    boolean ok=AirLinkIr.available(requireContext());
    status.setText(ok?"IR is available. Configure your TV's NEC codes.":"No IR transmitter detected on this phone.");
    keys.setVisibility(ok?View.VISIBLE:View.GONE);
    return;
   }
   if(selected>2){status.setText(explain(selected));keys.setVisibility(View.GONE);return;}
   String host=ip.getText().toString().trim();
   pref.edit().putString("ip",host).putInt("vendor",selected).apply();
   remote.connect(selected,host);
  });
  status=AirLinkUi.text(requireContext(),"Not connected",14,false,true);setup.addView(status);
  keys=new LinearLayout(requireContext());keys.setOrientation(1);keys.setVisibility(View.GONE);root.addView(keys);
  AirLinkUi.title(keys,"TV controls");
  addRow(new String[]{"Power","Home","Back"},new String[]{"power","home","back"});
  addRow(new String[]{"","Up",""},new String[]{"","up",""});
  addRow(new String[]{"Left","OK","Right"},new String[]{"left","ok","right"});
  addRow(new String[]{"","Down",""},new String[]{"","down",""});
  addRow(new String[]{"Vol −","Mute","Vol +"},new String[]{"voldown","mute","volup"});
  addRow(new String[]{"Rewind","Play / pause","Forward"},new String[]{"rew","play","ff"});
  AirLinkUi.info(root,"Compatibility","Both devices must be on the same LAN. Roku may require Settings → System → Advanced system settings → Control by mobile apps → Enabled. LG/Samsung must approve pairing on the TV. Not every key or TV model is supported.");
  AirLinkUi.button(root,"Open official remote options",b->openHelp());
 }
 private void addRow(String[] labels,String[] actions){
  LinearLayout row=new LinearLayout(requireContext());row.setOrientation(LinearLayout.HORIZONTAL);
  keys.addView(row,new LinearLayout.LayoutParams(-1,-2));
  for(int n=0;n<3;n++){
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,AirLinkUi.dp(requireContext(),58),1);
   if(actions[n].isEmpty()){row.addView(new View(requireContext()),lp);continue;}
   final String action=actions[n];
   com.google.android.material.button.MaterialButton b=new com.google.android.material.button.MaterialButton(requireContext());
   b.setText(labels[n]);b.setAllCaps(false);b.setTextSize(12);b.setOnClickListener(v->{
    if(brand.getSelectedItemPosition()==7){
     String error=AirLinkIr.press(requireContext(),action);
     if(error!=null)status.setText(error);
    } else remote.press(action);
   });
   row.addView(b,lp);
  }
 }
 private void configureInfrared(){
  EditText editor=new EditText(requireContext());
  editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|
      android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
  editor.setMinLines(6);editor.setText(AirLinkIr.codes(requireContext()));
  editor.setHint("frequency=38000\\npower=1234ABCD\\nvolup=...");
  new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
   .setTitle("Custom IR codes (NEC)")
   .setMessage("Only for phones with IR hardware and NEC-compatible TVs. Provide known 32-bit hex codes for keys such as power, volup, voldown, home, back, up, down, left, right, ok, mute. No default codes are included.")
   .setView(editor)
   .setPositiveButton("Save",(d,w)->AirLinkIr.save(requireContext(),editor.getText().toString()))
   .setNegativeButton("Cancel",null).show();
 }
 private String explain(int selected){
  switch(selected){
   case 3:return "Google TV uses its own authenticated remote pairing. Open the official Google TV app or your TV's built-in remote settings.";
   case 4:return "Fire TV uses Amazon's remote application and device pairing. Open the official Fire TV app.";
   case 5:return "Sky Glass does not currently expose a verified general-purpose network remote API. Use the Sky remote; AirPlay controls the mirror stream, not the TV menus.";
   default:return "This TV type has no confirmed universal IP control protocol. Check your manufacturer's official remote app, or use an IR blaster if supported.";
  }
 }
 private void openHelp(){
  String uri=brand.getSelectedItemPosition()==3?"https://play.google.com/store/search?q=Google%20TV&c=apps":
   brand.getSelectedItemPosition()==4?"https://play.google.com/store/search?q=Amazon%20Fire%20TV&c=apps":
   brand.getSelectedItemPosition()==5?"https://www.sky.com/help/": "https://play.google.com/store/search?q=smart%20tv%20remote&c=apps";
  startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(uri)));
 }
 @Override public void onDestroyView(){if(remote!=null)remote.destroy();super.onDestroyView();}
}