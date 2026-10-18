package io.github.jqssun.displaymirror;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.snackbar.Snackbar;

public class ConnectFragment extends Fragment {
  @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_connect, parent, false);
  }
  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    view.findViewById(R.id.connect_airplay_card).setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.airplay_fragment));
    view.findViewById(R.id.connect_sunshine_card).setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.sunshine_fragment));
    view.findViewById(R.id.connect_displaylink_card).setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.displaylink_fragment));
    view.findViewById(R.id.connect_remote_card).setOnClickListener(v -> {
      MirrorTouchscreenBridge.TargetInfo target = MirrorTouchscreenBridge.getDefaultTarget();
      if (target != null) {
        startActivity(MirrorTouchscreenBridge.createInternalIntent(requireContext(), target));
      } else {
        Snackbar.make(view, R.string.airlink_remote_waiting, Snackbar.LENGTH_LONG).show();
      }
    });
    view.findViewById(R.id.connect_tv_remote_button).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.remote_fragment));
    view.findViewById(R.id.connect_help_button).setOnClickListener(
        v -> Navigation.findNavController(v).navigate(R.id.help_fragment));
  }
}