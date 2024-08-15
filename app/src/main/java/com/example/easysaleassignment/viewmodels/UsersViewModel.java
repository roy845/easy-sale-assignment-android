package com.example.easysaleassignment.viewmodels;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easysaleassignment.adapters.UsersAdapter;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.data.local.repository.UserRepository;
import com.example.easysaleassignment.models.UserResponse;
import com.example.easysaleassignment.network.ApiClient;
import com.example.easysaleassignment.network.ApiService;
import com.example.easysaleassignment.ui.fragments.UsersFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class UsersViewModel extends AndroidViewModel {
    private static UsersViewModel instance;
    private MutableLiveData<List<UserEntity>> usersLiveData;
    public static ArrayList<UserEntity> allUsers;
    public MutableLiveData<UserEntity> indexItemSelected;
    MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>();
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private int currentPage = 0; // Track the current page

    public void loadNextPage(Context context) {
        int limit = 6;
        int offset = currentPage * limit;

            userRepository.getUsersWithPagination(limit, offset).observeForever(userEntities -> {
                if (!userEntities.isEmpty()) {
                    currentPage++;
                    allUsers.addAll(userEntities);
                    usersLiveData.setValue(allUsers); // Update LiveData with new data
                    showAdapter(context);
                }
            });

    }


    public UsersViewModel(@NonNull Application application, Context context) {
        super(application);
        userRepository = new UserRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        init(context);
    }

    /**
     * Load users from the repository or API. This can be called to retry loading users after a failure.
     */
    public void retryLoadUsers() {
        currentPage = 0; // Reset the current page to start fresh
        allUsers.clear(); // Clear any existing data to avoid duplicates
        fetchUsers(getApplication().getApplicationContext()); // Start fetching users
    }

    public LiveData<List<UserEntity>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<Boolean> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<UserEntity> getItemSelected() {
        return indexItemSelected;
    }

    public void setItemSelect(UserEntity user) {
        indexItemSelected.setValue(user);
    }

    public void setUserLiveData(List<UserEntity> list) {
        usersLiveData.setValue(list);
    }

    public LiveData<List<UserEntity>> getAllUsers(){
        return userRepository.getAllUsers();
    }

    public LiveData<List<UserEntity>> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    public void updateUser(UserEntity updatedUser) {
        userRepository.update(new UserEntity(updatedUser.getId(), updatedUser.getFirst_name(), updatedUser.getLast_name(), updatedUser.getEmail(), updatedUser.getAvatar()));
        Toast.makeText(getApplication().getApplicationContext(), updatedUser.getFirst_name() + " " + updatedUser.getLast_name() + " updated successfully!", Toast.LENGTH_SHORT).show();
    }

    public void removeUser(UserEntity userToRemove) {
            // Remove the user from the Room database
            userRepository.delete(new UserEntity(userToRemove.getId(), userToRemove.getFirst_name(), userToRemove.getLast_name(), userToRemove.getEmail(), userToRemove.getAvatar()));

            // Optionally, show a toast or update the UI to indicate success
            Toast.makeText(getApplication().getApplicationContext(), userToRemove.getFirst_name() + " " + userToRemove.getLast_name() + " removed successfully!", Toast.LENGTH_SHORT).show();

    }


    public void addUser(UserEntity newUser) {
        // Create a new UserEntity without an ID (Room will auto-generate it)
        UserEntity userEntity = new UserEntity(newUser.getFirst_name(), newUser.getLast_name(),newUser.getEmail(),newUser.getAvatar());
        // Insert the new user into the Room database
        userRepository.insert(userEntity);
        // Optionally, show a toast or update the UI to indicate success
        Toast.makeText(getApplication().getApplicationContext(), newUser.getFirst_name() + " " + newUser.getLast_name() + " added successfully!", Toast.LENGTH_SHORT).show();

    }

    public static UsersViewModel getInstance(Application application, Context context) {
        if (instance == null) {
            instance = new UsersViewModel(application, context);
        }
        return instance;
    }

    public void init(Context context) {
        usersLiveData = new MutableLiveData<>();
        allUsers = new ArrayList<>();
        fetchUsers(context);
        indexItemSelected = new MutableLiveData<>();
    }

    public void showAdapter(Context context) {
        UsersFragment.usersAdapter = new UsersAdapter(getApplication(), context);
        UsersFragment.recyclerView.setAdapter(UsersFragment.usersAdapter);
    }

    private void fetchInitialUsers(Context context) {
        // Start with the first page
        loadNextPage(context);

    }

    private void fetchUsers(Context context) {
        // Fetch users from the Room database in a background thread
        // Observe the LiveData from the repository
        userRepository.getAllUsers().observeForever(userEntities -> {
            if (userEntities != null && !userEntities.isEmpty()) {
//                Toast.makeText(context, "userEntities: " + userEntities.get(0).getFirst_name(), Toast.LENGTH_SHORT).show();
                allUsers.clear();
                for (UserEntity entity : userEntities) {
                    allUsers.add(new UserEntity(entity.getId(), entity.getFirst_name(), entity.getLast_name(), entity.getEmail(), entity.getAvatar()));
                }

                usersLiveData.setValue(allUsers);
                showAdapter(context);
            } else {
                // If the database is empty, fetch users from the API
                fetchUsersFromApi(context,1);
            }
        });


    }

    private void fetchUsersFromApi(Context context, int pageNumber) {

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Dynamic API call based on the page number
        Call<UserResponse> call = apiService.getUsers(pageNumber);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserEntity> usersFromApi = response.body().getData();
                    if (pageNumber == 1) {
                        allUsers.clear(); // Clear only on the first page
                    }
                    for (UserEntity user : usersFromApi) {
                        allUsers.add(user);
                        // Save the user in the Room database via UserRepository in a background thread
                        executorService.execute(() -> userRepository.insert(new UserEntity(user.getId(), user.getFirst_name(), user.getLast_name(), user.getEmail(), user.getAvatar())));
                    }

                    // If there are more pages to fetch, make the next API call
                    int totalPages = response.body().getTotalPages(); // Assuming the API provides the total number of pages
                    if (pageNumber < totalPages) {
                        fetchUsersFromApi(context, pageNumber + 1); // Recursively call the next page
                    } else {
                        // Post the combined result back to the main thread after the last page

                        usersLiveData.setValue(allUsers);
                        errorLiveData.setValue(false);
                        showAdapter(context);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                handleApiFailure(t);
                errorLiveData.setValue(true);

            }
        });
    }

    private void handleApiFailure(Throwable t) {
        String errorMessage;
        if (t instanceof IOException) {
            errorMessage = "Network error, please check your connection";
        } else if (t instanceof HttpException) {
            errorMessage = "Server error, please try again later";
        } else {
            errorMessage = "Unknown error occurred";
        }
        Toast.makeText(getApplication().getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }
}
