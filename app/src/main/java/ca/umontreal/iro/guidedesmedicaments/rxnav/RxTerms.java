package ca.umontreal.iro.guidedesmedicaments.rxnav;

import org.apache.http.client.HttpClient;

/**
 * Created by guillaume on 15-03-16.
 */
public class RxTerms {

    private final HttpClient client;

    public RxTerms(HttpClient client) {
        this.client = client;
    }
}
