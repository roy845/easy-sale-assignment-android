package com.example.easysaleassignment.ui.fragments;

import android.content.Intent;


import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.utils.ValidationUtils;
import com.example.easysaleassignment.viewmodels.UsersViewModel;

import java.util.concurrent.atomic.AtomicBoolean;


public class UserDetailsFragment extends Fragment {


    private ImageView userAvatarImageView;
    private TextView userNameTextView,emailLabelTextView,firstNameLabelTextView,lastNameLabelTextView;
    private Button updateButton, cancelButton;
    private EditText userEmailEditText, firstNameEditText, lastNameEditText;
    UsersViewModel usersViewModel;
    private Uri selectedImageUri;
    private TextView errorFirstNameTextView, errorLastNameTextView,editUserTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.user_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        initViews(view);
        setupViewModel();
        hideViewsAtStartup();
        observeSelectedUser();
        initCancelButtonClickListener();
        initUpdateButtonClickListener();
        initAvatarClickListener();

        super.onViewCreated(view, savedInstanceState);
    }


    private void hideViewsAtStartup(){
        editUserTextView.setVisibility(View.GONE);
        emailLabelTextView.setVisibility(View.GONE);
        firstNameLabelTextView.setVisibility(View.GONE);
        lastNameLabelTextView.setVisibility(View.GONE);
        userAvatarImageView.setVisibility(View.GONE);
        userNameTextView.setVisibility(View.GONE);
        userEmailEditText.setVisibility(View.GONE);
        firstNameEditText.setVisibility(View.GONE);
        lastNameEditText.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    private void observeSelectedUser() {
        Observer<UserEntity> userListUpdateObserver = selectedUser -> {
            if (selectedUser != null) {
                loadUserDetails(selectedUser);
            }
        };

        usersViewModel.getItemSelected().observe(getViewLifecycleOwner(), userListUpdateObserver);
    }

    private void loadUserDetails(UserEntity selectedUser){
        if(selectedUser.getFirst_name()!=null && selectedUser.getLast_name() != null){
            userNameTextView.setVisibility(View.VISIBLE);
            userNameTextView.setText(selectedUser.getFirst_name() + " " + selectedUser.getLast_name());
        }else{
            userNameTextView.setVisibility(View.GONE);
        }

        if(selectedUser.getEmail()!=null){
            emailLabelTextView.setVisibility(View.VISIBLE);
            userEmailEditText.setVisibility(View.VISIBLE);
            userEmailEditText.setText(selectedUser.getEmail());
        }else{
            userEmailEditText.setVisibility(View.GONE);
            emailLabelTextView.setVisibility(View.GONE);
        }

        if(selectedUser.getFirst_name()!=null){
            firstNameLabelTextView.setVisibility(View.VISIBLE);
            firstNameEditText.setVisibility(View.VISIBLE);
            firstNameEditText.setText(selectedUser.getFirst_name());
        }else{
            firstNameEditText.setVisibility(View.GONE);
            firstNameLabelTextView.setVisibility(View.GONE);
        }

        if(selectedUser.getLast_name()!=null){
            lastNameEditText.setVisibility(View.VISIBLE);
            lastNameLabelTextView.setVisibility(View.VISIBLE);
            lastNameEditText.setText(selectedUser.getLast_name());
        }else{
            lastNameLabelTextView.setVisibility(View.GONE);
            lastNameEditText.setVisibility(View.GONE);
        }

        if(selectedUser.getAvatar()!=null){
            userAvatarImageView.setVisibility(View.VISIBLE);
            Glide.with(UserDetailsFragment.this)
                    .load(selectedUser.getAvatar())
                    .into(userAvatarImageView);
        }else{
            userAvatarImageView.setVisibility(View.GONE);
        }

        if(selectedUser.getFirst_name() == null){
            editUserTextView.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }else{
            editUserTextView.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }

    }

    private void initViews(View view) {
        userAvatarImageView = view.findViewById(R.id.user_avatar);
        userNameTextView = view.findViewById(R.id.user_name_header);
        userEmailEditText = view.findViewById(R.id.edit_text_email);
        firstNameEditText = view.findViewById(R.id.edit_text_first_name);
        lastNameEditText = view.findViewById(R.id.edit_text_last_name);
        updateButton = view.findViewById(R.id.button_update);
        cancelButton = view.findViewById(R.id.button_cancel);
        editUserTextView = view.findViewById(R.id.update_user_title);
        emailLabelTextView = view.findViewById(R.id.label_email);
        firstNameLabelTextView = view.findViewById(R.id.label_first_name);
        lastNameLabelTextView = view.findViewById(R.id.label_last_name);
        errorFirstNameTextView = view.findViewById(R.id.error_label_first_name_update_user);
        errorLastNameTextView = view.findViewById(R.id.error_label_last_name_update_user);
    }

    private void setupViewModel() {
        usersViewModel = UsersViewModel.getInstance(getActivity().getApplication(), getContext());
    }


    private void initUpdateButtonClickListener() {
        updateButton.setOnClickListener(v -> {
            UserEntity updatedUser = usersViewModel.getItemSelected().getValue();
            if (updatedUser != null) {
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();
                String email = userEmailEditText.getText().toString().trim();

                AtomicBoolean hasError = new AtomicBoolean(false);

                    // Validate first name
                    if (!ValidationUtils.validateFirstName(firstName)) {
                        errorFirstNameTextView.setVisibility(View.VISIBLE);
                        hasError.set(true);
                    } else {
                        errorFirstNameTextView.setVisibility(View.GONE);
                    }

                    // Validate last name
                    if (!ValidationUtils.validateLastname(lastName)) {
                        errorLastNameTextView.setVisibility(View.VISIBLE);
                        hasError.set(true);
                    } else {
                        errorLastNameTextView.setVisibility(View.GONE);
                    }

                    // If there are errors, return early
                    if (hasError.get()) {
                        Toast.makeText(getContext(), "Please correct the errors and try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // No errors, proceed with updating the user
                    updatedUser.setFirst_name(firstName);
                    updatedUser.setLast_name(lastName);
                    updatedUser.setEmail(email);

                    if (selectedImageUri != null) {
                        updatedUser.setAvatar(selectedImageUri.toString());
                    }

                    usersViewModel.updateUser(updatedUser);

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        UserEntity nullUser = new UserEntity(-1, null, null, null, null);
                        usersViewModel.setItemSelect(nullUser);
                    } else {
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    }

            }
        });
    }




    private void initCancelButtonClickListener() {
        cancelButton.setOnClickListener(v -> {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                UserEntity nullUser = new UserEntity(-1,null,null,null,null);
                usersViewModel.setItemSelect(nullUser);

            }else{
                // Pop the fragment from the back stack
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }

        });
    }

    private void initAvatarClickListener() {
        userAvatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(UserDetailsFragment.this)
                            .load(selectedImageUri)
                            .into(userAvatarImageView);
                }
            }
    );
}
