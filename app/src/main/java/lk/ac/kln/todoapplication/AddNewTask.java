package lk.ac.kln.todoapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Objects;

import lk.ac.kln.todoapplication.Model.ToDoModel;
import lk.ac.kln.todoapplication.Utils.DatabaseHandler;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";

    private EditText newTaskText;
    private EditText newTaskDesc;
    private TextView newTaskDueDate;
    private EditText newTaskTags;
    private Button newTaskSaveButton;
    private DatabaseHandler db;
    private String selectedDueDate = null;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.new_task, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            Objects.requireNonNull(getDialog().getWindow())
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceStatus){
        super.onViewCreated(view, savedInstanceStatus);
        newTaskText = view.findViewById(R.id.newTaskText);
        newTaskDesc = view.findViewById(R.id.newTaskDesc);
        newTaskDueDate = view.findViewById(R.id.newTaskDueDate);
        newTaskTags = view.findViewById(R.id.newTaskTags);
        newTaskSaveButton = view.findViewById(R.id.newTaskButton);

        db = new DatabaseHandler(requireActivity());
        db.openDatabase();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            String desc = bundle.getString("description");
            String due = bundle.getString("dueDate");
            String tags = bundle.getString("tags");

            newTaskText.setText(task);
            newTaskDesc.setText(desc != null ? desc : "");
            selectedDueDate = due;
            newTaskDueDate.setText(due != null ? due : "Pick due date (optional)");
            newTaskTags.setText(tags != null ? tags : "");

            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
        } else {
            newTaskSaveButton.setEnabled(false);
            newTaskSaveButton.setTextColor(Color.GRAY);
            newTaskDueDate.setText("Pick due date (optional)");
        }

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                } else {
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
                }
            }
        });

        newTaskDueDate.setOnClickListener(v -> showDatePicker());

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(v -> {
            String text = newTaskText.getText().toString().trim();
            String desc = newTaskDesc.getText().toString().trim();
            String tags = newTaskTags.getText().toString().trim();
            String due = selectedDueDate;

            if (text.isEmpty()) return;

            SharedPreferences prefs = requireActivity().getSharedPreferences("todo_prefs", requireActivity().MODE_PRIVATE);
            int currentUserId = prefs.getInt("current_user_id", -1);
            if (currentUserId == -1) {
                dismiss();
                return;
            }

            if (finalIsUpdate && bundle != null) {
                int id = bundle.getInt("id");
                new Thread(() -> {
                    db.updateTask(id, text, desc, due, tags);
                    requireActivity().runOnUiThread(this::dismiss);
                }).start();
            } else {
                new Thread(() -> {
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setStatus(0);
                    task.setDescription(desc);
                    task.setDueDate(due);
                    task.setTags(tags);
                    task.setUserId(currentUserId); // link task to user
                    db.insertTask(task);
                    requireActivity().runOnUiThread(this::dismiss);
                }).start();
            }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String formatted = String.format("%04d-%02d-%02d", y, m + 1, d);
            selectedDueDate = formatted;
            newTaskDueDate.setText(formatted);
        }, year, month, day);

        dpd.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListner){
            ((DialogCloseListner)activity).handleDialogClose(dialog);
        }
    }
}
