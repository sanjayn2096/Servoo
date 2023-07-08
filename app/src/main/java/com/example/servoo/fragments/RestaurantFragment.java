package com.example.servoo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.servoo.R;
import com.example.servoo.databinding.FragmentRestaurantBinding;

public class RestaurantFragment extends Fragment {

    private FragmentRestaurantBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRestaurantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        String userInfoJson = savedInstanceState.getArguments().getString("userInfo");
//        UserInfo userInfo = new Gson().fromJson(userInfoJson, UserInfo.class);
//        binding.textViewUserName.setText(userInfo.getFirstName());
        binding.addRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(RestaurantFragment.this)
                        .navigate(R.id.action_Launch_Restaurant_Fragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}