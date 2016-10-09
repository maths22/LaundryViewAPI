package com.maths22.laundryviewapi;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.maths22.laundryviewapi.data.LaundryRoom;
import org.apache.commons.lang3.text.WordUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.cache.*;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("findLaundryRooms/{schoolId}")
public class FindLaundryRooms {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param schoolId School to list rooms of
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<LaundryRoom> lookup(@PathParam("schoolId") String schoolId) {

        List<LaundryRoom> ret = new ArrayList<>();
        if (schoolId.matches("^r_.*$")) {
            String roomId = schoolId.substring(2);
            String name = "Default";
            ret.add(new LaundryRoom(roomId, name));
        } else {

            ClientConfig cc = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, false);
            Client client = ClientBuilder.newClient(cc);

            WebTarget target = client.target("http://m.laundryview.com").path("lvs.php").queryParam("s", schoolId);

            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            if (response.getStatus() == 200) {
                try {
                    String text = response.readEntity(String.class);
                    Document doc = Jsoup.parse(text);
                    Elements items = doc.getElementById("rooms").children();

                    JsonBuilderFactory factory = Json.createBuilderFactory(null);
                    JsonArrayBuilder results = factory.createArrayBuilder();
                    for (Element item : items) {
                        if (item.children().size() > 0) {
                            String id = item.child(0).attr("id");
                            String name = WordUtils.capitalizeFully(item.child(0).ownText(), ' ', '-');

                            ret.add(new LaundryRoom(id, name));
                            results.add(factory.createObjectBuilder()
                                    .add("id", id)
                                    .add("name", name));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (response.getStatus() == 302) {
                JsonBuilderFactory factory = Json.createBuilderFactory(null);
                JsonArrayBuilder results = factory.createArrayBuilder();
                String id = response.getHeaderString("Location").split("=")[1];
                String name = "Default";

                ret.add(new LaundryRoom(id, name));
                results.add(factory.createObjectBuilder()
                        .add("id", id)
                        .add("name", name));
            } else {
                throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
        //cache.put("findLaundryRooms?schoolId=" + schoolId, ret);
        return ret;
    }
}
