package ca.umontreal.iro.guidedesmedicaments.rxnav;

import org.apache.http.client.HttpClient;

/**
 * The RxClass API is a web service for accessing drug classes and drug members for a number of
 * different drug class types. No license is needed to use the RxClass API.
 * <p/>
 *
 * @author Guillaume Poirier-Morency
 */
public class RxClass {

    private final HttpClient client;

    public RxClass(HttpClient client) {
        this.client = client;
    }
}
