package com.maths22.laundryviewapi;

import com.maths22.laundryviewapi.data.School;

import java.util.*;
import java.util.concurrent.*;

/**
 * Root resource (exposed at "myresource" path)
 */
public class FindSchools {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @param name Search key in school name (min 3 characters)
     * @return String that will be returned as a text/plain response.
     */
    public List<School> lookup(String name) {

        ExecutorService executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        FutureTask<List<School>> nameSearch = new FutureTask<>(new SchoolSearch.SearchByName(name));
        FutureTask<List<School>> sIdSearch = new FutureTask<>(new SchoolSearch.SearchBySId(name));
        FutureTask<List<School>> rIdSearch = new FutureTask<>(new SchoolSearch.SearchByRId(name));
        FutureTask<List<School>> linkSearch = new FutureTask<>(new SchoolSearch.SearchByLink(name));

        executor.execute(nameSearch);
        executor.execute(linkSearch);

        if(name.matches("^\\d+$")) {
            executor.execute(sIdSearch);
            executor.execute(rIdSearch);
        }

        List<School> nameSearchResults = new ArrayList<>();
        List<School> sIdSearchResults = new ArrayList<>();
        List<School> rIdSearchResults = new ArrayList<>();
        List<School> linkSearchResults = new ArrayList<>();
        try {
            nameSearchResults = nameSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try {
            linkSearchResults = linkSearch.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if(name.matches("^\\d+$")) {
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
        }
        executor.shutdown();

        List<School> ret = new ArrayList<>();

        if (nameSearchResults != null) {
           ret.addAll(nameSearchResults);
        }
        if (sIdSearchResults != null) {
            ret.addAll(sIdSearchResults);
        }
        if (rIdSearchResults != null) {
            ret.addAll(rIdSearchResults);
        }
        if (linkSearchResults != null) {
            ret.addAll(linkSearchResults);
        }

        return ret;
    }

    private boolean isValidLookupName(String name) {
        return name.length() >= 3 || name.matches("^\\d+$");
    }
}
