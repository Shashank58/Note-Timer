package com.notetimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.widget.RelativeLayout;
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

//Todo Hide start timer when another task is already  running.
//Todo
public class TasksActivity extends AppCompatActivity {
    private static final String TAG = "Tasks Activity";
    @BindView(R.id.add_task)
    FloatingActionButton addTask;
    @BindView(R.id.task_list)
    RecyclerView taskList;
    @BindView(R.id.primary_layout)
    RelativeLayout primaryLayout;

    private EditText taskDescription;
    private CheckBox startTimer, showInNotification;
    private TasksAdapter adapter;
    private List<Object> listOfTasks;
    private TaskHelper taskHelper;
    private SharedPrefHandler sharedPrefHandler;
    private int adapterPosition;
    private String stoppedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        ButterKnife.bind(this);
        taskList.setLayoutManager(new LinearLayoutManager(this));
        taskList.setHasFixedSize(true);

        AppUtils.getInstance().itemClickAnimation(addTask);
        setAdapter();
        fetchDataFromSharedPref();
    }

    private void fetchDataFromSharedPref() {
        sharedPrefHandler = new SharedPrefHandler();
        adapterPosition = sharedPrefHandler.getAdapterPosition(this);
        stoppedTime = sharedPrefHandler.getStoppedTime(this);
        Log.d(TAG, "fetchDataFromSharedPref: Stopped time - " + stoppedTime);
    }

    private void setAdapter() {
        taskHelper = new TaskHelper(this);
        listOfTasks = new ArrayList<>(taskHelper.getTasks(this, AppConstants.ALL_TASKS));
        adapter = new TasksAdapter();
        taskList.setAdapter(adapter);
    }

    @OnClick(R.id.menu)
    void openMenu() {
        startActivity(new Intent(this, MenuActivity.class));
    }

    @OnClick(R.id.add_task)
    void addTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.add_task, null);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog dialog = builder.create();
        fetchViews(dialogView);
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
                if (resId == R.id.done && !taskDescription.getText().toString().trim().isEmpty()) {
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
        int isRunning = startTimer.isChecked() ? 1 : 0;
        Task task = new Task(description, isRunning, 0, 0, AppUtils.getInstance().getCurrentDate());
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
        AppUtils.getInstance().showKeyBoard(this, taskDescription);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (taskHelper.getAdapterPosition() != -1) {
            Log.d(TAG, "onStop: Saving stuff");
            sharedPrefHandler.saveTimeAndPosition(this, taskHelper.getAdapterPosition(),
                    taskHelper.getCurrentDateTime());
            TaskDBHelper.getInstance().updateTime(this, taskHelper.getTimeInSecs(),
                    ((Task) listOfTasks.get(taskHelper.getAdapterPosition())).getId());
            taskHelper.stopTimer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
            return listOfTasks.get(position) instanceof String ? AppConstants.TYPE_TITLE
                    : AppConstants.TYPE_TASK;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == AppConstants.TYPE_TITLE) {
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.day.setText((String) listOfTasks.get(holder.getAdapterPosition()));
            } else {
                Log.d(TAG, "onBindViewHolder: ");
                final TasksHolder tasksHolder = (TasksHolder) holder;
                Task task = (Task) listOfTasks.get(holder.getAdapterPosition());
                if (adapterPosition == holder.getAdapterPosition()) {
                    Log.d(TAG, "onBindViewHolder: Should not come here!");
                    task.setElapsedTime(taskHelper.calculateTimeDifference(stoppedTime, task.getElapsedTime()));
                    sharedPrefHandler.deleteAllData(TasksActivity.this);
                }
                tasksHolder.task.setText(task.getDescription());
                expandCard(tasksHolder, task, holder.getAdapterPosition());
                setTime(tasksHolder, task);
            }
        }

        private void setTime(TasksHolder tasksHolder, Task task) {
            Log.d(TAG, "setTime: Is running - " + task.getIsRunning());
            if (task.getIsRunning() == 1 && task.getIsStopped() == 0) {
                Log.d(TAG, "setTime: Elapsed time well - " + task.getElapsedTime());
                if (task.getElapsedTime() == -1) {
                    tasksHolder.time.setText(getString(R.string.not_supported));
                } else {
                    Log.d(TAG, "setTime: Coming here - " + tasksHolder.getAdapterPosition());
                    taskHelper.startTimer(tasksHolder.time, tasksHolder.getAdapterPosition(),
                            task.getElapsedTime());
                }
            } else {
                Log.d(TAG, "setTime: Elapsed time - " + task.getElapsedTime());
                tasksHolder.time.setText(task.getElapsedTime() == 0 ? "" :
                        taskHelper.convertToReadableFormat(task.getElapsedTime()));
            }
        }

        private void expandCard(final TasksHolder tasksHolder, final Task task, final int position) {
            tasksHolder.cardLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.getIsStopped() == 0) {
                        if (tasksHolder.actionsLayout.getVisibility() == View.GONE) {
                            showActions(task, tasksHolder);
                        } else {
                            tasksHolder.actionsLayout.setVisibility(View.GONE);
                        }
                    } else {
                        AppUtils.getInstance().showSnackBar(primaryLayout,
                                getString(R.string.task_finished));
                    }
                }
            });
        }

        private void showActions(Task task, TasksHolder tasksHolder) {
            if (task.getIsRunning() == 1) {
                tasksHolder.actions.get(1).setVisibility(View.GONE);
            } else {
                tasksHolder.actions.get(1).setVisibility(View.VISIBLE);
                playListener(tasksHolder.actions.get(1), tasksHolder.time,
                        tasksHolder.getAdapterPosition());
            }
            doneListener(tasksHolder.actions.get(0), task, tasksHolder);
            tasksHolder.actionsLayout.setVisibility(View.VISIBLE);
        }

        private void doneListener(ImageView done, final Task task, final TasksHolder tasksHolder) {
            done.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskDBHelper.getInstance().stopTimerForTask(TasksActivity.this,
                            1, taskHelper.getTimeInSecs(), task.getId());
                    Log.d(TAG, "onClick: Coming here");
                    task.setIsStopped(1);
                    task.setIsRunning(0);
                    task.setElapsedTime(taskHelper.getTimeInSecs());
                    taskHelper.stopTimer();
                    sharedPrefHandler.deleteAllData(TasksActivity.this);
                    tasksHolder.actionsLayout.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        private void playListener(final ImageView play, final TextView time, final int position) {
            play.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskHelper.getAdapterPosition() == -1) {
                        timerStart(time, position, play);
                    } else {
                        AppUtils.getInstance().showSnackBar(primaryLayout,
                                getString(R.string.stop_running_timer));
                    }
                }
            });
        }

        private void timerStart(TextView time, int position, ImageView play) {
            taskHelper.startTimer(time, position, 0);
            play.setVisibility(View.GONE);
            Task task = (Task) listOfTasks.get(position);
            task.setIsRunning(1);
            TaskDBHelper.getInstance().updateTimerStatus(TasksActivity.this,
                    task.getId(), task.getIsRunning());
        }

        @Override
        public int getItemCount() {
            return listOfTasks.size();
        }

        class TasksHolder extends RecyclerView.ViewHolder {
            protected
            @BindViews({R.id.done, R.id.play, R.id.edit, R.id.notification})
            List<ImageView> actions;
            protected
            @BindView(R.id.actions_layout)
            LinearLayout actionsLayout;
            protected
            @BindView(R.id.task)
            TextView task;
            protected
            @BindView(R.id.time)
            TextView time;
            protected
            @BindView(R.id.card_layout)
            CardView cardLayout;

            public TasksHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class TitleHolder extends RecyclerView.ViewHolder {
            protected
            @BindView(R.id.day)
            TextView day;

            public TitleHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
