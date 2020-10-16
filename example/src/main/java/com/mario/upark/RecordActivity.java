package com.mario.upark;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mario.upark.common.Global;
import com.mario.upark.common.History;
import com.mario.upark.common.HistoryAdapter;

import java.util.ArrayList;
import java.util.List;

/* import android.support.v7.app.AppCompatActivity; */

public class RecordActivity extends AppCompatActivity {

    HistoryAdapter historyAdapter;
    List<History> histories = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        initUIView();
        initJsonData();
    }

    private void initJsonData() {
        ProgressDialog dialog = Global.onShowProgressDialog(this, getResources().getString(R.string.login_server_connect), false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference().child("Histories").child(Global.gUser.id);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Global.onDismissProgressDialog(dialog);

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    History history = postSnapshot.getValue(History.class);
                    histories.add(history);
                }

                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Global.onDismissProgressDialog(dialog);
            }
        });
    }

    private void initUIView() {
        historyAdapter = new HistoryAdapter(RecordActivity.this, histories);
        listView = findViewById(R.id.lv_record);
        listView.setAdapter(historyAdapter);
    }

    public void onClickBackIcon(View view) {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

    @Override
    public void onBackPressed() {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

}
