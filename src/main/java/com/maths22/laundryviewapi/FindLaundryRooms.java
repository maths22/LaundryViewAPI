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
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
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

        ClientConfig cc = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, false);
        Client client = ClientBuilder.newClient(cc);

        WebTarget target = client.target("https://laundryview.com/").path("api/c_room").queryParam("loc", schoolId);

        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            try {
                String text = response.readEntity(String.class);
                JsonReaderFactory readerFactory = Json.createReaderFactory(null);
                JsonObject obj = readerFactory.createReader(new StringReader(text)).readObject();


                JsonBuilderFactory factory = Json.createBuilderFactory(null);
                JsonArrayBuilder results = factory.createArrayBuilder();
                for (JsonValue item : obj.getJsonArray("room_data")) {
                    if(item.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                        JsonObject lrObj = (JsonObject) item;
                        String id = lrObj.getString("laundry_room_location");
                        String name = WordUtils.capitalizeFully(lrObj.getString("laundry_room_name"), ' ', '-');

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
            throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
        }
        //cache.put("findLaundryRooms?schoolId=" + schoolId, ret);
        return ret;
    }
}
