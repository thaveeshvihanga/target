package lk.ac.kln.todoapplication.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.ac.kln.todoapplication.AddNewTask;
import lk.ac.kln.todoapplication.MainActivity;
import lk.ac.kln.todoapplication.Model.ToDoModel;
import lk.ac.kln.todoapplication.R;
import lk.ac.kln.todoapplication.Utils.DatabaseHandler;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private final MainActivity activity;
    private final DatabaseHandler db;

    public ToDoAdapter(MainActivity activity, DatabaseHandler db) {
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (todoList == null || position < 0 || position >= todoList.size()) return;

        db.openDatabase();

        final ToDoModel item = todoList.get(position);
        holder.task.setOnCheckedChangeListener(null);

        // Bind data
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));

        holder.task.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            final int newStatus = isChecked ? 1 : 0;
            item.setStatus(newStatus);
            new Thread(() -> db.updateStatus(item.getId(), newStatus)).start();
        });

        String desc = item.getDescription();
        String due = item.getDueDate();
        if (desc != null && !desc.isEmpty()) {
            holder.subtitle.setText(desc);
            holder.subtitle.setVisibility(View.VISIBLE);
        } else if (due != null && !due.isEmpty()) {
            holder.subtitle.setText("Due: " + due);
            holder.subtitle.setVisibility(View.VISIBLE);
        } else {
            holder.subtitle.setText("");
            holder.subtitle.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return todoList != null ? todoList.size() : 0;
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return activity;
    }

    public void deleteItem(int position) {
        if (todoList == null || position < 0 || position >= todoList.size()) return;

        final ToDoModel item = todoList.get(position);

        new Thread(() -> {
            db.deleteTask(item.getId());
            activity.runOnUiThread(() -> {

                if (position < todoList.size() && todoList.get(position).getId() == item.getId()) {
                    todoList.remove(position);
                    notifyItemRemoved(position);
                } else {
                    notifyDataSetChanged();
                }
            });
        }).start();
    }

    public ToDoModel getTaskAt(int position) {
        if (todoList == null || position < 0 || position >= todoList.size()) return null;
        return todoList.get(position);
    }

    public void editItem(int position) {
        ToDoModel item = getTaskAt(position);
        if (item == null) return;

        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("description", item.getDescription());
        bundle.putString("dueDate", item.getDueDate());
        bundle.putString("tags", item.getTags());

        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView subtitle;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.ToDoCheckBox);
            subtitle = view.findViewById(R.id.todo_subtitle);
        }
    }
}
