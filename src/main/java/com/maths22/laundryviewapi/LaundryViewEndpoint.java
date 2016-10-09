package com.maths22.laundryviewapi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.Days;
import com.google.appengine.repackaged.org.joda.time.Minutes;
import com.maths22.laundryviewapi.data.LaundryRoom;
import com.maths22.laundryviewapi.data.RoomMachineStatus;
import com.maths22.laundryviewapi.data.School;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jacob on 1/21/2016.
 */
@Api(
        name = "laundryView",
        version = "v1"
)
public class LaundryViewEndpoint {
    public List<School> findSchools(@Named("name") String name) {
        Cache cache = null;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            Map properties = new HashMap<>();
            properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.DAYS.toSeconds(1));
            cache = cacheFactory.createCache(properties);
        } catch (CacheException e) {
            // TODO catch exception
        }

        if(cache != null) {
            TimeStampedObject<List<School>> entry = (TimeStampedObject<List<School>>) cache.get("findSchools?name=" + name);

            if(entry != null  && Days.daysBetween(entry.getTime(), new DateTime()).isLessThan(Days.days(1))) {
                return entry.getObject();
            }
        }
        List<School> ret = new FindSchools().lookup(name);
        cache.put("findSchools?name=" + name,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }
    public List<LaundryRoom> findLaundryRooms(@Named("schoolId") String schoolId) {
        Cache cache = null;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            Map properties = new HashMap<>();
            properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.DAYS.toSeconds(1));
            cache = cacheFactory.createCache(properties);
        } catch (CacheException e) {
            // TODO catch exception
        }

        if(cache != null) {
            TimeStampedObject<List<LaundryRoom>> entry = (TimeStampedObject<List<LaundryRoom>>) cache.get("findLaundryRooms?schoolId=" + schoolId);

            if(entry != null && Days.daysBetween(entry.getTime(), new DateTime()).isLessThan(Days.days(1))) {
                return entry.getObject();
            }
        }
        List<LaundryRoom> ret = new FindLaundryRooms().lookup(schoolId);
        cache.put("findLaundryRooms?schoolId=" + schoolId,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }
    public RoomMachineStatus machineStatus(@Named("roomId") String roomId) {
        Cache cache = null;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            Map properties = new HashMap<>();
            properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.MINUTES.toSeconds(1));
            cache = cacheFactory.createCache(properties);
        } catch (CacheException e) {
            // TODO catch exception
        }

        if(cache != null) {
            TimeStampedObject<RoomMachineStatus> entry = (TimeStampedObject<RoomMachineStatus>) cache.get("machineStatus?roomId=" + roomId);

            if(entry != null  && Minutes.minutesBetween(entry.getTime(), new DateTime()).isLessThan(Minutes.minutes(1))) {
                return entry.getObject();
            }
        }
        RoomMachineStatus ret = new MachineStatus().lookup(roomId);
        cache.put("machineStatus?roomId=" + roomId,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }
}
