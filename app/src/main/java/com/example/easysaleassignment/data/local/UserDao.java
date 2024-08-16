package com.example.easysaleassignment.data.local;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface  UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users ORDER BY first_name ASC LIMIT :limit OFFSET :offset")
    LiveData<List<UserEntity>> getUsersWithPagination(int limit, int offset);

    @Query("SELECT * FROM users ORDER BY first_name ASC")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users WHERE first_name LIKE :query OR last_name LIKE :query ORDER BY first_name ASC")
    LiveData<List<UserEntity>> searchUsers(String query);

    @Query("SELECT * FROM users WHERE email = :userEmail LIMIT 1")
    LiveData<UserEntity> getUserByEmail(String userEmail);

    @Query("SELECT COUNT(*) FROM users") // Replace "user_table" with your actual table name if different
    LiveData<Integer> getUserCount();
}
