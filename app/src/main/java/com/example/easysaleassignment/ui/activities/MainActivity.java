package com.example.easysaleassignment.ui.activities;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.interfaces.IUserAdapterListener;
import com.example.easysaleassignment.ui.fragments.UserDetailsFragment;

public class MainActivity extends AppCompatActivity implements IUserAdapterListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Handle window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Set the default title to an empty string to clear it
            actionBar.setTitle("");

            // Create a TextView for the title
            TextView textView = new TextView(this);
            textView.setText(R.string.app_name);
            textView.setTextColor(Color.WHITE); // Set text color
            textView.setTextSize(18); // Set text size
            textView.setGravity(Gravity.END); // Align text to the right

            // Create an ImageView for the icon
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.baseline_people_24); // Replace with your icon resource

            // Set layout parameters with left margin
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            imageParams.setMargins(16, 0, 0, 0); // Set left margin (16 pixels)

            imageView.setLayoutParams(imageParams); // Apply the parameters to the ImageView

            // Create a container to hold both the TextView and ImageView
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            linearLayout.addView(imageView); // Add the icon first
            linearLayout.addView(textView); // Add the title next to the icon

            // Add the container to the ActionBar
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END | Gravity.CENTER_VERTICAL // Align to right and center vertically
            );

            actionBar.setCustomView(linearLayout, params);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }



    @Override
        public void userClicked () {
            UserDetailsFragment fragB;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fragB = new UserDetailsFragment();
                getSupportFragmentManager().beginTransaction().
                        add(R.id.fragment2, fragB).//add on top of the static fragment
                        addToBackStack("BBB").//cause the back button scrolling through the loaded fragments
                        commit();
                getSupportFragmentManager().executePendingTransactions();

            } else //I am in portrait
            {
                fragB = new UserDetailsFragment();
                getSupportFragmentManager().beginTransaction().
                        add(R.id.mainActivity, fragB).//add on top of the static fragment
                        addToBackStack("BBB").//cause the back button scrolling through the loaded fragments
                        commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        }
    }
