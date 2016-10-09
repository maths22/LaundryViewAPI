package com.maths22.laundryviewapi;

import com.maths22.laundryviewapi.data.School;
import org.apache.commons.lang3.text.WordUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.json.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Jacob on 1/17/2016.
 */
public class SchoolSearch {
    public static class SearchByName implements Callable<List<School>> {
        private final String name;

        public SearchByName(String name) {
            this.name = name;
        }

        @Override
        public List<School> call() throws Exception {
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

            List<School> ret = new ArrayList<>();

            if (list instanceof JsonObject) {
                return ret;
            } else {
                JsonArray schools = (JsonArray) list;
                for (JsonValue schoolV : schools) {
                    JsonObject school = (JsonObject) schoolV;
                    ret.add(new School(school.getString("school_desc_key"),
                            WordUtils.capitalizeFully(school.getString("property_name"), ' ', '-')));
                }
                return ret;
            }
        }
    }

    public static class SearchBySId implements Callable<List<School>> {
        private final String sId;

        public SearchBySId(String sId) {
            this.sId = sId;
        }

        @Override
        public List<School> call() throws Exception {
            ClientConfig cc = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, false);
            Client client = ClientBuilder.newClient(cc);

            WebTarget target = client.target("http://m.laundryview.com").path("lvs.php").queryParam("s", sId);

            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            List<School> ret = new ArrayList<>();

            if (response.getStatus() == 200) {
                try {
                    String text = response.readEntity(String.class);
                    Document doc = Jsoup.parse(text);
                    Elements items = doc.getElementById("rooms").children();

                    if (items.size() > 0) {
                        String name = items.get(0).text();
                        ret.add(new School(sId, WordUtils.capitalizeFully(name)));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (response.getStatus() == 302) {
                ret.add(new School(sId, "School #" + sId));
            }
            return ret;
        }
    }

    public static class SearchByRId implements Callable<List<School>> {
        private final String rId;

        public SearchByRId(String rId) {
            this.rId = rId;
        }

        @Override
        public List<School> call() throws Exception {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://m.laundryview.com")
                    .path("submitFunctions.php")
                    .queryParam("monitor", "true")
                    .queryParam("lr", rId);

            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            List<School> ret = new ArrayList<>();

            if (response.getStatus() == 200) {
                try {
                    String text = response.readEntity(String.class);
                    JsonObject error = Json.createReader(new StringReader(text)).readObject();
                } catch (Exception ex) {
                    //The server only returns JSON on errors!
                    ret.add(new School("r_" + rId, "Laundry room #" + rId));
                }
            }
            return ret;
        }
    }

    public static class SearchByLink implements Callable<List<School>> {
        private final String link;

        public SearchByLink(String link) {
            this.link = link;
        }

        @Override
        public List<School> call() throws Exception {
            ClientConfig cc = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, false);
            Client client = ClientBuilder.newClient(cc);
            WebTarget target = client.target("http://m.laundryview.com")
                    .path(link);

            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            List<School> ret = new ArrayList<>();
            if (response.getStatus() == 302) {
                String id = response.getHeaderString("Location").split("=")[1];
                ret.add(new School(id, link));
            }
            return ret;
        }
    }
}
