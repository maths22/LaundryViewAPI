package com.maths22.laundryviewapi.data;

import java.io.Serializable;

/**
 * Created by Jacob on 1/21/2016.
 */
public class LaundryRoom implements Serializable {
    private String id;
    private String name;

    public LaundryRoom(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public LaundryRoom() {
        this.id = "";
        this.name = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
