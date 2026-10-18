package io.github.jqssun.displaymirror;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HelpFragment extends Fragment {
  @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_help, parent, false);
  }
  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    view.findViewById(R.id.help_start_setup)
        .setOnClickListener(v -> Onboarding.show(requireActivity(), true));
    view.findViewById(R.id.help_view_logs)
        .setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.logs_fragment));
  }
}