package com.maths22.laundryviewapi;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
        if (!isValidLookupName(name)) {
            throw new WebApplicationException("Invalid School Name (minimum 3 characters)", Response.Status.BAD_REQUEST);
        }

        ExecutorService executor = Executors.newCachedThreadPool();

        FutureTask<JsonArray> nameSearch = new FutureTask<>(new SchoolSearch.SearchByName(name));
        FutureTask<JsonArray> sIdSearch = new FutureTask<>(new SchoolSearch.SearchBySId(name));
        FutureTask<JsonArray> rIdSearch = new FutureTask<>(new SchoolSearch.SearchByRId(name));
        FutureTask<JsonArray> linkSearch = new FutureTask<>(new SchoolSearch.SearchByLink(name));

        executor.execute(nameSearch);
        executor.execute(sIdSearch);
        executor.execute(rIdSearch);
        executor.execute(linkSearch);

        JsonArray nameSearchResults = null;
        JsonArray sIdSearchResults = null;
        JsonArray rIdSearchResults = null;
        JsonArray linkSearchResults = null;
        try {
            nameSearchResults = nameSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            sIdSearchResults = sIdSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            rIdSearchResults = rIdSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            linkSearchResults = linkSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        JsonArrayBuilder ret = factory.createArrayBuilder();

        if (nameSearchResults != null) {
            for (JsonValue obj : nameSearchResults) {
                ret.add(obj);
            }
        }
        if (sIdSearchResults != null) {
            for (JsonValue obj : sIdSearchResults) {
                ret.add(obj);
            }
        }
        if (rIdSearchResults != null) {
            for (JsonValue obj : rIdSearchResults) {
                ret.add(obj);
            }
        }
        if (linkSearchResults != null) {
            for (JsonValue obj : linkSearchResults) {
                ret.add(obj);
            }
        }

        return ret.build().toString();
    }

    private boolean isValidLookupName(String name) {
        return name.length() >= 3 || name.matches("^\\d+$");
    }
}
