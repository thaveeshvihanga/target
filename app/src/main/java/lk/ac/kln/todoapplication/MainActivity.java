package lk.ac.kln.todoapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Collections;
import java.util.List;

import lk.ac.kln.todoapplication.Adapter.ToDoAdapter;
import lk.ac.kln.todoapplication.Model.ToDoModel;
import lk.ac.kln.todoapplication.Utils.DatabaseHandler;

public class MainActivity extends AppCompatActivity implements DialogCloseListner {

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private View rootView;
    private ImageButton btnLogout;

    private List<ToDoModel> taskList;
    private DatabaseHandler db;
    private int currentUserId = -1;
    private String currentUsername = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.root_main);
        btnLogout = findViewById(R.id.logout);

        db = new DatabaseHandler(this);
        db.openDatabase();

        // Read session
        SharedPreferences prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        currentUserId = getIntent().getIntExtra("user_id", prefs.getInt("current_user_id", -1));
        currentUsername = prefs.getString("current_username", null);

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {

            prefs.edit().putInt("current_user_id", currentUserId).apply();
            if (getIntent().hasExtra("user_id") && getIntent().hasExtra("username")) {

                String intentUsername = getIntent().getStringExtra("username");
                if (intentUsername != null) {
                    currentUsername = intentUsername;
                    prefs.edit().putString("current_username", currentUsername).apply();
                }
            }
        }


        // Logout
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        SharedPreferences p = getSharedPreferences("todo_prefs", MODE_PRIVATE);
                        p.edit()
                                .remove("current_user_id")
                                .remove("current_username")
                                .remove("seen_onboarding_for_user_" + currentUserId)
                                .apply();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        tasksRecyclerView = findViewById(R.id.tasksRecycleView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(this, db);
        tasksRecyclerView.setAdapter(tasksAdapter);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerSwipeHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);


        loadTasksForUser();
        findViewById(R.id.fab).setOnClickListener(view -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));
    }

    private void loadTasksForUser() {
        taskList = db.getTasksByUser(currentUserId);
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        loadTasksForUser();
    }





}
