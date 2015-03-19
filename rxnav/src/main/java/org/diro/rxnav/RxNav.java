package org.diro.rxnav;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * RxNav is a browser for several drug information sources, including RxNorm, RxTerms and NDF-RT.
 * RxNav finds drugs in RxNorm from the names and codes in its constituent vocabularies. RxNav
 * displays links from clinical drugs, both branded and generic, to their active ingredients, drug
 * components and related brand names. RxNav also provides lists of NDC codes and links to package
 * inserts in DailyMed. The RxTerms record for a given drug can be accessed through RxNav, as well
 * as clinical information from NDF-RT, including pharmacologic classes, mechanisms of action and
 * physiologic effects.
 * <p/>
 * Only the JSON RxNav API is provided. If you like XML, you probably like hardcoding requests
 * anyway, so do not use this SDK.
 * <p/>
 * The documentation provided is not guaranteed to be up-to-date with what's available on RxNav
 * official website (http://rxnav.nlm.nih.gov/) so only use it for informational purpose.
 * <p/>
 * If you find a bug, fill an issue right now! (I will fix it, I promise)
 *
 * @author Guillaume Poirier-Morency
 */
public class RxNav {

    private final HttpClient client;

    /**
     * @param client HTTP client used to perform requests
     */
    public RxNav(HttpClient client) {
        this.client = client;
    }

    /**
     * Execute a HTTP request on RxNav public endpoint using the provided
     * {@link org.apache.http.client.HttpClient}.
     *
     * @param path  requested path prefixed by "/REST/" and suffixed by ".json"
     * @param query HTTP query hopefully encoded by {@link java.net.URLEncoder}
     * @return the requested resource that should be extracted to the meaningful data
     * @throws IOException        always expect some I/O failure
     * @throws JSONException      should not happen unless the API returns a corrupted response
     * @throws URISyntaxException if a silly path or query has been provided
     */
    protected JSONObject get(String path, String query) throws IOException, JSONException, URISyntaxException {
        HttpResponse response = client.execute(new HttpGet(new URI("http", null, "rxnav.nlm.nih.gov", 80, "/REST/" + path + ".json", query, null)));

        // Recent JSON api support reading from InputStream, but we are kinda stuck dumping the
        // whole InputStream into a String...
        String body = IOUtils.toString(response.getEntity().getContent());

        return (JSONObject) new JSONTokener(body).nextValue();
    }

}
