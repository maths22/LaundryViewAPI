package com.maths22.laundryviewapi;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.maths22.laundryviewapi.data.Machine;
import com.maths22.laundryviewapi.data.RoomMachineStatus;
import com.maths22.laundryviewapi.data.Status;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("machineStatus/{roomId}")
public class MachineStatus {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param roomId School to list rooms of
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RoomMachineStatus lookup(@PathParam("roomId") String roomId) {


        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://laundryview.com")
                .path("api/currentRoomData")
                .queryParam("location", roomId);

        Response response = target.request(MediaType.APPLICATION_JSON).get();

        RoomMachineStatus ret = new RoomMachineStatus();

        if (response.getStatus() == 200) {
            try {
                String text = response.readEntity(String.class);
                JsonReaderFactory readerFactory = Json.createReaderFactory(null);
                JsonObject rootObj = readerFactory.createReader(new StringReader(text)).readObject();


                for (JsonValue item : rootObj.getJsonArray("objects")) {
                    JsonObject obj = (JsonObject) item;

                    if(obj.containsKey("appliance_type")) {
                        String itemType = obj.getString("appliance_type");
                        String id = obj.getString("appliance_desc_key");
                        String number = obj.getString("appliance_desc");
                        String status = obj.getString("time_left_lite");
                        Status retStatus;
                        if (status.startsWith("Avail")) {
                            retStatus = Status.AVAILBLE;
                        } else if (status.endsWith("min remaining")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Idle")) {
                            retStatus = Status.DONE;
                        } else if (status.equals("Ext. Cycle")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Unavailable")) {
                            retStatus = Status.OUT_OF_SERVICE;
                        } else {
                            retStatus = Status.UNKNOWN;
                            //TODO add logger
                        }

                        Machine machine = new Machine(id, number, retStatus);

                        if (retStatus.equals(Status.IN_USE) && !status.equals("Ext. Cycle")) {
                            machine.setTimeRemaining(obj.getInt("time_remaining"));
                        }
                        if(itemType.equals("D")) {
                            ret.getDryers().add(machine);
                        } else {
                            ret.getWashers().add(machine);
                        }
                    }

                    if(obj.containsKey("appliance_type2") || obj.getString("type").equals("washNdry")) {
                        String itemType = obj.containsKey("appliance_type2") ? obj.getString("appliance_type2") : null;
                        // It must be that obj.getString("type").equals("washNdry")
                        if(itemType == null) {
                            itemType = "W";
                        }
                        String id = obj.getString("appliance_desc_key2");
                        String number = obj.getString("appliance_desc2");
                        String status = obj.getString("time_left_lite2");
                        Status retStatus;
                        if (status.startsWith("Avail")) {
                            retStatus = Status.AVAILBLE;
                        } else if (status.endsWith("min remaining")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Idle")) {
                            retStatus = Status.DONE;
                        } else if (status.equals("Ext. Cycle")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Unavailable")) {
                            retStatus = Status.OUT_OF_SERVICE;
                        } else {
                            retStatus = Status.UNKNOWN;
                            //TODO add logger
                        }

                        Machine machine = new Machine(id, number, retStatus);

                        if (retStatus.equals(Status.IN_USE) && !status.equals("Ext. Cycle")) {
                            machine.setTimeRemaining(obj.getInt("time_remaining2"));
                        }
                        if(itemType.equals("D")) {
                            ret.getDryers().add(machine);
                        } else {
                            ret.getWashers().add(machine);
                        }
                    }


                }
            } catch (Exception ex) {
                //TODO catch something here
                ex.printStackTrace();
            }
        } else {
            throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
        }

        return ret;
    }
}
