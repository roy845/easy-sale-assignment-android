package com.example.easysaleassignment.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.easysaleassignment.R;

public class SplashScreen extends AppCompatActivity {
    // Set the duration of the splash screen in milliseconds
    private static final int SPLASH_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        hideActionBar();
        loadGif();
        scheduleTransitionToMainActivity();
    }

    /**
     * Hides the ActionBar for the splash screen.
     */
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * Loads the GIF into the ImageView using Glide.
     */
    private void loadGif() {
        ImageView imageView = findViewById(R.id.splashLoading);
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading1)
                .into(imageView);
    }

    /**
     * Schedules the transition to the MainActivity after the splash screen duration.
     */
    private void scheduleTransitionToMainActivity() {
        new Handler().postDelayed(() -> navigateToMainActivity(), SPLASH_DURATION);
    }

    /**
     * Navigates to the MainActivity and finishes the current activity.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
