package com.maths22.laundryviewapi.data;

import java.io.Serializable;

/**
 * Created by Jacob on 1/21/2016.
 */
public class Machine implements Serializable {
    private String id;
    private String number;
    private Status status;
    private int timeRemaining;

    public Machine(String id, String number, Status status) {
        this();
        this.id = id;
        this.number = number;
        this.status = status;
    }

    public Machine() {
        this.id = "";
        this.number = "";
        this.status = Status.UNKNOWN;
        this.timeRemaining = -1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String name) {
        this.number = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
