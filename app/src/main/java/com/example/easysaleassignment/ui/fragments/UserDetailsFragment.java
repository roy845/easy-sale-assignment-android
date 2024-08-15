package com.example.easysaleassignment.ui.fragments;

import android.content.Intent;


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


public class UserDetailsFragment extends Fragment {


    private ImageView userAvatarImageView;
    private TextView userNameTextView;
    private Button updateButton, cancelButton;
    private EditText userEmailEditText, firstNameEditText, lastNameEditText;
    UsersViewModel usersViewModel;
    private Uri selectedImageUri;

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

        Observer<UserEntity> userListUpdateObserver = selectedUser -> {
            if (selectedUser != null) {
                loadUserDetails(selectedUser);
            }
        };

        usersViewModel.getItemSelected().observe(getViewLifecycleOwner(), userListUpdateObserver);

        initCancelButtonClickListener();
        initUpdateButtonClickListener();
        initAvatarClickListener();

        super.onViewCreated(view, savedInstanceState);
    }

    private void loadUserDetails(UserEntity selectedUser){
        if(selectedUser.getFirst_name()!=null && selectedUser.getLast_name() != null){
            userNameTextView.setText(selectedUser.getFirst_name() + " " + selectedUser.getLast_name());
        }else{
            userNameTextView.setVisibility(View.GONE);
        }

        if(selectedUser.getEmail()!=null){
            userEmailEditText.setText(selectedUser.getEmail());
        }else{
            userEmailEditText.setVisibility(View.GONE);
        }

        if(selectedUser.getFirst_name()!=null){
            firstNameEditText.setText(selectedUser.getFirst_name());
        }else{
            firstNameEditText.setVisibility(View.GONE);
        }

        if(selectedUser.getLast_name()!=null){
            lastNameEditText.setText(selectedUser.getFirst_name());
        }else{
            lastNameEditText.setVisibility(View.GONE);
        }

        if(selectedUser.getAvatar()!=null){
            Glide.with(UserDetailsFragment.this)
                    .load(selectedUser.getAvatar())
                    .into(userAvatarImageView);
        }else{
            userAvatarImageView.setVisibility(View.GONE);
        }

        if(selectedUser.getFirst_name() == null){
            updateButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
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

    }

    private void setupViewModel() {
        usersViewModel = UsersViewModel.getInstance(getActivity().getApplication(), getContext());
    }


    private void initUpdateButtonClickListener() {
        updateButton.setOnClickListener(v -> {
            UserEntity updatedUser = usersViewModel.getItemSelected().getValue();

            if (updatedUser != null) {
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String email = userEmailEditText.getText().toString();

                // Validate the fields
                if (!ValidationUtils.validateFieldsUpdateUser(firstName,lastName,email)) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate the email
                if (!ValidationUtils.isValidEmail(email)) {
                    Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the user with the new data from the EditTexts
                updatedUser.setFirst_name(firstName);
                updatedUser.setLast_name(lastName);
                updatedUser.setEmail(email); // Correctly set the email
                if (selectedImageUri != null) {
                    updatedUser.setAvatar(selectedImageUri.toString()); // Update the avatar URI
                }

                // Notify ViewModel of the changes
                usersViewModel.updateUser(updatedUser);

                // Pop the fragment from the back stack
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }



    private void initCancelButtonClickListener() {
        cancelButton.setOnClickListener(v -> {
            // Pop the fragment from the back stack
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
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
