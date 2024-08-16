package com.example.easysaleassignment.utils;

import android.net.Uri;
import android.util.Patterns;
import android.widget.ImageView;

public class ValidationUtils {

        public static boolean validateFields(String firstName, String lastName, String email, Uri imageUri) {
            return !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && imageUri != null;
        }

        public static boolean validateEmail(String email){
            return !email.isEmpty();
        }

        public static boolean validateFirstName(String firstname){
            return !firstname.isEmpty();
        }

        public static boolean validateLastname(String lastname){
            return !lastname.isEmpty();
        }

        public static boolean validateImageView(ImageView avatarImageView){
            return avatarImageView == null;
        }

        public static boolean validateFieldsUpdateUser(String firstName, String lastName, String email) {

            return !firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty();
        }

        public static boolean isValidEmail(CharSequence email) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }


