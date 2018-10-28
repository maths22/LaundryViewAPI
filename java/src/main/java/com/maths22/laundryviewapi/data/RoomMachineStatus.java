package com.maths22.laundryviewapi.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jacob on 1/21/2016.
 */
public class RoomMachineStatus implements Serializable {
    private List<Machine> washers;
    private List<Machine> dryers;

    public RoomMachineStatus() {
        washers = new ArrayList<>();
        dryers = new ArrayList<>();
    }

    public List<Machine> getWashers() {
        return washers;
    }

    public void setWashers(List<Machine> washers) {
        this.washers = washers;
    }

    public List<Machine> getDryers() {
        return dryers;
    }

    public void setDryers(List<Machine> dryers) {
        this.dryers = dryers;
    }

}
