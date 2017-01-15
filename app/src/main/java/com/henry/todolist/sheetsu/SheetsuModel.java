package com.henry.todolist.sheetsu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by henry on 17/1/14.
 */


public class SheetsuModel {
    @SerializedName("datetime")
    @Expose
    private String datetime;
    @SerializedName("tasks")
    @Expose
    private String tasks;
    @SerializedName("IsFinish")
    @Expose
    private String isFinish;
    @SerializedName("uuid")
    @Expose
    private String uuid;

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
    public String getIsFinish() {
        return isFinish;
    }

    /**
     *
     * @param isFinish
     *     The isFinish
     */
    public void setIsFinish(String isFinish) {
        this.isFinish = isFinish;
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
