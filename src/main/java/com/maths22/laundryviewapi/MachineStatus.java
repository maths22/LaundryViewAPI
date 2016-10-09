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
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        WebTarget target = client.target("http://m.laundryview.com")
                .path("submitFunctions.php")
                .queryParam("monitor", "true")
                .queryParam("lr", roomId);

        Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

        RoomMachineStatus ret = new RoomMachineStatus();

        if (response.getStatus() == 200) {
            try {
                String text = response.readEntity(String.class);
                Document doc = Jsoup.parse(text);

                Elements items = doc.body().children();

                JsonBuilderFactory factory = Json.createBuilderFactory(null);
                JsonObjectBuilder objBuilder = factory.createObjectBuilder();
                List<Machine> currentList = ret.getWashers();

                for (Element item : items) {
                    if (item.id().equals("washer")) {
                    } else if (item.id().equals("dryer")) {
                        currentList = ret.getDryers();
                    } else {
                        String id = item.child(0).attr("id");
                        String number = item.child(0).ownText().replace("\u00A0", "");
                        String status = item.child(0).child(2).ownText();
                        Status retStatus;
                        if (status.equals("Avail")) {
                            retStatus = Status.AVAILBLE;
                        } else if (status.endsWith("mins left")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Idle")) {
                            retStatus = Status.DONE;
                        } else if (status.equals("Extended Cycle")) {
                            retStatus = Status.IN_USE;
                        } else if (status.equals("Out of service")) {
                            retStatus = Status.OUT_OF_SERVICE;
                        } else {
                            retStatus = Status.UNKNOWN;
                            //TODO add logger
                        }
                        String name = WordUtils.capitalizeFully(item.child(0).ownText(), ' ', '-');

                        Machine machine = new Machine(id, number, retStatus);

                        if (retStatus.equals(Status.IN_USE) && !status.equals("Extended Cycle")) {
                            machine.setTimeRemaining(Integer.parseInt(status.split("\\s+")[0]));
                        }
                        currentList.add(machine);
                    }
                }
            } catch (Exception ex) {
                //TODO catch something here
            }
        } else {
            throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
        }

        return ret;
    }
}
