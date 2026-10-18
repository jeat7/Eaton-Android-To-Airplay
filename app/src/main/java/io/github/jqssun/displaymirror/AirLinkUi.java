package io.github.jqssun.displaymirror;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
public final class AirLinkUi {
 private AirLinkUi(){}
 public static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
 public static TextView text(Context c,String value,int size,boolean bold,boolean muted){
  TextView t=new TextView(c);t.setText(value);t.setTextSize(size);
  t.setTextColor(c.getColor(muted?R.color.eaton_muted:R.color.eaton_text));
  if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(c,4),0,dp(c,5));return t;
 }
 public static ScrollView page(Context c,String title,String sub){
  ScrollView s=new ScrollView(c);s.setBackgroundColor(c.getColor(R.color.eaton_bg));
  LinearLayout l=new LinearLayout(c);l.setOrientation(1);l.setPadding(dp(c,16),dp(c,18),dp(c,16),dp(c,56));
  s.addView(l);l.addView(text(c,title,27,true,false));l.addView(text(c,sub,14,false,true));return s;
 }
 public static LinearLayout body(View s){return (LinearLayout)((ScrollView)s).getChildAt(0);}
 public static LinearLayout panel(LinearLayout root){
  Context c=root.getContext();LinearLayout p=new LinearLayout(c);p.setOrientation(1);
  p.setPadding(dp(c,16),dp(c,12),dp(c,16),dp(c,14));
  GradientDrawable d=new GradientDrawable();d.setColor(c.getColor(R.color.eaton_panel));
  d.setCornerRadius(dp(c,18));d.setStroke(dp(c,1),c.getColor(R.color.eaton_line));p.setBackground(d);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(c,10);root.addView(p,lp);return p;
 }
 public static void title(LinearLayout r,String txt){TextView t=text(r.getContext(),txt,17,true,false);
 t.setTextColor(r.getContext().getColor(R.color.eaton_teal));t.setPadding(0,dp(r.getContext(),20),0,dp(r.getContext(),5));r.addView(t);}
 public static MaterialButton button(LinearLayout root,String label,View.OnClickListener action){
  MaterialButton b=new MaterialButton(root.getContext());b.setText(label);b.setAllCaps(false);
  b.setMinHeight(dp(root.getContext(),48));b.setOnClickListener(action);root.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;
 }
 public static void link(LinearLayout root,String title,String sub,View.OnClickListener action){
  LinearLayout p=panel(root);p.addView(text(root.getContext(),title,17,true,false));
  p.addView(text(root.getContext(),sub,13,false,true));button(p,"Open",action);
 }
 public static void info(LinearLayout root,String title,String sub){
  LinearLayout p=panel(root);p.addView(text(root.getContext(),title,17,true,false));
  p.addView(text(root.getContext(),sub,13,false,true));
 }
}