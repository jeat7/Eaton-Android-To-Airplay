package io.github.jqssun.displaymirror.airplay;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.github.jqssun.displaymirror.CastPlaceholderActivity;
import io.github.jqssun.displaymirror.MainActivity;
import io.github.jqssun.displaymirror.Pref;
import io.github.jqssun.displaymirror.R;
import io.github.jqssun.displaymirror.State;
import io.github.jqssun.displaymirror.job.AcquireShizuku;
import io.github.jqssun.displaymirror.shizuku.ShizukuUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import rikka.shizuku.Shizuku;

/**
 * Built-in TV app picker: launch an activity on the AirPlay own-content virtual display.
 * Unlike MediaProjection app sharing, this creates a separate Android activity instance/task.
 * Not all apps allow secondary-display launches or background playback.
 */
public final class AirPlayTvApps {
  private static final Handler MAIN = new Handler(Looper.getMainLooper());
  private AirPlayTvApps() {}

  private static class Entry {
    final String label;
    final String packageName;
    Entry(String label, String packageName) {
      this.label = label;
      this.packageName = packageName;
    }
    @Override public String toString() { return label; }
  }

  public static void showSetup(Activity activity) {
    boolean running = ShizukuUtils.hasShizukuStarted();
    new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.airlink_tv_apps_permission_title)
        .setMessage(running
            ? R.string.airlink_tv_apps_grant_permission
            : R.string.airlink_tv_apps_install_shizuku)
        .setNegativeButton(android.R.string.cancel, null)
        .setNeutralButton(R.string.airlink_tv_apps_setup_guide,
            (dialog, which) -> activity.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://shizuku.rikka.app/guide/setup/"))))
        .setPositiveButton(running
            ? R.string.airlink_tv_apps_grant_access
            : R.string.airlink_tv_apps_open_shizuku,
            (dialog, which) -> {
              if (running) {
                try {
                  Shizuku.requestPermission(AcquireShizuku.SHIZUKU_PERMISSION_REQUEST_CODE);
                } catch (RuntimeException e) {
                  State.log("TV Apps Shizuku permission request: " + e.getMessage());
                  Toast.makeText(activity, R.string.airlink_tv_apps_permission_failed,
                      Toast.LENGTH_LONG).show();
                }
              } else {
                Intent launch = activity.getPackageManager()
                    .getLaunchIntentForPackage("moe.shizuku.privileged.api");
                if (launch != null) {
                  activity.startActivity(launch);
                } else {
                  activity.startActivity(new Intent(Intent.ACTION_VIEW,
                      Uri.parse("https://shizuku.rikka.app/")));
                }
              }
            })
        .show();
  }

  public static void showPicker(Activity activity) {
    if (!ShizukuUtils.hasPermission()) {
      showSetup(activity);
      return;
    }
    if (!Pref.getAirPlayTvAppsMode()) {
      Toast.makeText(activity, R.string.airlink_tv_apps_enable_first, Toast.LENGTH_LONG).show();
      return;
    }
    if (!activity.getPackageManager().hasSystemFeature(
        PackageManager.FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS)) {
      Toast.makeText(activity, R.string.airlink_tv_apps_not_supported, Toast.LENGTH_LONG).show();
      return;
    }

    PackageManager pm = activity.getPackageManager();
    Intent launcherQuery = new Intent(Intent.ACTION_MAIN);
    launcherQuery.addCategory(Intent.CATEGORY_LAUNCHER);
    Map<String, Entry> appMap = new LinkedHashMap<>();
    for (ResolveInfo info : pm.queryIntentActivities(launcherQuery, 0)) {
      if (info.activityInfo == null || !info.activityInfo.exported) continue;
      String pkg = info.activityInfo.packageName;
      if (pkg.equals(activity.getPackageName())) continue;
      String label = String.valueOf(info.loadLabel(pm));
      appMap.putIfAbsent(pkg, new Entry(label, pkg));
    }
    if (appMap.isEmpty()) {
      Toast.makeText(activity, R.string.airlink_tv_apps_no_apps, Toast.LENGTH_LONG).show();
      return;
    }
    List<Entry> apps = new ArrayList<>(appMap.values());
    Collections.sort(apps, (a, b) -> {
      int byLabel = a.label.compareToIgnoreCase(b.label);
      return byLabel == 0 ? a.packageName.compareTo(b.packageName) : byLabel;
    });

    LinearLayout body = new LinearLayout(activity);
    body.setOrientation(LinearLayout.VERTICAL);
    int padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
    body.setPadding(padding, 0, padding, 0);
    EditText search = new EditText(activity);
    search.setSingleLine(true);
    search.setHint(R.string.airlink_tv_apps_search_hint);
    body.addView(search, new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    ListView list = new ListView(activity);
    ArrayAdapter<Entry> adapter = new ArrayAdapter<>(
        activity, android.R.layout.simple_list_item_1, new ArrayList<>(apps));
    list.setAdapter(adapter);
    body.addView(list, new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, (int) (390 * activity.getResources().getDisplayMetrics().density)));

    androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
        .setTitle(R.string.airlink_tv_apps_choose)
        .setView(body)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
    list.setOnItemClickListener((parent, view, position, id) -> {
      Entry chosen = adapter.getItem(position);
      if (chosen == null) return;
      dialog.dismiss();
      Pref.getPreferences().edit()
          .putString(Pref.KEY_AIRPLAY_TV_APP_PACKAGE, chosen.packageName).apply();
      State.log("TV Apps: selected package " + chosen.packageName);
      int displayId = AirPlayState.getVirtualDisplayId();
      if (AirPlayService.getInstance().isTvAppsSessionMode() && displayId > 0) {
        launch(activity, displayId, chosen.packageName);
      } else {
        Toast.makeText(activity, R.string.airlink_tv_apps_connect_to_launch,
            Toast.LENGTH_LONG).show();
      }
    });
    search.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.getFilter().filter(s);
      }
      @Override public void afterTextChanged(Editable s) {}
    });
    dialog.show();
  }

  /** Invoked whenever the encoder registers an AirPlay display; wait for WindowManager setup. */
  public static void onDisplayReady(int displayId) {
    if (!AirPlayService.getInstance().isTvAppsSessionMode()) return;
    String pkg = Pref.getAirPlayTvAppPackage();
    if (pkg.isEmpty()) return;
    MAIN.postDelayed(() -> {
      if (!AirPlayService.getInstance().isTvAppsSessionMode()
          || AirPlayState.getVirtualDisplayId() != displayId) return;
      MainActivity activity = State.getCurrentActivity();
      if (activity != null) launch(activity, displayId, pkg);
    }, 900);
  }

  private static void launch(Activity activity, int displayId, String packageName) {
    if (!AirPlayService.getInstance().isTvAppsSessionMode()
        || AirPlayState.getVirtualDisplayId() != displayId
        || !ShizukuUtils.hasPermission()) return;

    PackageManager pm = activity.getPackageManager();
    Intent launch = pm.getLaunchIntentForPackage(packageName);
    if (launch == null) {
      State.log("TV Apps: app is no longer launchable: " + packageName);
      Toast.makeText(activity, R.string.airlink_tv_apps_app_unavailable, Toast.LENGTH_LONG).show();
      return;
    }
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityManager am =
            (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null && !am.isActivityStartAllowedOnDisplay(activity, displayId, launch)) {
          Toast.makeText(activity, R.string.airlink_tv_apps_display_rejected,
              Toast.LENGTH_LONG).show();
          State.log("TV Apps: Android denied app launch on display " + displayId);
          return;
        }
      }
      ActivityOptions options = ActivityOptions.makeBasic();
      options.setLaunchDisplayId(displayId);
      activity.startActivity(launch, options.toBundle());
      CastPlaceholderActivity.dismiss();
      AirPlayService.getInstance().onTvAppLaunched(packageName);
      State.log("TV Apps: launched " + packageName + " on display " + displayId);
    } catch (SecurityException | IllegalArgumentException e) {
      State.log("TV Apps launch denied: " + e.getMessage());
      Toast.makeText(activity, R.string.airlink_tv_apps_display_rejected, Toast.LENGTH_LONG).show();
    } catch (RuntimeException e) {
      State.log("TV Apps launch failed: " + e.getMessage());
      Toast.makeText(activity, R.string.airlink_tv_apps_launch_failed, Toast.LENGTH_LONG).show();
    }
  }
}
