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
import android.os.Handler;
import android.support.annotation.UiThread;
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

//Todo Hide start timer when another task is already running.
public class TasksActivity extends AppCompatActivity {
    private static final String TAG = "Tasks Activity";
    @BindView(R.id.task_list)
    RecyclerView taskList;
    @BindView(R.id.primary_layout)
    RelativeLayout primaryLayout;

    private EditText taskDescription;
    private CheckBox startTimer;
    private TasksAdapter adapter;
    private List<Object> listOfTasks;
    private TaskHelper taskHelper;
    private SharedPrefHandler sharedPrefHandler;
    private int editPosition = -1;
    private long runningTaskId;
    private String stoppedTime;
    private Handler handler = new Handler();
    private final int DONE_TASK = 50;
    private final int PAUSED_TASK = 60;
    private final int STARTED_TASK = 70;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        ButterKnife.bind(this);
        taskList.setLayoutManager(new LinearLayoutManager(this));
        taskList.setHasFixedSize(true);

        setAdapter();
        fetchDataFromSharedPref();
    }

    private void fetchDataFromSharedPref() {
        sharedPrefHandler = new SharedPrefHandler();
        runningTaskId = sharedPrefHandler.getTaskId(this);
        stoppedTime = sharedPrefHandler.getStoppedTime(this);
        Log.d(TAG, "fetchDataFromSharedPref: Stopped time - " + stoppedTime);
    }

    private void setAdapter() {
        taskHelper = new TaskHelper();
        listOfTasks = new ArrayList<>(taskHelper.getTasks(this));
        adapter = new TasksAdapter();
        taskList.setAdapter(adapter);
    }

    @OnClick(R.id.menu)
    void openMenu() {
        startActivity(new Intent(this, MenuActivity.class));
    }

    @OnClick(R.id.add_task)
    void addTask() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showAddTaskDialog("");
            }
        }, 200);
    }

    private void showAddTaskDialog(String task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.add_task, null);
        builder.setView(dialogView).setCancelable(false);
        final AlertDialog dialog = builder.create();
        fetchViews(dialogView, task);
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

    private void fetchViews(View dialogView, String task) {
        taskDescription = (EditText) dialogView.findViewById(R.id.task_to_be_done);
        startTimer = (CheckBox) dialogView.findViewById(R.id.timer_check_box);
        taskDescription.setText(task);
    }

    private void hideDialog(final AlertDialog dialog, final int resId, final View dialogView) {
        dialogView.findViewById(resId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resId == R.id.done) {
                    storeTask();
                }
                revealShow(dialogView, false, dialog);
                editPosition = -1;
            }
        });
    }

    private void storeTask() {
        String description = taskDescription.getText().toString().trim();
        if (description.isEmpty()) {
            return;
        }
        if (editPosition != -1) {
            Task task = (Task) listOfTasks.get(editPosition);
            task.setDescription(description);
            adapter.notifyItemChanged(editPosition);
        } else {
            int isRunning = startTimer.isChecked() ? AppConstants.TASK_STATUS_RUNNING :
                    AppConstants.TASK_STATUS_IDLE;
            Task task = new Task(description, isRunning, 0, AppUtils.getInstance().getCurrentDate());
            long id = TaskDBHelper.getInstance().insertTask(this, task);
            taskDescription = null;
            task.setId(id);
            addToAdapter(task);
        }
    }

    private void addToAdapter(Task task) {
        if (task.getTaskStatus() == AppConstants.TASK_STATUS_IDLE) {
            if (listOfTasks.size() < 1) {
                listOfTasks.add(getString(R.string.pending_tasks));
                listOfTasks.add(task);
                return;
            }
            int posOfPending = listOfTasks.indexOf(getString(R.string.pending_tasks));
            if (posOfPending != -1) {
                listOfTasks.add(posOfPending + 1, task);
            } else {
                int pos = getPendingPosition();
                listOfTasks.add(pos, getString(R.string.pending_tasks));
                listOfTasks.add(pos + 1, task);
            }
        } else {
            if (listOfTasks.size() < 1) {
                listOfTasks.add(getString(R.string.running_task));
                listOfTasks.add(task);
                return;
            }
            if (listOfTasks.get(0).equals(getString(R.string.running_task))) {
                listOfTasks.add(1, task);
            } else {
                listOfTasks.add(0, getString(R.string.running_task));
                listOfTasks.add(1, task);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private int getPendingPosition() {
        switch ((String) listOfTasks.get(0)) {
            case "Running":
                int pos = 1;
                for (int i = 1; i < listOfTasks.size(); i++) {
                    if (listOfTasks.get(i) instanceof Task || (listOfTasks.get(i)
                            .equals("Paused"))) {
                        pos++;
                    } else {
                        break;
                    }
                }
                return pos;

            case "Paused":
                int position = 1;
                for (int i = 1; i < listOfTasks.size(); i++) {
                    if (listOfTasks.get(i) instanceof Task) {
                        position++;
                    } else {
                        break;
                    }
                }
                return position;

            case "Finished":

                return 0;

            default:
                return 0;
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
            }
        });

        anim.start();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private void showDialog(View view, int w, int h, float maxRadius) {
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view,
                w, h, 0, maxRadius);

        view.setVisibility(View.VISIBLE);
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                AppUtils.getInstance().showKeyBoard(TasksActivity.this, taskDescription);
            }
        });
        revealAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (taskHelper.getAdapterPosition() != -1) {
            Task task = (Task) listOfTasks.get(taskHelper.getAdapterPosition());
            sharedPrefHandler.saveTimeAndId(this, task.getId(),
                    AppUtils.getInstance().getCurrentDateTime());
            TaskDBHelper.getInstance().updateTime(this, taskHelper.getTimeInSecs(),
                    task.getId());
            startNotification();
            taskHelper.stopTimer();
        }
    }

    private void startNotification() {
        final Intent intent = new Intent(this, TimerService.class);
        Task task = (Task) listOfTasks.get(taskHelper.getAdapterPosition());
        intent.putExtra(AppConstants.TIME, taskHelper.getTimer());
        intent.putExtra(AppConstants.DESCRIPTION, task.getDescription());
        intent.putExtra(AppConstants.TASK_ID, task.getId());
        intent.putExtra(AppConstants.TIME_IN_SECS, taskHelper.getTimeInSecs());
        Thread thread = new Thread() {
            @Override
            public void run() {
                startService(intent);
            }
        };
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction(AppConstants.STOP_NOTIFICATION);
        startService(intent);
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
                final TasksHolder tasksHolder = (TasksHolder) holder;
                Task task = (Task) listOfTasks.get(holder.getAdapterPosition());
                if (runningTaskId == task.getId()) {
                    task.setElapsedTime(taskHelper.calculateTimeDifference(stoppedTime, task.getElapsedTime()));
                    sharedPrefHandler.deleteAllData(TasksActivity.this);
                }
                tasksHolder.task.setText(task.getDescription());
                expandCard(tasksHolder, task);
                setTime(tasksHolder, task);
                tasksHolder.createdAt.setText(task.getCreatedAt().replaceAll("-", " "));
            }
        }

        private void setTime(TasksHolder tasksHolder, Task task) {
            if (task.getTaskStatus() == AppConstants.TASK_STATUS_RUNNING) {
                if (task.getElapsedTime() == -1) {
                    tasksHolder.time.setText(getString(R.string.not_supported));
                } else {
                    taskHelper.startTimer(tasksHolder.time, tasksHolder.getAdapterPosition(),
                            task.getElapsedTime());
                }
            } else {
                tasksHolder.time.setText(task.getElapsedTime() < 1 ? "" :
                        taskHelper.convertToReadableFormat(task.getElapsedTime()));
            }
        }

        private void expandCard(final TasksHolder tasksHolder, final Task task) {
            tasksHolder.cardLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task.getTaskStatus() != AppConstants.TASK_STATUS_FINISHED) {
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
            if (task.getTaskStatus() == AppConstants.TASK_STATUS_RUNNING){
                tasksHolder.actions.get(1).setImageResource(R.drawable.ic_pause);
            } else {
                tasksHolder.actions.get(1).setImageResource(R.drawable.ic_play_arrow);
            }
            playListener(tasksHolder.actions.get(1), tasksHolder, task);
            doneListener(tasksHolder.actions.get(0), task, tasksHolder);
            editListener(tasksHolder.actions.get(2), task.getDescription(),
                    tasksHolder.getAdapterPosition());
            tasksHolder.actionsLayout.setVisibility(View.VISIBLE);
        }

        private void editListener(ImageView edit, final String description, final int pos) {
            edit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editPosition = pos;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showAddTaskDialog(description);
                        }
                    }, 200);
                }
            });
        }

        private void doneListener(ImageView done, final Task task, final TasksHolder tasksHolder) {
            done.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        @UiThread
                        public void run() {
                            TaskDBHelper.getInstance().stopTimerForTask(TasksActivity.this,
                                    AppConstants.TASK_STATUS_FINISHED, taskHelper.getTimeInSecs(), task.getId());
                            task.setTaskStatus(AppConstants.TASK_STATUS_FINISHED);
                            task.setElapsedTime(taskHelper.getTimeInSecs());
                            taskHelper.stopTimer();
                            sharedPrefHandler.deleteAllData(TasksActivity.this);
                            tasksHolder.actionsLayout.setVisibility(View.GONE);
                            reOrder(tasksHolder.getAdapterPosition(), DONE_TASK);
                        }
                    }, 200);
                }
            });
        }

        private void reOrder(int pos, int type) {
            Task task = (Task) listOfTasks.get(pos);
            switch (type) {
                case DONE_TASK:
                    int finishedPos = listOfTasks.indexOf(getString(R.string.finished_tasks));
                    listOfTasks.remove(pos);
                    if (finishedPos == -1) {
                        listOfTasks.add(getString(R.string.finished_tasks));
                        listOfTasks.add(task);
                    } else {
                        listOfTasks.add(finishedPos + 1, task);
                    }
                    break;

                case PAUSED_TASK:
                    int pausePos = listOfTasks.indexOf(getString(R.string.paused_tasks));
                    listOfTasks.remove(pos);
                    if (pausePos == -1) {
                        int positionToInsert = findPausePosition();
                        listOfTasks.add(positionToInsert, getString(R.string.paused_tasks));
                        listOfTasks.add(positionToInsert + 1, task);
                    } else {
                        listOfTasks.add(pausePos + 1, task);
                    }
                    break;

                case STARTED_TASK:
                    listOfTasks.remove(pos);
                    if (!listOfTasks.contains(getString(R.string.running_task))) {
                        listOfTasks.add(0, getString(R.string.running_task));
                        listOfTasks.add(1, task);
                    } else {
                        listOfTasks.add(1, task);
                    }
                    break;
            }
            adapter.notifyDataSetChanged();
        }

        private int findPausePosition() {
            switch ((String)listOfTasks.get(0)) {
                case "Running":
                    int pos = 1;
                    for(int i = 1; i < listOfTasks.size(); i++) {
                        if (listOfTasks.get(i) instanceof Task)
                            pos++;
                        else
                            break;
                    }
                    return pos;

                case "Pending":

                    return 0;

                case "Finished":

                    return 0;

                default:
                    return 0;
            }
        }

        private void playListener(final ImageView play, final TasksHolder tasksHolder,
                                  final Task task ) {
            play.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (task.getTaskStatus() != AppConstants.TASK_STATUS_RUNNING) {
                                tryStartingTimer(tasksHolder, play);
                                reOrder(tasksHolder.getAdapterPosition(), STARTED_TASK);
                            } else  {
                                pauseTask(task, tasksHolder);
                                taskHelper.stopTimer();
                                reOrder(tasksHolder.getAdapterPosition(), PAUSED_TASK);
                            }
                        }
                    }, 200);
                }
            });
        }

        private void pauseTask(Task task, TasksHolder tasksHolder) {
            task.setElapsedTime(taskHelper.getTimeInSecs());
            task.setTaskStatus(AppConstants.TASK_STATUS_PAUSED);
            tasksHolder.actions.get(1).setImageResource(R.drawable.ic_play_arrow);
            TaskDBHelper.getInstance().updateTime(TasksActivity.this,
                    taskHelper.getTimeInSecs(), task.getId());
            TaskDBHelper.getInstance().updateTimerStatus(TasksActivity.this,
                    task.getId(), AppConstants.TASK_STATUS_PAUSED);
        }

        private void tryStartingTimer(TasksHolder tasksHolder, ImageView play) {
            if (taskHelper.getAdapterPosition() == -1) {
                timerStart(tasksHolder.time, tasksHolder.getAdapterPosition(), play);
            } else {
                AppUtils.getInstance().showSnackBar(primaryLayout,
                        getString(R.string.stop_running_timer));
            }
        }

        private void timerStart(TextView time, int position, ImageView play) {
            play.setImageResource(R.drawable.ic_pause);
            Task task = (Task) listOfTasks.get(position);
            task.setTaskStatus(AppConstants.TASK_STATUS_RUNNING);
            taskHelper.startTimer(time, position, task.getElapsedTime());
            TaskDBHelper.getInstance().updateTimerStatus(TasksActivity.this,
                    task.getId(), task.getTaskStatus());
        }

        @Override
        public int getItemCount() {
            return listOfTasks.size();
        }

        class TasksHolder extends RecyclerView.ViewHolder {
            protected @BindViews({R.id.done, R.id.play, R.id.edit}) List<ImageView> actions;
            protected @BindView(R.id.actions_layout) LinearLayout actionsLayout;
            protected @BindView(R.id.task) TextView task;
            protected @BindView(R.id.time) TextView time;
            protected @BindView(R.id.card_layout) CardView cardLayout;
            protected @BindView(R.id.created_date) TextView createdAt;

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
