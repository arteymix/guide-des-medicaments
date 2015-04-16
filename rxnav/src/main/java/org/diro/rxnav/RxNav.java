package org.diro.rxnav;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * RxNav is a browser for several drug information sources, including RxNorm, RxTerms and NDF-RT.
 * <p/>
 * RxNav finds drugs in RxNorm from the names and codes in its constituent vocabularies. RxNav
 * displays lnks from clinical drugs, both branded and generic, to their active ingredients, drug
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

    private final String scheme;

    private final String host;

    private final int port;

    private final String basePath;

    private final String suffix;

    /**
     * Used to extract and generate JSON.
     */
    protected final Gson gson = new Gson();

    /**
     * Initialize RxNav to use a custom API endpoint.
     *
     * @param scheme   should be http or https
     * @param host     hostname or ip address from which the API is served
     * @param port     port, usually 80 unless you do request through SSL
     * @param basePath base path prefixing path if the API is serving from a non-standard path
     * @param suffix   generally ".json", but can be overwritten when needed
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
     * Obtain a HttpURLConnection against the requested resource on RxNav API.
     * <p/>
     * As recommended by the 'Terms Of Service', all requests are cached for a period of 24 hours
     * assuming that {@link java.net.ResponseCache} has been set correctly.
     *
     * @param path  requested path automatically prefixed by "/REST/" and suffixed by ".json"
     * @param query HTTP query used to parametrize the request
     * @return
     * @throws IOException always expect some I/O failure
     */
    protected HttpURLConnection getHttpConnection(String path, NameValuePair... query) throws IOException {
        URL url = new URL(scheme, host, port, basePath + path + suffix + "?" + URLEncodedUtils.format(Arrays.asList(query), "UTF-8"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // accepting staled response for up to 24 hours
        connection.addRequestProperty("Cache-Control", "max-stale=86400");

        return connection;
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
     * @deprecated use {@link RxNav.getHttpConnection} with {@link Gson}
     */
    protected JSONObject get(String path, NameValuePair... query) throws IOException, JSONException {
        HttpURLConnection connection = getHttpConnection(path, query);

        try {
            return (JSONObject) new JSONTokener(IOUtils.toString(connection.getInputStream())).nextValue();
        } finally {
            connection.disconnect();
        }
    }

}
