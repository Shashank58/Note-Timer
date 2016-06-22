package com.notetimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author shashankm
 */
public class TasksActivity extends AppCompatActivity{
    private static final String TAG = "Tasks Activity";
    @BindView(R.id.add_task)
    FloatingActionButton addTask;
    @BindView(R.id.task_list)
    RecyclerView taskList;

    private EditText taskDescription;
    private CheckBox startTimer, showInNotification;
    private TasksAdapter adapter;
    private List<Object> listOfTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        ButterKnife.bind(this);
        taskList.setLayoutManager(new LinearLayoutManager(this));
        taskList.setHasFixedSize(true);

        setAdapter();
    }

    private void setAdapter() {
        TaskHelper taskHelper = new TaskHelper();
        listOfTasks = new ArrayList<>(taskHelper.getTasks(this, AppConstants.ALL_TASKS));
        adapter = new TasksAdapter();
        taskList.setAdapter(adapter);
    }

    @OnClick(R.id.add_task)
    void addTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.add_task, null);
        fetchViews(dialogView);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog dialog = builder.create();

        if (VERSION.SDK_INT >= 21) {
            dialog.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    revealShow(dialogView, true, null);
                }
            });

            hideDialog(dialog, R.id.done, dialogView);
            hideDialog(dialog, R.id.cancel, dialogView);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void fetchViews(View dialogView) {
        if (taskDescription == null || startTimer == null || showInNotification == null) {
            Log.d(TAG, "fetchViews: Entering");
            taskDescription = (EditText) dialogView.findViewById(R.id.task_to_be_done);
            startTimer = (CheckBox) dialogView.findViewById(R.id.timer_check_box);
            showInNotification = (CheckBox) dialogView.findViewById(R.id.show_notification_check_box);
        }
    }

    private void hideDialog(final AlertDialog dialog, final int resId, final View dialogView) {
        dialogView.findViewById(resId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resId == R.id.done) {
                   storeTask();
                }
                revealShow(dialogView, false, dialog);
            }
        });
    }

    private void storeTask() {
        String description = taskDescription.getText().toString().trim();
        Log.d(TAG, "storeTask: Description - " + description);
        if (description.isEmpty()) {
            return;
        }
        int isRunning = startTimer.isChecked()? 1 : 0;
        Task task = new Task(description, isRunning, "", "", AppUtils.getInstance().getCurrentDate());
        long id = TaskDBHelper.getInstance().insertTask(this, task);
        taskDescription = null;
        task.setId(id);
        addToAdapter(task);
    }

    private void addToAdapter(Task task) {
        if (listOfTasks.size() < 1) {
            listOfTasks.add("Today");
            listOfTasks.add(task);
            adapter.notifyDataSetChanged();
        } else {
            listOfTasks.add(task);
            adapter.notifyDataSetChanged();
        }
    }

    private void revealShow(View rootView, boolean reveal, final AlertDialog dialog) {
        final View view = rootView.findViewById(R.id.root_view);
        int w = view.getRight();
        int h = view.getBottom();
        float maxRadius = (int) Math.hypot(view.getWidth(), view.getHeight());

        if (reveal) {
            showDialog(view, w, h, maxRadius);
        } else {
            hideDialog(dialog, view, w, h, maxRadius);
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void hideDialog(final AlertDialog dialog, final View view, int w, int h, float maxRadius) {
        Animator anim = ViewAnimationUtils.createCircularReveal(view, w, h, maxRadius, 0);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dialog.dismiss();
                view.setVisibility(View.INVISIBLE);
                addTask.setVisibility(View.VISIBLE);
            }
        });

        anim.start();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void showDialog(View view, int w, int h, float maxRadius) {
        addTask.setVisibility(View.INVISIBLE);
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view,
                w, h, 0, maxRadius);

        view.setVisibility(View.VISIBLE);
        revealAnimator.start();
    }

    class TasksAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == AppConstants.TYPE_TITLE) {
                viewHolder = new TitleHolder(inflater.inflate(R.layout.day, parent, false));
            } else {
                viewHolder = new TasksHolder(inflater.inflate(R.layout.task_list_item, parent, false));
            }

            return viewHolder;
        }

        @Override
        public int getItemViewType(int position) {
            return listOfTasks.get(position) instanceof String? AppConstants.TYPE_TITLE
                    : AppConstants.TYPE_TASK;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == AppConstants.TYPE_TITLE) {
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.day.setText((String)listOfTasks.get(holder.getAdapterPosition()));
            } else {
                TasksHolder tasksHolder = (TasksHolder) holder;
                Task task = (Task) listOfTasks.get(holder.getAdapterPosition());
                tasksHolder.task.setText(task.getDescription());
            }
        }

        @Override
        public int getItemCount() {
            return listOfTasks.size();
        }

        class TasksHolder extends RecyclerView.ViewHolder {
            protected @BindViews({R.id.done, R.id.play, R.id.edit, R.id.notification})
            List<ImageView> actions;
            protected @BindView(R.id.actions_layout) LinearLayout actionsLayout;
            protected @BindView(R.id.task) TextView task;
            protected @BindView(R.id.time) TextView time;

            public TasksHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class TitleHolder extends RecyclerView.ViewHolder {
            protected @BindView(R.id.day) TextView day;

            public TitleHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
