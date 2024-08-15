package com.example.easysaleassignment.models;

import com.example.easysaleassignment.data.local.UserEntity;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserResponse {
    @SerializedName("data")
    private List<UserEntity> data;
    @SerializedName("total_pages")
    private int total_pages;
    @SerializedName("total")
    private int total;
    @SerializedName("per_page")
    private int per_page;

    public List<UserEntity> getData() {
        return data;
    }

    public int getTotal() {
        return total;
    }

    public int getPerPage() {
        return per_page;
    }

    public int getTotalPages() {
        return total_pages;
    }
}
