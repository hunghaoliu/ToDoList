package com.henry.todolist.database;

/**
 * Created by henry on 17/1/15.
 */
public class TaskModel {
    private String datetime;
    private String tasks;
    private boolean isFinish;
    private int isLocal;  // 0: sheetsu synced, 1: need to update sheetsu, 2: need to insert sheetsu
    private String uuid;

    public TaskModel(String datetime, String tasks, boolean isFinish, int isLocal, String uuid) {
        this.datetime = datetime;
        this.tasks = tasks;
        this.isFinish = isFinish;
        this.isLocal = isLocal;
        this.uuid = uuid;
    }

    /**
     *
     * @return
     *     The datetime
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     *
     * @param datetime
     *     The datetime
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
    /**
     *
     * @return
     *     The tasks
     */
    public String getTasks() {
        return tasks;
    }

    /**
     *
     * @param tasks
     *     The tasks
     */
    public void setTasks(String tasks) {
        this.tasks = tasks;
    }    /**
     *
     * @return
     *     The isFinish
     */
    public boolean getIsFinish() {
        return isFinish;
    }

    /**
     *
     * @param isFinish
     *     The isFinish
     */
    public void setIsFinish(boolean isFinish) {
        this.isFinish = isFinish;
    }
    /**
     * @return
     *     The isLocal
     */
    public int getIsLocal() {
        return isLocal;
    }

    /**
     *
     * @param isLocal
     *     The isLocal
     */
    public void setIsLocal(int isLocal) {
        this.isLocal = isLocal;
    }

    /**
     *
     * @return
     *     The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @param uuid
     *     The uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
