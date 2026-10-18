package io.github.jqssun.displaymirror;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.MaterialSharedAxis;
import io.github.jqssun.displaymirror.airplay.AirPlayService;
import io.github.jqssun.displaymirror.displaylink.ApkImporter;
import io.github.jqssun.displaymirror.displaylink.DisplaylinkState;
import io.github.jqssun.displaymirror.job.AcquireShizuku;
import io.github.jqssun.displaymirror.shizuku.ShizukuUtils;
import io.github.jqssun.displaymirror.sunshine.SunshineHost;
import io.github.jqssun.displaymirror.sunshine.SunshineState;

public class OverviewFragment extends Fragment {

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
    setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_overview, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    _initShizuku(view);
    view.findViewById(R.id.connect_tv_btn).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.airplay_fragment));
    view.findViewById(R.id.setup_guide_btn).setOnClickListener(v -> Onboarding.show(requireActivity(), true));
    view.findViewById(R.id.last_device_label).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.airplay_fragment));
    _initOverlay(view);

    view.findViewById(R.id.sunshine_card)
        .setOnClickListener(
            v -> Navigation.findNavController(v).navigate(R.id.action_overview_to_sunshine));
    view.findViewById(R.id.airplay_card)
        .setOnClickListener(
            v -> Navigation.findNavController(v).navigate(R.id.action_overview_to_airplay));
    view.findViewById(R.id.displaylink_card)
        .setOnClickListener(
            v -> Navigation.findNavController(v).navigate(R.id.action_overview_to_displaylink));

    view.findViewById(R.id.touchscreen_row)
        .setOnClickListener(
            v -> {
              MirrorTouchscreenBridge.TargetInfo target =
                  MirrorTouchscreenBridge.getDefaultTarget();
              startActivity(MirrorTouchscreenBridge.createInternalIntent(requireContext(), target));
            });

    State.uiState.observe(getViewLifecycleOwner(), state -> _updateStatus(view));
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getView() != null) {
      _updateShizuku(getView());
      _updateOverlay(getView());
      _updateStatus(getView());
    }
  }

  private void _initShizuku(View view) {
    MaterialButton btn = view.findViewById(R.id.shizuku_permission_btn);
    btn.setOnClickListener(v -> State.startNewJob(State.MODE_UTILITY, new AcquireShizuku()));
    _updateShizuku(view);
  }

  private void _updateShizuku(View view) {
    TextView status = view.findViewById(R.id.shizuku_status);
    MaterialButton btn = view.findViewById(R.id.shizuku_permission_btn);
    boolean started = ShizukuUtils.hasShizukuStarted();
    boolean hasPermission = ShizukuUtils.hasPermission();
    if (!started) {
      status.setText(R.string.airlink_shizuku_optional);
      btn.setVisibility(View.GONE);
    } else if (!hasPermission) {
      status.setText(
          getString(R.string.status_started_not_authorized_server, ShizukuUtils.getServerUid()));
      btn.setVisibility(View.VISIBLE);
    } else {
      status.setText(getString(R.string.status_authorized_server, ShizukuUtils.getServerUid()));
      btn.setVisibility(View.GONE);
    }
  }

  private void _initOverlay(View view) {
    MaterialButton btn = view.findViewById(R.id.overlay_permission_btn);
    btn.setOnClickListener(
        v ->
            startActivity(
                new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireContext().getPackageName()))));
    _updateOverlay(view);
  }

  private void _updateOverlay(View view) {
    TextView status = view.findViewById(R.id.overlay_status);
    MaterialButton btn = view.findViewById(R.id.overlay_permission_btn);
    boolean has = Settings.canDrawOverlays(requireContext());
    status.setText(has ? R.string.airlink_overlay_ready : R.string.airlink_overlay_optional);
    btn.setVisibility(has ? View.GONE : View.VISIBLE);
    _updatePermissionsCard(view);
  }

  private void _updatePermissionsCard(View view) {
    TextView title = view.findViewById(R.id.permissions_status_title);
    TextView detail = view.findViewById(R.id.permissions_status_detail);
    ImageView icon = view.findViewById(R.id.permissions_status_icon);
    boolean overlay = Settings.canDrawOverlays(requireContext());
    title.setText(R.string.airlink_core_ready);
    detail.setText(overlay ? R.string.airlink_setup_optional_ready : R.string.airlink_setup_optional_not_ready);
    icon.setImageResource(R.drawable.ic_check_circle);
    ((TextView) view.findViewById(R.id.hero_status)).setText(
        AirPlayService.getInstance().isConnected() ? R.string.airplay_connected : R.string.status_ready);
  }

  private void _updateStatus(View view) {
    int activeColor =
        MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimary);
    int inactiveColor =
        MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurfaceVariant);

    // sunshine
    TextView sunshineStatus = view.findViewById(R.id.sunshine_status);
    ImageView sunshineIcon = view.findViewById(R.id.sunshine_icon);
    boolean sunshineActive = SunshineHost.isRunning();
    boolean sunshineProjecting =
        sunshineActive
            && SunshineState.virtualDisplay != null;
    if (sunshineProjecting) {
      sunshineStatus.setText(R.string.sunshine_status_casting);
    } else if (sunshineActive) {
      sunshineStatus.setText(R.string.sunshine_status_waiting);
    } else {
      sunshineStatus.setText(R.string.sunshine_status_idle);
    }
    sunshineIcon.setImageTintList(
        ColorStateList.valueOf(sunshineProjecting ? activeColor : inactiveColor));

    // airplay
    TextView airplayStatus = view.findViewById(R.id.airplay_status);
    ImageView airplayIcon = view.findViewById(R.id.airplay_icon);
    AirPlayService airplay = AirPlayService.getInstance();
    boolean airplayConnected = airplay != null && airplay.isConnected();
    if (airplayConnected) {
      airplayStatus.setText(R.string.airplay_connected);
    } else {
      airplayStatus.setText(R.string.airplay_no_devices);
    }
    airplayIcon.setImageTintList(
        ColorStateList.valueOf(airplayConnected ? activeColor : inactiveColor));

    // displaylink
    TextView displaylinkStatus = view.findViewById(R.id.displaylink_status);
    ImageView displaylinkIcon = view.findViewById(R.id.displaylink_icon);
    boolean displaylinkImported = ApkImporter.areLibsImported(requireContext());
    boolean displaylinkActive = DisplaylinkState.instance.getVirtualDisplay() != null;
    if (displaylinkActive) {
      displaylinkStatus.setText(R.string.displaylink_libs_status_active);
    } else if (displaylinkImported) {
      displaylinkStatus.setText(R.string.displaylink_libs_status_imported);
    } else {
      displaylinkStatus.setText(R.string.displaylink_libs_status_missing);
    }
    displaylinkIcon.setImageTintList(
        ColorStateList.valueOf(displaylinkActive ? activeColor : inactiveColor));

    String lastName = requireContext().getSharedPreferences(Pref.PREF_NAME,
        android.content.Context.MODE_PRIVATE).getString("airlink_last_device_name", "");
    ((TextView) view.findViewById(R.id.last_device_label)).setText(lastName.isEmpty()
        ? getString(R.string.airlink_no_last_device) : getString(R.string.airlink_last_device,lastName));
    // touchscreen
    View touchscreenRow = view.findViewById(R.id.touchscreen_row);
    TextView touchscreenStatus = view.findViewById(R.id.touchscreen_status);
    boolean touchscreenAvailable = MirrorTouchscreenBridge.getDefaultTarget() != null;
    touchscreenStatus.setText(touchscreenAvailable ? R.string.airlink_remote_ready
        : R.string.airlink_remote_waiting);
    touchscreenRow.setEnabled(touchscreenAvailable);
    touchscreenRow.setAlpha(touchscreenAvailable ? 1f : 0.38f);
  }
}
