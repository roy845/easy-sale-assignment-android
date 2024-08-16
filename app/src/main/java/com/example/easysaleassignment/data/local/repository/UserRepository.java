package com.example.easysaleassignment.data.local.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import com.example.easysaleassignment.data.local.AppDatabase;
import com.example.easysaleassignment.data.local.UserDao;
import com.example.easysaleassignment.data.local.UserEntity;

import java.util.List;

public class UserRepository {
    private final UserDao userDao;
    private final LiveData<List<UserEntity>> allUsers;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        allUsers = userDao.getAllUsers();
    }

    public LiveData<List<UserEntity>> getUsersWithPagination(int limit, int offset) {
        return userDao.getUsersWithPagination(limit, offset);
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<UserEntity>> searchUsers(String query) {
        return userDao.searchUsers("%" + query + "%");
    }

    public void insert(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.insert(user));
    }

    public void update(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.update(user));
    }

    public void delete(UserEntity user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.delete(user));
    }

    public LiveData<UserEntity> getUserByEmail(String email) {
       return userDao.getUserByEmail(email);
    }

    public LiveData<Integer> getUserCount() {
        return userDao.getUserCount();
    }

}
