package io.github.jqssun.displaymirror;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Non-mandatory, skippable setup that can be reopened from Home, Help and Settings. */
public final class Onboarding {
  private static final String DONE = "airlink_onboarding_done_v2";
  private Onboarding() {}

  public static void show(Activity a, boolean replay) {
    if (a.isFinishing()) return;
    if (!replay && a.getSharedPreferences(Pref.PREF_NAME, Context.MODE_PRIVATE)
        .getBoolean(DONE, false)) return;
    welcome(a);
  }

  private static void done(Activity a) {
    a.getSharedPreferences(Pref.PREF_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(DONE, true).apply();
  }

  private static void welcome(Activity a) {
    new MaterialAlertDialogBuilder(a).setTitle(R.string.airlink_welcome)
        .setMessage(R.string.airlink_welcome_body)
        .setPositiveButton(R.string.airlink_get_started, (d,w) -> choose(a))
        .setNeutralButton(R.string.airlink_skip, (d,w) -> done(a)).show();
  }

  private static void choose(Activity a) {
    String[] options = {a.getString(R.string.airlink_mirror_tv),
        a.getString(R.string.airlink_moonlight), a.getString(R.string.airlink_usb_display),
        a.getString(R.string.airlink_advanced_optional)};
    new MaterialAlertDialogBuilder(a).setTitle(R.string.airlink_choose_route)
        .setItems(options, (d,which) -> {
          a.getSharedPreferences(Pref.PREF_NAME, Context.MODE_PRIVATE).edit()
              .putInt("airlink_route", which).apply();
          permissions(a);
        })
        .setNegativeButton(R.string.airlink_back, (d,w) -> welcome(a))
        .setNeutralButton(R.string.airlink_skip, (d,w) -> done(a)).show();
  }

  private static void permissions(Activity a) {
    boolean overlay = Settings.canDrawOverlays(a);
    boolean audio = ContextCompat.checkSelfPermission(a, Manifest.permission.RECORD_AUDIO)
        == PackageManager.PERMISSION_GRANTED;
    String message = a.getString(R.string.airlink_permissions_intro) + "\n\n"
        + a.getString(R.string.airlink_floating_controls) + ": "
        + a.getString(overlay ? R.string.status_ready : R.string.airlink_optional)
        + "\n" + a.getString(R.string.airlink_audio_access) + ": "
        + a.getString(audio ? R.string.status_ready : R.string.airlink_optional)
        + "\n\n" + a.getString(R.string.airlink_capture_explainer);
    MaterialAlertDialogBuilder dlg = new MaterialAlertDialogBuilder(a)
        .setTitle(R.string.airlink_permissions_title).setMessage(message)
        .setPositiveButton(R.string.airlink_next, (d,w) -> ready(a))
        .setNegativeButton(R.string.airlink_back, (d,w) -> choose(a));
    if (!overlay) dlg.setNeutralButton(R.string.airlink_enable_overlay, (d,w) -> {
      try {
        a.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + a.getPackageName())));
      } catch (Exception ignored) {}
      ready(a);
    });
    dlg.show();
  }

  private static void ready(Activity a) {
    new MaterialAlertDialogBuilder(a).setTitle(R.string.airlink_ready_title)
        .setMessage(R.string.airlink_ready_body)
        .setPositiveButton(R.string.airlink_open_connect, (d,w) -> {
          done(a);
          if (a instanceof MainActivity) ((MainActivity) a).openConnectTab();
        })
        .setNegativeButton(R.string.airlink_finish, (d,w) -> done(a)).show();
  }
}