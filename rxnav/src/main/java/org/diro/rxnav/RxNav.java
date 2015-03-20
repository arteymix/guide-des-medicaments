package org.diro.rxnav;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

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

    /**
     * @param host
     * @param port
     * @param basePath
     */
    public RxNav(String scheme, String host, int port, String basePath) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
    }

    /**
     * Initialize RxNav to use the public API at http://rxnav.nlm.nih.gov
     * <p/>
     * It is recommended to use your own API
     */
    public RxNav() {
        this("http", "rxnav.nlm.nih.gov", 80, "/REST/");
    }

    /**
     * Execute a HTTP request on RxNav public endpoint using the provided
     * {@link org.apache.http.client.HttpClient}.
     *
     * @param path  requested path prefixed by "/REST/" and suffixed by ".json"
     * @param query HTTP query hopefully encoded by {@link java.net.URLEncoder}
     * @return the requested resource that should be extracted to the meaningful data
     * @throws IOException   always expect some I/O failure
     * @throws JSONException should not happen unless the API returns a corrupted response
     */
    protected JSONObject get(String path, List<? extends NameValuePair> query) throws IOException, JSONException {
        URL url = new URL(scheme, host, port, basePath + path + ".json?" + URLEncodedUtils.format(query, "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        return (JSONObject) new JSONTokener(connection.getResponseMessage()).nextValue();
    }

}
