package com.maths22.laundryviewapi;

import org.apache.commons.lang3.text.WordUtils;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("findSchools/{name}")
public class FindSchools {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param name Search key in school name (min 3 characters)
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String lookup(@PathParam("name") String name) {
        if (name.length() < 3) {
            throw new WebApplicationException("Invalid School Name (minimum 3 characters)", Response.Status.BAD_REQUEST);
        }

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://m.laundryview.com").path("submitFunctions.php").queryParam("q", name);

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();

        JsonStructure list;

        if (response.getStatus() == 200) {
            String text = response.readEntity(String.class);
            try (JsonReader jsonReader = Json.createReader(new StringReader(text))) {
                list = jsonReader.read();
            } catch (JsonException ex) {
                throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new WebApplicationException("LaundryView Request Failed", Response.Status.INTERNAL_SERVER_ERROR);
        }

        if (list instanceof JsonObject) {
            return "[]";
        } else {
            JsonBuilderFactory factory = Json.createBuilderFactory(null);
            JsonArrayBuilder ret = factory.createArrayBuilder();
            JsonArray schools = (JsonArray) list;
            for (JsonValue schoolV : schools) {
                JsonObject school = (JsonObject) schoolV;
                ret.add(factory.createObjectBuilder()
                        .add("id", school.getString("school_desc_key"))
                        .add("name", WordUtils.capitalizeFully(school.getString("property_name"), ' ', '-')));
            }
            return ret.build().toString();
        }
    }
}
