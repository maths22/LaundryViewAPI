package com.maths22.laundryviewapi;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.maths22.laundryviewapi.data.Machine;
import com.maths22.laundryviewapi.data.RoomMachineStatus;
import com.maths22.laundryviewapi.data.Status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NotificationManager {
    private Table ddbTable;

    public NotificationManager() {
        AmazonDynamoDB client;
        if(System.getenv("dynamo-local") != null) {
            client = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration("http://ddblocal.docker:8000", "us-west-2"))
                    .build();
        } else {
            client = AmazonDynamoDBClientBuilder.standard()
                    .build();
        }
        DynamoDB db = new DynamoDB(client);
        ddbTable = db.getTable(System.getenv("notification_table"));

    }


    public void register(String machineId, String requesterId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 12);
        long expiry = cal.getTimeInMillis() / 1000L;
        Item item = new Item()
                .withPrimaryKey("RequesterId", requesterId,
                        "MachineId", machineId)
                .withNumber("TimeToExist", expiry);
        ddbTable.putItem(item);

    }

    public void unregister(String machineId, String requesterId) {
        DeleteItemSpec spec = new DeleteItemSpec()
                .withPrimaryKey(
                        "MachineId",
                        machineId,
                        "RequesterId",
                        requesterId);


        ddbTable.deleteItem(spec);
    }

    private static String getGoogleKey() {
        if(System.getenv("googlekey-local") != null) {
            byte[] encoded = new byte[0];
            try {
                encoded = Files.readAllBytes(Paths.get(System.getenv("googlekey-local")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String(encoded);
        }
        String secretName = System.getenv("firebase_secret_name");

        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .build();

        String secret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult;

        getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        secret = getSecretValueResult.getSecretString();
        return secret;
    }

    static boolean fbInitialized = false;

    private static void initFirebase() {
        if(fbInitialized) return;

        FirebaseOptions options;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream( getGoogleKey().getBytes() )))

//                .setDatabaseUrl("https://numeric-drummer-119720.firebaseio.com/")
                .build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        FirebaseApp.initializeApp(options);
        fbInitialized = true;
    }

    public static void main(String args[]) {
        new NotificationManager().handleRequest();
    }

    public void handleRequest() {
        initFirebase();

        Map<String, RoomMachineStatus> statuses = new HashMap<>();
        for(Item item : ddbTable.scan()) {
            try {
                String[] pieces = item.getString("MachineId").split("\\|");
                String lrName = pieces[0];
                String lrId = pieces[1];
                String machineId = pieces[2];
                RoomMachineStatus stat = statuses.get(lrId);
                if (stat == null) {
                    stat = new LaundryViewEndpoint().machineStatus(lrId);
                    statuses.put(lrId, stat);
                }
                String type = "WASHER";
                Optional<Machine> machineOpt = stat.getWashers().stream().filter((m) -> m.getId().equals(machineId)).findFirst();
                if (!machineOpt.isPresent()) {
                    type = "DRYER";
                    machineOpt = stat.getDryers().stream().filter((m) -> m.getId().equals(machineId)).findFirst();
                }
                if (!machineOpt.isPresent()) continue;

                Machine mac = machineOpt.get();
                System.out.println(mac.getId() + " " + mac.getStatus());
                if (mac.getStatus().equals(Status.AVAILBLE) || mac.getStatus().equals(Status.DONE)) {
                    Message message = Message.builder()
                            .setNotification(new Notification(
                                type.equals("WASHER") ? "Washing Machine Done" : "Dryer Done",
                                lrName + ": Machine #" + mac.getNumber()
                            ))
                            .setAndroidConfig(AndroidConfig.builder()
                                    .setNotification(AndroidNotification.builder()
                                            .setIcon("washing_machine")
                                            .setSound("default")
                                            .setColor("#000075")
                                            .build()).build())
                            // .setApnsConfig(ApnsConfig.builder()
                            //         .setAps(Aps.builder()
                            //                     .build())
                            //                 .build())
                            //                        .setToken(item.getString("RequesterId"))
                            //                        .build();
                            //
                            //                FirebaseMessaging.getInstance().send(message);
                            //
                            //                message = Message.builder()
                            .putData("completed", item.getString("MachineId"))
                            .setToken(item.getString("RequesterId"))
                            .build();

                    try {
                        FirebaseMessaging.getInstance().send(message);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }
                    ddbTable.deleteItem("RequesterId", item.getString("RequesterId"), "MachineId", item.getString("MachineId"));
                }
            } catch (Exception e) {
                // We want to keep processing the other alerts
                e.printStackTrace();
            }
        }
    }
}
