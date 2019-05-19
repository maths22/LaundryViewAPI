package com.maths22.laundryviewapi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.maths22.laundryviewapi.data.LaundryRoom;
import com.maths22.laundryviewapi.data.RoomMachineStatus;
import com.maths22.laundryviewapi.data.School;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by Jacob on 1/21/2016.
 */
public class LaundryViewEndpoint implements RequestStreamHandler {
    private ObjectMapper om;

    public LaundryViewEndpoint() {
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static class LvRequest {
        private String method;
        private Map<String, Object> args;

        public String getMethod() {
            return method;
        }

        public Map<String, Object> getArgs() {
            return args;
        }
    }

    private static class ResponseWrapper {
        private String body;
        private int statusCode;

        public ResponseWrapper(int statusCode, String body) {
            this.body = body;
            this.statusCode = statusCode;
        }

        public String getBody() {
            return body;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static void main(String args[]) throws IOException {
        new LaundryViewEndpoint().handleRequest(System.in, System.out, null);
    }

    public void handleRequest(InputStream is, OutputStream os, Context ctx) throws IOException {
//        LambdaLogger logger = ctx.getLogger();

        HashMap req = om.readValue(is, HashMap.class);
        if("Scheduled Event".equals(req.get("detail-type"))) {
            new NotificationManager().handleRequest();
            return;
        }
        Unirest.setTimeouts(3000, 10000);
        LvRequest request = om.readValue((String)req.get("body"), LvRequest.class);
        Object ret = null;
        switch(request.getMethod()) {
            case "findSchools":
                ret = findSchools((String) request.getArgs().get("name"));
                break;
            case "findLaundryRooms":
                ret = findLaundryRooms((String) request.getArgs().get("schoolId"));
                break;
            case "machineStatus":
                ret = machineStatus((String) request.getArgs().get("roomId"));
                break;

            case "registerMachine":
                ret = registerNotification(
                        (String) request.getArgs().get("machineId"),
                        (String) request.getArgs().get("requesterId")
                      );
                break;

            case "unregisterMachine":
                ret = unregisterNotification(
                        (String) request.getArgs().get("machineId"),
                        (String) request.getArgs().get("requesterId")
                        );
                break;
        }

        ResponseWrapper res = new ResponseWrapper(HTTP_OK, om.writeValueAsString(ret));
        om.writeValue(os, res);
        os.close();
    }

    public List<School> findSchools(String name) {
        DynamoCache cache = new DynamoCache();
        cache.setTtl(TimeUnit.DAYS.toSeconds(1));

        TimeStampedObject<List<School>> entry = (TimeStampedObject<List<School>>) cache.get("findSchools?name=" + name);

        if(entry != null  && Days.daysBetween(entry.getTime(), new DateTime()).isLessThan(Days.days(1))) {
            return entry.getObject();
        }
        List<School> ret = new FindSchools().lookup(name);
        cache.put("findSchools?name=" + name,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }
    public List<LaundryRoom> findLaundryRooms(String schoolId) {
        DynamoCache cache = new DynamoCache();
        cache.setTtl(TimeUnit.DAYS.toSeconds(1));

        TimeStampedObject<List<LaundryRoom>> entry = (TimeStampedObject<List<LaundryRoom>>) cache.get("findLaundryRooms?schoolId=" + schoolId);

        if(entry != null && Days.daysBetween(entry.getTime(), new DateTime()).isLessThan(Days.days(1))) {
            return entry.getObject();
        }
        List<LaundryRoom> ret = new FindLaundryRooms().lookup(schoolId);
        cache.put("findLaundryRooms?schoolId=" + schoolId,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }
    public RoomMachineStatus machineStatus(String roomId) {
        DynamoCache cache = new DynamoCache();
        cache.setTtl(TimeUnit.MINUTES.toSeconds(1));

        TimeStampedObject<RoomMachineStatus> entry = (TimeStampedObject<RoomMachineStatus>) cache.get("machineStatus?roomId=" + roomId);

        if(entry != null  && Minutes.minutesBetween(entry.getTime(), new DateTime()).isLessThan(Minutes.minutes(1))) {
            return entry.getObject();
        }
        RoomMachineStatus ret = new MachineStatus().lookup(roomId);
        cache.put("machineStatus?roomId=" + roomId,
                new TimeStampedObject<>(ret, new DateTime()));
        return ret;
    }

    public String registerNotification(String machineId, String requesterId) {
        new NotificationManager().register(machineId, requesterId);
        return "ok";
    }

    public String unregisterNotification(String machineId, String requesterId) {
        new NotificationManager().unregister(machineId, requesterId);
        return "ok";
    }
}
