package com.example.easysaleassignment.adapters;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.easysaleassignment.R;
import com.example.easysaleassignment.data.local.UserEntity;
import com.example.easysaleassignment.interfaces.IUserAdapterListener;
import com.example.easysaleassignment.viewmodels.UsersViewModel;
import java.io.IOException;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    public static int rowIndex = -1;
    private IUserAdapterListener listener;
    private UsersViewModel viewModel;
    Application application;
    Context context;


    public static List<UserEntity> usersArrayList;

    public UsersAdapter(Application application, Context context) {
        this.application = application;
        this.context = context;
        viewModel = UsersViewModel.getInstance(application, context);
        usersArrayList = viewModel.getUsersLiveData().getValue();
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        listener = (IUserAdapterListener) parent.getContext();
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {
        UserEntity user = usersArrayList.get(position);
        if (rowIndex == position){
            holder.row_linearlayout.setBackground(ContextCompat.getDrawable(context,R.drawable.lines2));
            holder.firstNameTextView.setTextColor(Color.BLACK);
            holder.lastNameTextView.setTextColor(Color.BLACK);
            holder.emailTextView.setTextColor(Color.BLACK);
        } else {
            holder.row_linearlayout.setBackground(ContextCompat.getDrawable(context,R.drawable.lines));
            holder.firstNameTextView.setTextColor(Color.WHITE);
            holder.lastNameTextView.setTextColor(Color.WHITE);
            holder.emailTextView.setTextColor(Color.WHITE);
        }
        //Regular Click
        holder.row_linearlayout.setOnClickListener(v -> {

            rowIndex = position;
            notifyItemChanged(rowIndex);
            notifyDataSetChanged();
            viewModel.setItemSelect(user);
            listener.userClicked();
        });


        try {
            holder.bindData(user.getFirst_name(), user.getLast_name(),user.getEmail(),user.getAvatar());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return usersArrayList!=null ? usersArrayList.size():0;
    }

    public void setUsers(List<UserEntity> usersList){
        this.usersArrayList = usersList;
        notifyDataSetChanged();
    }


    public UserEntity getUser(int position) {
        return usersArrayList.get(position);
    }


    public class UserViewHolder extends RecyclerView.ViewHolder{

        final Context context;
        final View userItem;
        final ImageView avatarImageView;
        final TextView firstNameTextView;
        final TextView lastNameTextView;
        final TextView emailTextView;
        LinearLayout row_linearlayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            this.userItem = itemView.findViewById(R.id.user_item);

            this.avatarImageView = itemView.findViewById(R.id.avatar);
            this.firstNameTextView = itemView.findViewById(R.id.first_name);
            this.lastNameTextView = itemView.findViewById(R.id.last_name);
            emailTextView = itemView.findViewById(R.id.email);
            row_linearlayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);
        }

        public void bindData(String firstName, String lastName,String email,String avatarUrl) throws IOException {
            firstNameTextView.setText(firstName);
            lastNameTextView.setText(lastName);
            emailTextView.setText(email);

            Glide.with(context)
                    .load(avatarUrl)
                    .into(avatarImageView);

        }
    }



}
