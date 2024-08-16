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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.utils.ValidationUtils;
import com.example.easysaleassignment.viewmodels.UsersViewModel;

import java.util.concurrent.atomic.AtomicBoolean;

public class AddUserFragment extends Fragment {
    private ImageView userAvatarImageView;
    private Button addButton, cancelButton;
    private EditText userEmailEditText, firstNameEditText, lastNameEditText;
    UsersViewModel usersViewModel;
    private Uri selectedImageUri;
    private TextView errorMissingEmailTextView, errorInvalidEmailTextView, errorFirstNameTextView, errorLastNameTextView,
            errorImageViewTextView,generalFeedbackTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.add_user_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,  @Nullable Bundle savedInstanceState) {
        initViews(view);
        setupViewModel();
        initCancelButtonClickListener();
        initAddButtonClickListener();
        initAvatarClickListener();
        restoreErrorTexts(savedInstanceState);

        super.onViewCreated(view, savedInstanceState);
    }

    private void restoreErrorTexts(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore visibility states of error TextViews
            if (errorMissingEmailTextView != null) {
                errorMissingEmailTextView.setVisibility(savedInstanceState.getInt("errorMissingEmailVisibility"));
            }
            if (errorInvalidEmailTextView != null) {
                errorInvalidEmailTextView.setVisibility(savedInstanceState.getInt("errorInvalidEmailVisibility"));
            }
            if (errorFirstNameTextView != null) {
                errorFirstNameTextView.setVisibility(savedInstanceState.getInt("errorFirstNameVisibility"));
            }
            if (errorLastNameTextView != null) {
                errorLastNameTextView.setVisibility(savedInstanceState.getInt("errorLastNameVisibility"));
            }
            if (errorImageViewTextView != null) {
                errorImageViewTextView.setVisibility(savedInstanceState.getInt("errorImageViewVisibility"));
            }

            // Restore the state of generalFeedbackTextView
            if (generalFeedbackTextView != null) {
                generalFeedbackTextView.setVisibility(savedInstanceState.getInt("generalFeedbackVisibility"));
                generalFeedbackTextView.setText(savedInstanceState.getString("generalFeedbackText"));
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the visibility states of error TextViews
        outState.putInt("errorMissingEmailVisibility", errorMissingEmailTextView.getVisibility());
        outState.putInt("errorInvalidEmailVisibility", errorInvalidEmailTextView.getVisibility());
        outState.putInt("errorFirstNameVisibility", errorFirstNameTextView.getVisibility());
        outState.putInt("errorLastNameVisibility", errorLastNameTextView.getVisibility());
        outState.putInt("errorImageViewVisibility", errorImageViewTextView.getVisibility());

        // Save the state of generalFeedbackTextView
        outState.putInt("generalFeedbackVisibility", generalFeedbackTextView.getVisibility());
        outState.putString("generalFeedbackText", generalFeedbackTextView.getText().toString());
    }

    private void initViews(View view) {
        userAvatarImageView = view.findViewById(R.id.add_user_avatar);
        userEmailEditText = view.findViewById(R.id.add_edit_text_email);
        firstNameEditText= view.findViewById(R.id.add_edit_text_first_name);
        lastNameEditText= view.findViewById(R.id.add_edit_text_last_name);
        addButton = view.findViewById(R.id.add_button_add);
        cancelButton = view.findViewById(R.id.add_button_cancel);
        errorMissingEmailTextView = view.findViewById(R.id.error_label_email_missing);
        errorInvalidEmailTextView = view.findViewById(R.id.error_label_email);
        errorFirstNameTextView = view.findViewById(R.id.error_label_first_name);
        errorLastNameTextView = view.findViewById(R.id.error_label_last_name);
        errorImageViewTextView =  view.findViewById(R.id.error_label_avatar_missing);
        generalFeedbackTextView = view.findViewById(R.id.general_feedback_text);
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
            // Reset the error flag
            AtomicBoolean hasError = new AtomicBoolean(false);

            // Observe the user by email
            usersViewModel.getUserByEmail(email).observe(getViewLifecycleOwner(), existingUser -> {
                // If the user already exists, show a toast and return
                if (existingUser != null) {
                    generalFeedbackTextView.setVisibility(View.VISIBLE);
                    generalFeedbackTextView.setText("*User with email: " + email + " already exists.");
                    hasError.set(true);
                }else{
                    generalFeedbackTextView.setVisibility(View.GONE);
                }

                // Validate email presence
                if (!ValidationUtils.validateEmail(email)) {
                    errorMissingEmailTextView.setVisibility(View.VISIBLE);
                    hasError.set(true);
                } else {
                    errorMissingEmailTextView.setVisibility(View.GONE);
                }

                // Validate email format
                if (ValidationUtils.validateEmail(email) && !ValidationUtils.isValidEmail(email)) {
                    errorInvalidEmailTextView.setVisibility(View.VISIBLE);
                    hasError.set(true);
                } else {
                    errorInvalidEmailTextView.setVisibility(View.GONE);
                }

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

                // Validate avatar presence
                if (!ValidationUtils.validateImageView(userAvatarImageView) && selectedImageUri == null) {
                    errorImageViewTextView.setVisibility(View.VISIBLE);
                    hasError.set(true);
                } else {
                    errorImageViewTextView.setVisibility(View.GONE);
                }

                // If there are errors, return early
                if (hasError.get()) {
                    Toast.makeText(getContext(), "Please correct the errors and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // No errors, proceed with adding the user
                UserEntity newUser = new UserEntity(firstName, lastName, email, selectedImageUri.toString());

                // Add the new user to the ViewModel
                usersViewModel.addUser(newUser);

                // Optionally: Clear the input fields after adding the user
                firstNameEditText.setText("");
                lastNameEditText.setText("");
                userEmailEditText.setText("");
                userAvatarImageView.setImageResource(R.drawable.ic_person_add_24); // Reset to placeholder image
                selectedImageUri = null; // Reset selected image URI

                // Optionally: Pop the fragment from the back stack
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
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
