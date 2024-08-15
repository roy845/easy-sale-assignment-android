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

    @Query("SELECT * FROM users LIMIT :limit OFFSET :offset")
    LiveData<List<UserEntity>> getUsersWithPagination(int limit, int offset);

    @Query("SELECT * FROM users ORDER BY first_name ASC")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users WHERE first_name LIKE :query OR last_name LIKE :query ORDER BY first_name ASC")
    LiveData<List<UserEntity>> searchUsers(String query);
}
