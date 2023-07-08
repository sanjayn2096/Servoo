package com.example.servoo.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.servoo.R;
import com.example.servoo.adapters.OrdersAdapter;
import com.example.servoo.data.Order;
import com.example.servoo.service.OrderStatusService;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private static final String TAG = "LiveOrdersFragment";

    private RecyclerView recyclerView;
    private OrdersAdapter ordersAdapter;
    private List<Order> orderList;

    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();

        // Start the OrderStatusService
        startOrderStatusService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(orderList);
        recyclerView.setAdapter(ordersAdapter);

        // Load initial orders
        loadOrders();

        return view;
    }

    private void loadOrders() {
        firestore.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        Order order = documentChange.getDocument().toObject(Order.class);
                        orderList.add(order);
                    }
                    ordersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching orders: " + e.getMessage()));
    }

    private void startOrderStatusService() {
        Context context = requireContext();
        Intent serviceIntent = new Intent(context, OrderStatusService.class);
        context.startService(serviceIntent);
    }

    public static OrdersFragment newInstance() {
        return new OrdersFragment();
    }
}