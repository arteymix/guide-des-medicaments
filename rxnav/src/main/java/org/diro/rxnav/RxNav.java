package org.diro.rxnav;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * RxNav is a browser for several drug information sources, including RxNorm, RxTerms and NDF-RT.
 * <p/>
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

    /**
     *
     */
    public final String scheme;

    /**
     *
     */
    public final String host;

    /**
     *
     */
    public final int port;

    /**
     *
     */
    public final String basePath;

    public final String suffix;

    /**
     * Initialize RxNav to use a custom API endpoint.
     *
     * @param scheme
     * @param host
     * @param port
     * @param basePath
     * @param suffix
     */
    public RxNav(String scheme, String host, int port, String basePath, String suffix) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
        this.suffix = suffix;
    }

    /**
     * Initialize RxNav to use the public API at http://rxnav.nlm.nih.gov
     */
    public RxNav() {
        this("http", "rxnav.nlm.nih.gov", 80, "/REST/", ".json");
    }

    /**
     * Execute a HTTP request on RxNav endpoint using {@link java.net.HttpURLConnection}.
     * <p/>
     * RxNav generally returns a load of useless meta-data and it is expected to be extracted by the
     * caller.
     * <p/>
     * As recommended by the 'Terms Of Service', all requests are cached for a period of 24 hours
     * assuming that {@link java.net.ResponseCache} has been set correctly.
     *
     * @param path  requested path prefixed by "/REST/" and suffixed by ".json"
     * @param query HTTP query used to parametrize the request
     * @return the requested resource that should be extracted to the meaningful data
     * @throws IOException   always expect some I/O failure
     * @throws JSONException should not happen unless the API returns a corrupted response
     */
    protected JSONObject get(String path, NameValuePair... query) throws IOException, JSONException {
        URL url = new URL(scheme, host, port, basePath + path + suffix + (query.length > 0 ? "" : "?" + URLEncodedUtils.format(Arrays.asList(query), "UTF-8")));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // accepting staled response for up to 24 hours
        connection.addRequestProperty("Cache-Control", "max-stale=86400");

        try {
            return (JSONObject) new JSONTokener(IOUtils.toString(connection.getInputStream())).nextValue();
        } finally {
            connection.disconnect();
        }
    }

}
