package com.example.easysaleassignment.ui.fragments;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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


public class UsersFragment extends Fragment {

    public static UsersAdapter usersAdapter;
    public static RecyclerView recyclerView;
    UsersViewModel usersViewModel;
    private FloatingActionButton fab;
    private SearchView searchView;
    private ProgressBar progressBar;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private TextView errorTextView,emptyResultsTextView;
    private MaterialButton retryButton;
    private ImageView errorImageView,emptyResultsImageView;


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
        setupSearchView();
        observeUserLiveData();
        observeErrorLiveData();
        changeSearchViewColor();



        super.onViewCreated(view, savedInstanceState);
    }

    public void changeSearchViewColor(){
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setImageResource(R.drawable.baseline_search_24);
        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setImageResource(R.drawable.baseline_close_24);
        EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchText.setTextColor(Color.WHITE);
        searchText.setHintTextColor(Color.LTGRAY); // Or any other color you prefer

    }

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
        searchView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
        errorImageView.setVisibility(View.VISIBLE);
        initRetryButtonClickListener();
    }

    public void initViewsWhenSuccess(){
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.VISIBLE);
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

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                progressBar.setVisibility(View.GONE);
                searchUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                progressBar.setVisibility(View.VISIBLE);
                emptyResultsTextView.setVisibility(View.GONE);
                emptyResultsImageView.setVisibility(View.GONE);

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }


                searchRunnable = () -> {
                    progressBar.setVisibility(View.GONE);
                    searchUsers(newText);
                };

                handler.postDelayed(searchRunnable, 500);

                return false;
            }
        });
    }

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
                int itemPosition = viewHolder.getAdapterPosition();
                UserEntity user = usersAdapter.getUser(itemPosition);
                if(direction == ItemTouchHelper.LEFT) {
                    Log.i("Swipe direction : ", "Left");

                    Snackbar snackbar = Snackbar.make(viewHolder.itemView,"You removed "+user.getFirst_name() + " " + user.getLast_name(),Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", v -> usersViewModel.addUser(user));

                    new AlertDialog.Builder(getContext())
                            .setMessage("Do you want to delete \"" + user.getFirst_name() + " " + user.getLast_name() +  "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                usersViewModel.removeUser(user);
                                snackbar.show();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> usersAdapter.notifyItemChanged(itemPosition))
                            .setOnCancelListener(dialogInterface -> usersAdapter.notifyItemChanged(itemPosition))
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
                        add(R.id.mainActivity, fragB).//add on top of the static fragment
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
        searchView = view.findViewById(R.id.searchViewId);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        usersAdapter = new UsersAdapter(getActivity().getApplication(), getContext());
        recyclerView.setAdapter(usersAdapter);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//
//                if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == allUsers.size() - 1) {
//                    // Reached the end of the list
//                    usersViewModel.loadNextPage(getContext()); // Load more data
//                }
//            }
//        });
    }

    /**
     * Setup the ViewModel for the fragment.
     */
    private void setupViewModel() {
        usersViewModel = UsersViewModel.getInstance(getActivity().getApplication(), getContext());

    }
}
