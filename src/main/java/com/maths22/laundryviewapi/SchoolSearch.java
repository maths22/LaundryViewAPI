package com.maths22.laundryviewapi;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.maths22.laundryviewapi.data.School;
import org.apache.commons.text.WordUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        public List<School> call() {
            HttpResponse<JsonNode> response;
            try {
                response = Unirest.get("https://laundryview.com/api/c_locations")
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
                throw new RuntimeException("LaundryView Request Failed");
            }

            JSONArray list;

            if (response.getStatus() == 200) {
                list = response.getBody().getArray();
            } else {
                throw new RuntimeException("LaundryView Request Failed");
            }

            Iterator<Object> schools = list.iterator();
            Stream<Object> schoolsStream = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(schools, Spliterator.ORDERED),
                    false
            );
            List<School> allSchools = schoolsStream
                    .map((v) -> {
                        JSONObject obj = (JSONObject) v;
                        return new School(obj.getString("school_desc_key"),
                                WordUtils.capitalizeFully(obj.getString("school_name"), ' ', '-'));
                    })
                    .filter((s) -> s.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());

            List<School> startSchools = allSchools.stream()
                    .filter((s) -> s.getName().toLowerCase().startsWith(name.toLowerCase()))
                    .sorted(Comparator.comparing(School::getName))
                    .collect(Collectors.toList());
            List<School> endSchools = allSchools.stream()
                    .filter((s) -> !s.getName().toLowerCase().startsWith(name.toLowerCase()))
                    .sorted(Comparator.comparing(School::getName))
                    .collect(Collectors.toList());
            List<School> ret = new ArrayList<>();
            ret.addAll(startSchools);
            ret.addAll(endSchools);
            return ret;
        }
    }

    public static class SearchBySId implements Callable<List<School>> {
        private final String sId;

        public SearchBySId(String sId) {
            this.sId = sId;
        }

        @Override
        public List<School> call() throws Exception {
            // TODO support this method?
//            ClientConfig cc = new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, false);
//            Client client = ClientBuilder.newClient(cc);
//
//            WebTarget target = client.target("http://m.laundryview.com").path("lvs.php").queryParam("s", sId);
//
//            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            List<School> ret = new ArrayList<>();

//            if (response.getStatus() == 200) {
//                try {
//                    String text = response.readEntity(String.class);
//                    Document doc = Jsoup.parse(text);
//                    Elements items = doc.getElementById("rooms").children();
//
//                    if (items.size() > 0) {
//                        String name = items.get(0).text();
//                        ret.add(new School(sId, WordUtils.capitalizeFully(name)));
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            } else if (response.getStatus() == 302) {
//                ret.add(new School(sId, "School #" + sId));
//            }
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
            // TODO support this method?
//            Client client = ClientBuilder.newClient();

//            WebTarget target = client.target("http://m.laundryview.com")
//                    .path("submitFunctions.php")
//                    .queryParam("monitor", "true")
//                    .queryParam("lr", rId);
//
//            Response response = target.request(MediaType.TEXT_HTML_TYPE).get();

            List<School> ret = new ArrayList<>();

//            if (response.getStatus() == 200) {
//                try {
//                    String text = response.readEntity(String.class);
//                    JsonObject error = Json.createReader(new StringReader(text)).readObject();
//                } catch (Exception ex) {
//                    //The server only returns JSON on errors!
//                    ret.add(new School("r_" + rId, "Laundry room #" + rId));
//                }
//            }
            return ret;
        }
    }

    public static class SearchByLink implements Callable<List<School>> {
        private final String link;

        private CloseableHttpClient noRedirectClient = HttpClients.custom()
                .disableRedirectHandling()
                .build();

        public SearchByLink(String link) {
            this.link = link;
        }

        @Override
        public List<School> call() throws Exception {
            org.apache.http.HttpResponse response;
            HttpGet request = new HttpGet("https://www.laundryview.com/" + link);
            try {
                response = noRedirectClient.execute(request);
            } catch (IOException e) {
                throw new RuntimeException("LaundryView Request Failed");
            }


            List<School> ret = new ArrayList<>();
            if (response.getStatusLine().getStatusCode() == 302) {
                String id = new URI(response.getFirstHeader("Location").getValue()).getQuery().split("=")[1];
                ret.add(new School(id, link));
            }
            return ret;
        }
    }
}
