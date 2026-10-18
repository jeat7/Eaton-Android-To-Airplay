package io.github.jqssun.displaymirror;
import android.app.PendingIntent;import android.appwidget.*;import android.content.*;
import android.widget.RemoteViews;
public class AirLinkWidget extends AppWidgetProvider{
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){
 for(int id:ids){Intent i=new Intent(c,MainActivity.class);
 i.setAction(MainActivity.ACTION_OPEN_SCREEN);i.putExtra(MainActivity.EXTRA_SCREEN,MainActivity.SCREEN_AIRPLAY);
 PendingIntent p=PendingIntent.getActivity(c,2,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.airlink_widget);
 v.setOnClickPendingIntent(R.id.airlink_widget_launch,p);m.updateAppWidget(id,v);}
 }
}