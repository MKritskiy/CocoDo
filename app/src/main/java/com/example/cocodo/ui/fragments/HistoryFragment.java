package com.example.cocodo.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocodo.MainActivity;
import com.example.cocodo.R;
import com.example.cocodo.database.MyDatabase;
import com.example.cocodo.utils.RecyclerTaskListAdapter;
import com.example.cocodo.utils.SpacesItemDecoration;
import com.example.cocodo.utils.Task;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {
    public interface UpdaterRecViewList{
        void updateTaskRecView();
    }
    private TaskListFragment.UpdaterRecViewList updaterRecViewList;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            updaterRecViewList = (TaskListFragment.UpdaterRecViewList) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentButtonClickListener");
        }

    }
    private static List<Task> taskList;
    private static RecyclerTaskListAdapter adapter;
    private static RecyclerView recyclerView;
    static FragmentManager fragmentManager;
    ImageButton historyBackImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        recyclerView = view.findViewById(R.id.history_rec_view);
        historyBackImageButton = view.findViewById(R.id.history_back_image_button);
        updateTaskRecView();
        fragmentManager = getParentFragmentManager();
        historyBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return view;
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private final Context context;
        Task task;
        ItemTouchHelper itemTouchHelper;

        public LoadDataTask(Context context, Task task) {
            this.context = context.getApplicationContext();
            this.task = task;
        }

        public LoadDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
                taskList = MyDatabase.getDatabase(context).taskDao().getCompletedTasksSortedByDate();
                adapter = null;
//              for (Task t:taskList) {
//                  MyDatabase.getDatabase(context).taskDao().deleteAllSubTasks();
//                  MyDatabase.getDatabase(context).taskDao().deleteAllTasks();
//
//              }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (adapter == null) {
                adapter = new RecyclerTaskListAdapter(context, taskList, recyclerView, MyDatabase.getDatabase(context).taskDao());
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new RecyclerTaskListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MainActivity.onTaskListItemClickListener(view, taskList.get(position));
                    }
                });
                itemTouchHelper =
                        new ItemTouchHelper(
                                new ItemTouchHelper.SimpleCallback(
                                        0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                                        int swipedTaskPosition = viewHolder.getAdapterPosition();
                                        Task swipedTask = taskList.get(swipedTaskPosition);

                                        Executor executor = Executors.newSingleThreadExecutor();
                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                MyDatabase.getDatabase(context).taskDao().deleteTask(swipedTask);
                                            }
                                        });
                                        taskList.remove(swipedTaskPosition);
                                        adapter.notifyItemRemoved(swipedTaskPosition);
                                    }
                                });
                itemTouchHelper.attachToRecyclerView(recyclerView);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }
    public void updateTaskRecView() {
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        if (recyclerView.getItemDecorationCount() < 1)
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        new HistoryFragment.LoadDataTask(getContext().getApplicationContext()).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        updaterRecViewList.updateTaskRecView();
    }



}
