package com.maths22.laundryviewapi;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.maths22.laundryviewapi.data.Machine;
import com.maths22.laundryviewapi.data.RoomMachineStatus;
import com.maths22.laundryviewapi.data.Status;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;


/**
 * Root resource (exposed at "myresource" path)
 */
public class MachineStatus {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param roomId School to list rooms of
     * @return String that will be returned as a text/plain response.
     */
    public RoomMachineStatus lookup(String roomId) {
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.get("https://laundryview.com/api/currentRoomData")
                    .queryString("location", roomId)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();

            throw new RuntimeException("LaundryView Request Failed");
        }

        RoomMachineStatus ret = new RoomMachineStatus();

        if (response.getStatus() == 200) {
            try {
                JsonNode node = response.getBody();

                JSONObject rootObj = node.getObject();


                for (Object item : rootObj.getJSONArray("objects")) {
                    JSONObject obj = (JSONObject) item;

                    if(obj.has("appliance_type")) {
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

                    if(obj.has("appliance_type2") || obj.getString("type").equals("washNdry") || obj.getString("type").equals("dblDry")) {
                        String itemType = obj.has("appliance_type2") ? obj.getString("appliance_type2") : null;
                        // It must be that obj.getString("type").equals("washNdry")
                        if(itemType == null) {
                            itemType =  obj.getString("type").equals("washNdry") ? "W" : "D";
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
            throw new RuntimeException("LaundryView Request Failed");
        }

        return ret;
    }
}
