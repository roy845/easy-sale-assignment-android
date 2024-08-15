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
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.utils.ValidationUtils;
import com.example.easysaleassignment.viewmodels.UsersViewModel;

public class AddUserFragment extends Fragment {
    private ImageView userAvatarImageView;
    private Button addButton, cancelButton;
    private EditText userEmailEditText, firstNameEditText, lastNameEditText;
    UsersViewModel usersViewModel;
    private Uri selectedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.add_user_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        initViews(view);
        setupViewModel();
        initCancelButtonClickListener();
        initAddButtonClickListener();
        initAvatarClickListener();

        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view) {
        userAvatarImageView = view.findViewById(R.id.add_user_avatar);
        userEmailEditText = view.findViewById(R.id.add_edit_text_email);
        firstNameEditText= view.findViewById(R.id.add_edit_text_first_name);
        lastNameEditText= view.findViewById(R.id.add_edit_text_last_name);
        addButton = view.findViewById(R.id.add_button_add);
        cancelButton = view.findViewById(R.id.add_button_cancel);

    }

    private void setupViewModel() {
        usersViewModel = UsersViewModel.getInstance(getActivity().getApplication(), getContext());
    }

    private void initAddButtonClickListener() {
        addButton.setOnClickListener(v -> {
            // Get the input data
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = userEmailEditText.getText().toString().trim();

            // Validate input data
            if (!ValidationUtils.validateFields(firstName,lastName,email,selectedImageUri)) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new User object with the input data
            UserEntity newUser = new UserEntity(firstName,lastName,email,selectedImageUri.toString());

            // Add the new user to the ViewModel
            usersViewModel.addUser(newUser);

            // Optionally: Clear the input fields after adding the user
            firstNameEditText.setText("");
            lastNameEditText.setText("");
            userEmailEditText.setText("");
            userAvatarImageView.setImageResource(R.drawable.ic_person_add_24); // Reset to placeholder image

            // Optionally: Pop the fragment from the back stack
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
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
                    Glide.with(AddUserFragment.this)
                            .load(selectedImageUri)
                            .into(userAvatarImageView);
                }
            }
    );

}
