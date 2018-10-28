package com.maths22.laundryviewapi;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.maths22.laundryviewapi.data.LaundryRoom;
import org.apache.commons.text.WordUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Root resource (exposed at "myresource" path)
 */
public class FindLaundryRooms {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param schoolId School to list rooms of
     * @return String that will be returned as a text/plain response.
     */
    public List<LaundryRoom> lookup(String schoolId) {

        List<LaundryRoom> ret = new ArrayList<>();

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get("https://laundryview.com/api/c_room")
                    .queryString("loc", schoolId)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new RuntimeException("LaundryView Request Failed");
        }


        if (response.getStatus() == 200) {
            try {
                JSONObject obj = response.getBody().getObject();

                for (Object item : obj.getJSONArray("room_data")) {
                    if(item instanceof JSONObject) {
                        JSONObject lrObj = (JSONObject) item;
                        String id = lrObj.getString("laundry_room_location");
                        String name = WordUtils.capitalizeFully(lrObj.getString("laundry_room_name"), ' ', '-');

                        ret.add(new LaundryRoom(id, name));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (response.getStatus() == 302) {
            throw new RuntimeException("LaundryView Request Failed");
        }
        return ret;
    }
}
