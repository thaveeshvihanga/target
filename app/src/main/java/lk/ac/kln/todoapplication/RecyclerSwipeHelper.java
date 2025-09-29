package lk.ac.kln.todoapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import lk.ac.kln.todoapplication.Adapter.ToDoAdapter;

public class RecyclerSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private final ToDoAdapter adapter;

    public RecyclerSwipeHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        if (direction == ItemTouchHelper.LEFT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task");
            builder.setMessage("Do you want to DELETE this task?");
            builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                adapter.deleteItem(position);
            });
            builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {

                adapter.notifyItemChanged(position);
                dialogInterface.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            adapter.notifyItemChanged(position);
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        Drawable icon = null;
        ColorDrawable background;

        int backgroundCornerOffset = 20;

        if (dX > 0) { // right swipe
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.baseline_edit_note_24);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
        } else { // left swipe
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.baseline_delete_24);
            background = new ColorDrawable(Color.RED);
        }

        // draw background
        if (dX > 0) {
            background.setBounds(itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
        } else if (dX < 0) {
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);

        // draw icon
        if (icon != null) {
            int iconHeight = icon.getIntrinsicHeight();
            int iconWidth = icon.getIntrinsicWidth();
            int iconMargin = (itemView.getHeight() - iconHeight) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + iconHeight;

            if (dX > 0) {
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + iconWidth;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else if (dX < 0) {
                int iconLeft = itemView.getRight() - iconMargin - iconWidth;
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else {
                icon.setBounds(0, 0, 0, 0);
            }

            icon.draw(c);
        }


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
