package com.example.easysaleassignment.ui.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.adapters.UsersAdapter;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.ui.SwipeToDeleteCallback;
import com.example.easysaleassignment.viewmodels.UsersViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class UsersFragment extends Fragment {
    private final String SKIP_MESSAGE = "skipMessage";
    private final String CHECKED = "checked";
    private final String NOT_CHECKED = "NOT checked";
    private static final String PREFS_NAME = "MyPrefs";
    public static UsersAdapter usersAdapter;
    public static RecyclerView recyclerView;
    UsersViewModel usersViewModel;
    private FloatingActionButton fab;
//    private SearchView searchView;
    private EditText searchEditText;
    private ProgressBar progressBar;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private TextView errorTextView,emptyResultsTextView;
    private MaterialButton retryButton;
    private ImageView errorImageView,emptyResultsImageView;
    private LinearLayoutManager layoutManager;
    CheckBox doNotShowAgain;
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        // Inflate the layout countries for this fragment
        return inflater.inflate(R.layout.users_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        initFabClickListener();
        enableSwipeToDeleteAndUndo();
//        setupSearchView();
        setupEditTextSearch();
        observeUserLiveData();
        observeErrorLiveData();
        changeEditTextSearchColor();
//        changeSearchViewColor();
        initialScreen();
//        setSearchViewQueryHint(getString(R.string.search_hint));
        super.onViewCreated(view, savedInstanceState);
    }

    public void initialScreen() {
        SharedPreferences settings = getSharedPreferences();
        String skipMessage = getSkipMessage(settings);

        if (!shouldSkipInitialScreen(skipMessage)) {
            showInitialScreenDialog(settings);
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(PREFS_NAME, 0);
    }

    private String getSkipMessage(SharedPreferences settings) {
        return settings.getString(SKIP_MESSAGE, NOT_CHECKED);
    }

    private boolean shouldSkipInitialScreen(String skipMessage) {
        return CHECKED.equals(skipMessage);
    }

    private void showInitialScreenDialog(SharedPreferences settings) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        LayoutInflater adbInflater = LayoutInflater.from(getContext());
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);

        CheckBox doNotShowAgain = eulaLayout.findViewById(R.id.skip);
        setupDialog(adb, eulaLayout, doNotShowAgain, settings);

        adb.show();
    }

    private void setupDialog(AlertDialog.Builder adb, View eulaLayout, CheckBox doNotShowAgain, SharedPreferences settings) {

        adb.setView(eulaLayout);

        // Inflate custom title view
        View customTitleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.customTitle);
        titleTextView.setText(R.string.modal_header); // Set your title text here

        // Set custom title view
        adb.setCustomTitle(customTitleView);
        adb.setMessage(R.string.initialScreenTxt);

        adb.setPositiveButton("Ok", (dialog, which) -> {
            saveSkipMessagePreference(doNotShowAgain, settings);
        });
    }

    private void saveSkipMessagePreference(CheckBox doNotShowAgain, SharedPreferences settings) {

        String checkBoxResult = doNotShowAgain.isChecked() ? CHECKED : NOT_CHECKED;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(SKIP_MESSAGE, checkBoxResult);
        editor.commit();
    }


//    private void setSearchViewQueryHint(String hint){
//        searchView.setQueryHint(hint);
//    }


    public void changeEditTextSearchColor() {
        setEditTextGravityAndAlignment();
        setEditTextColors();
        setEditTextCursorDrawable();
    }

    private void setEditTextGravityAndAlignment() {
        searchEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        searchEditText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
    }

    private void setEditTextColors() {
        // Set the text color to white
        searchEditText.setTextColor(Color.WHITE);

        // Set the hint text color to white
        searchEditText.setHintTextColor(Color.WHITE);
    }

    private void setEditTextCursorDrawable() {
        // Optionally, make the cursor white if the Android version supports it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            searchEditText.setTextCursorDrawable(null);
        }
    }

//    public void changeSearchViewColor(){
//        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
//        searchIcon.setImageResource(R.drawable.baseline_search_24);
//        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
//        closeIcon.setImageResource(R.drawable.baseline_close_24);
//        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
//        searchText.setTextColor(Color.WHITE);
//        searchText.setHintTextColor(Color.WHITE);
//
//    }

    public void observeErrorLiveData(){
        usersViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if(error){
                initViewsWhenError();
            }else{
                initViewsWhenSuccess();
            }
        });
    }

    public void initViewsWhenError(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
//        searchView.setVisibility(View.GONE);
        searchEditText.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
        errorImageView.setVisibility(View.VISIBLE);
        initRetryButtonClickListener();
    }

    public void initViewsWhenSuccess(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
//        searchView.setVisibility(View.VISIBLE);
        searchEditText.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        errorImageView.setVisibility(View.GONE);
    }

    public void initRetryButtonClickListener(){
        retryButton.setOnClickListener(v-> usersViewModel.retryLoadUsers());
    }

    /**
     * Observes the LiveData from the UsersViewModel and updates the UI accordingly.
     */
    private void observeUserLiveData() {
        usersViewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void setupEditTextSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recyclerView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                emptyResultsTextView.setVisibility(View.GONE);
                emptyResultsImageView.setVisibility(View.GONE);

                // Remove any previous runnable from the handler queue
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                // Create a new runnable for the search operation
                searchRunnable = () -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    searchUsers(s.toString());
                };

                // Post the runnable with a delay of 500 milliseconds
                handler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

//    private void setupEditTextSearch() {
//        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                progressBar.setVisibility(View.GONE);
//                searchUsers(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                progressBar.setVisibility(View.VISIBLE);
//                emptyResultsTextView.setVisibility(View.GONE);
//                emptyResultsImageView.setVisibility(View.GONE);
//
//                if (searchRunnable != null) {
//                    handler.removeCallbacks(searchRunnable);
//                }
//
//
//                searchRunnable = () -> {
//                    progressBar.setVisibility(View.GONE);
//                    searchUsers(newText);
//                };
//
//                handler.postDelayed(searchRunnable, 500);
//
//                return false;
//            }
//        });
//    }

//    private void setupSearchView() {
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                progressBar.setVisibility(View.GONE);
//                searchUsers(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                progressBar.setVisibility(View.VISIBLE);
//                emptyResultsTextView.setVisibility(View.GONE);
//                emptyResultsImageView.setVisibility(View.GONE);
//
//                if (searchRunnable != null) {
//                    handler.removeCallbacks(searchRunnable);
//                }
//
//
//                searchRunnable = () -> {
//                    progressBar.setVisibility(View.GONE);
//                    searchUsers(newText);
//                };
//
//                handler.postDelayed(searchRunnable, 500);
//
//                return false;
//            }
//        });
//    }

    private void showResults(List<UserEntity> users) {
        recyclerView.setVisibility(View.VISIBLE);
        usersAdapter.setUsers(users);
        emptyResultsTextView.setVisibility(View.GONE);
        emptyResultsImageView.setVisibility(View.GONE);
    }

    private void showNoResults() {
        recyclerView.setVisibility(View.GONE);
        emptyResultsTextView.setVisibility(View.VISIBLE);
        emptyResultsImageView.setVisibility(View.VISIBLE);
    }

    private void searchUsers(String query) {
        usersViewModel.searchUsers(query).observe(getViewLifecycleOwner(), users -> {

            if (users != null && !users.isEmpty()) {
                showResults(users);
            } else {
                showNoResults();
            }
        });
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AtomicInteger itemPosition = new AtomicInteger(viewHolder.getAdapterPosition());
                UserEntity user = usersAdapter.getUser(itemPosition.get());
                if(direction == ItemTouchHelper.LEFT) {
                    Log.i("Swipe direction : ", "Left");

                    Snackbar snackbar = Snackbar.make(viewHolder.itemView,"You removed "+user.getFirst_name() + " " + user.getLast_name(),Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", v -> {

                        usersViewModel.addUser(user);
                        usersViewModel.setItemSelect(user);

                    });

                    new AlertDialog.Builder(getContext())
                            .setMessage("Do you want to delete \"" + user.getFirst_name() + " " + user.getLast_name() +  "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                usersViewModel.removeUser(user);
                                snackbar.show();

                                UserEntity nullUser = new UserEntity(-1, null, null, null, null);
                                UserEntity selectedUser = usersViewModel.indexItemSelected.getValue();
                                if (selectedUser != null && selectedUser.getFirst_name().equals(user.getFirst_name())) {
                                    usersViewModel.setItemSelect(nullUser);
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> usersAdapter.notifyItemChanged(itemPosition.get()))
                            .setOnCancelListener(dialogInterface -> usersAdapter.notifyItemChanged(itemPosition.get()))
                            .create().show();
                }


            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    private void initFabClickListener() {
        fab.setOnClickListener(v -> {
            AddUserFragment fragB;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fragB = new AddUserFragment();
                getParentFragmentManager().beginTransaction().
                        add(R.id.fragment2, fragB).//add on top of the static fragment
                        addToBackStack("BBB").//cause the back button scrolling through the loaded fragments
                        commit();
                getParentFragmentManager().executePendingTransactions();
            } else //I am in portrait
            {
                fragB = new AddUserFragment();
                getParentFragmentManager().beginTransaction().
                        add(R.id.mainActivity, fragB).//add on top of the static fragment
                        addToBackStack("BBB").//cause the back button scrolling through the loaded fragments
                        commit();
                getParentFragmentManager().executePendingTransactions();
            }
        });
    }


    /**
     * Initialize the views in the fragment.
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.editSearch);
//        searchView = view.findViewById(R.id.searchViewId);
        progressBar = view.findViewById(R.id.progressBarId);
        fab = view.findViewById(R.id.fab);
        errorTextView = view.findViewById(R.id.error_msg_text);
        retryButton = view.findViewById(R.id.retry_button);
        errorImageView = view.findViewById(R.id.error_image);
        emptyResultsImageView = view.findViewById(R.id.empty_results_image);
        emptyResultsTextView = view.findViewById(R.id.empty_results_text);
    }

    /**
     * Setup the RecyclerView with layout manager and adapter.
     */
    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));
        usersAdapter = new UsersAdapter(getActivity().getApplication(), getContext());
        recyclerView.setAdapter(usersAdapter);
//        setupPagination();
    }

//    private void setupPagination(){
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//
//
//                visibleItemCount = layoutManager.getChildCount();
//                totalItemCount = layoutManager.getItemCount();
//                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
//
//
//
//                    if(!recyclerView.canScrollVertically(1)){
//                        loading = false;
//                        Toast.makeText(getContext(),"This is the last item !", Toast.LENGTH_LONG).show();
//
//                        usersViewModel.loadNextPage(getContext());
//                        usersAdapter.notifyDataSetChanged();
//                        loading = true;
//                    }
//
//
//
//
//
//            }
//
//        });
//    }



    /**
     * Setup the ViewModel for the fragment.
     */
    private void setupViewModel() {
        usersViewModel = UsersViewModel.getInstance(getActivity().getApplication(), getContext());

    }
}
