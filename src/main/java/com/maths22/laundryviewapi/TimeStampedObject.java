package com.maths22.laundryviewapi;

import com.sun.org.apache.xml.internal.utils.SerializableLocatorImpl;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jacob on 2/1/2016.
 */
public class TimeStampedObject<T> implements Serializable {
    private T object;
    private DateTime time;

    public TimeStampedObject(T object, DateTime time) {
        this.object = object;
        this.time = time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public DateTime getTime() {
        return time;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
