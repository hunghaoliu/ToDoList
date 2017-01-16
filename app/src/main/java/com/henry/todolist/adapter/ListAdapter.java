package com.henry.todolist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.henry.todolist.R;
import com.henry.todolist.database.TaskModel;
import com.henry.todolist.sheetsu.SheetsuModel;
import com.henry.todolist.util.Constants;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by henry on 17/1/15.
 */
public class ListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<TaskModel> mList = new ArrayList<>();

    public ListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(TaskModel item) {
        if (item.getIsLocal() == Constants.SHEETSU_SYNC_NEED_DELETE)
            return;

        for (TaskModel task : mList) {
           if (task.getUuid().equals(item.getUuid())) {
               if (task.getIsLocal() != item.getIsLocal()) {
                   task.setIsLocal(item.getIsLocal());
               }

               return;
           }
        }

        mList.add(item);
        notifyDataSetChanged();
    }

    public boolean deleteItem(String uuid) {
        boolean result = false;

        for (TaskModel task : mList) {
            if (task.getUuid().equals(uuid)) {
                mList.remove(task);
                notifyDataSetChanged();
                return true;
            }
        }

        return result;
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void checked(int position) {
        mList.get(position).setIsFinish(!mList.get(position).getIsFinish());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_todo, null);
            holder = new ViewHolder();
            holder.textDate = (TextView) convertView.findViewById(R.id.item_date);
            holder.textTask = (TextView) convertView.findViewById(R.id.item_task);
            holder.imageFinish = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textDate.setText(mList.get(i).getDatetime());
        holder.textTask.setText(mList.get(i).getTasks());
        if (mList.get(i).getIsFinish()) {
            holder.imageFinish.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            holder.imageFinish.setImageResource(android.R.drawable.checkbox_off_background);
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView textDate;
        public TextView textTask;
        public ImageView imageFinish;
    }
}
