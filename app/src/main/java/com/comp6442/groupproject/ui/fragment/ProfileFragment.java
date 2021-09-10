package com.comp6442.groupproject.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.comp6442.groupproject.R;
import com.comp6442.groupproject.data.FirebaseAuthLiveData;
import com.comp6442.groupproject.data.UserViewModel;
import com.comp6442.groupproject.data.model.User;
import com.comp6442.groupproject.ui.LogInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
  private FirebaseAuth mAuth;
  private static final String ARG_PARAM1 = "uid";
  private String uid;
  private UserViewModel viewModel;
  private User user;
  private TextView userNameView;

  public ProfileFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @return A new instance of fragment HomeFragment.
   */
  public static ProfileFragment newInstance(String param1) {
    Timber.i("New instance created with param %s", param1);
    ProfileFragment fragment = new ProfileFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * The callback also receives a savedInstanceState Bundle argument containing any state
   * previously saved by onSaveInstanceState(). Note that savedInstanceState has
   * a null value the first time the fragment is created
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mAuth = FirebaseAuthLiveData.getInstance().getAuth();

    if (getArguments() != null) {
      this.uid = getArguments().getString(ARG_PARAM1);
    }
    Timber.i("onCreate called with uid %s", uid);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Timber.d("breadcrumb");
    return inflater.inflate(R.layout.fragment_profile, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Timber.d("breadcrumb");

    userNameView = view.findViewById(R.id.profile_username);

    if (savedInstanceState != null) {
      //Restore the fragment's state here
      Timber.i("Restoring fragment state");
      this.uid = savedInstanceState.getString("uid");
    }

    if (this.uid != null) {
      setProfileUserName();

      // when a user is looking at his/her own profile, hide Follow and Message buttons.
      FirebaseUser firebaseUser = mAuth.getCurrentUser();
      if (firebaseUser != null && firebaseUser.getUid().equals(this.uid)) {
        Timber.i("Fetching logged in user's profile. Hiding Follow and Message buttons.");
        // ideally, delete these parts entirely to expand the view
        view.findViewById(R.id.profile_follow_button).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.profile_message_button).setVisibility(View.INVISIBLE);
      }
    } else {
      Timber.w("uid is null");
    }

    Button signOutButton = view.findViewById(R.id.sign_out_button);
    signOutButton.setOnClickListener(unused -> logOut());
    signOutButton.setEnabled(true);
  }

  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    Timber.d("breadcrumb");
    /* It is strongly recommended to tie Lifecycle-aware components to the STARTED state of a
    fragment, as this state guarantees that the fragment's view is available, if one was created,
    and that it is safe to perform a FragmentTransaction on the child FragmentManager of the fragment.
    If the fragment's view is non-null, the fragment's view Lifecycle is moved to STARTED
    immediately after the fragment's Lifecycle is moved to STARTED.
    When the fragment becomes STARTED, the onStart() callback is invoked.
    * */
  }

  /* When your activity is no longer visible to the user, it has entered the Stopped state,
  *  and the system invokes the onStop() callback. This may occur, for example,
  *  when a newly launched activity covers the entire screen. The system may also call onStop()
  *  when the activity has finished running, and is about to be terminated.
  * */
  @Override
  public void onStop() {
    super.onStop();
    Timber.d("breadcrumb");
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ARG_PARAM1, this.uid);
    Timber.i("Saved instance state");
  }

  /* After all of the exit animations and transitions have completed, and the
  fragment's view has been detached from the window, the fragment's view Lifecycle is
  moved into the DESTROYED state and emits the ON_DESTROY event to its observers. The
  fragment then invokes its onDestroyView() callback. At this point, the fragment's view
  has reached the end of its lifecycle and getViewLifecycleOwnerLiveData() returns a null value.
  At this point, all references to the fragment's view should be removed,
  allowing the fragment's view to be garbage collected.
  */
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Timber.d("breadcrumb");
  }

  /* onDetach() is always called after any Lifecycle state changes. */
  @Override
  public void onDetach() {
    super.onDetach();
    Timber.d("breadcrumb");
  }

  private void setProfileUserName() {
    viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    viewModel.getLiveUser().observe(getViewLifecycleOwner(), user -> {
      setUser(user);
      userNameView.setText(user.getUserName());
    });
  }

  private void setUser(User user) {
    this.user = user;
    Timber.i("Fragment attached to user : %s", user);
  }

  public void logOut() {
    if (mAuth.getCurrentUser() != null) mAuth.signOut();
    Timber.i("Taking user to sign-in screen");
    startActivity(new Intent(getActivity(), LogInActivity.class));
  }
}