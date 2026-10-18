package io.github.jqssun.displaymirror;
import android.app.PendingIntent;import android.content.Intent;import android.os.Build;
import android.service.quicksettings.TileService;
public class AirLinkQuickTile extends TileService {
 @Override public void onClick(){super.onClick();Intent i=new Intent(this,MainActivity.class);
 i.setAction(MainActivity.ACTION_OPEN_SCREEN);i.putExtra(MainActivity.EXTRA_SCREEN,MainActivity.SCREEN_AIRPLAY);
 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 if(Build.VERSION.SDK_INT>=34){startActivityAndCollapse(PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));}
 else startActivityAndCollapse(i);
 }
}