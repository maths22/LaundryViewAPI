package com.maths22.laundryviewapi;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.IOException;
import java.util.Calendar;

public class DynamoCache {
    private Table ddbTable = null;
    private long ttlInSeconds;
    private boolean enabled;

    ObjectMapper om;

    public DynamoCache() {
        enabled = System.getenv("cache_table") != null;
        if(!enabled) return;

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
        ddbTable = db.getTable(System.getenv("cache_table"));

        om = new ObjectMapper();
        om.registerModule(new JodaModule());
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public void setTtl(long seconds) {
        this.ttlInSeconds = seconds;
    }



    public Object get(String key) {
        if(!enabled) return null;
        Item item = ddbTable.getItem("Key", key);
        if(item == null) {
            return null;
        }
        //TODO
        try {
            return om.readValue(item.getJSON("Data"), Object.class);
        } catch (IOException e) {
            return null;
        }
    }

    public void put(String key, Object data) {
        if(!enabled) return;
        long now = Calendar.getInstance().getTimeInMillis() / 1000L;
        long expiry = now + ttlInSeconds;
        try {
            Item item = new Item()
                    .withPrimaryKey("Key", key)
                    .withNumber("TimeToExist", expiry)
                    .withJSON("Data", om.writeValueAsString(data));
            ddbTable.putItem(item);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
