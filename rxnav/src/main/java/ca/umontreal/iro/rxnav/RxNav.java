package ca.umontreal.iro.rxnav;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
     * Used to request the RxNav API.
     */
    protected final OkHttpClient httpClient;

    /**
     * Used to extract and generate JSON.
     */
    protected final Gson gson = new Gson();

    /**
     * Initialize RxNav to use a custom API endpoint.
     *
     * @param httpClient OkHttpClient used to request the API (reuse it as much as possible!)
     * @param scheme     should be http or https
     * @param host       hostname or ip address from which the API is served
     * @param port       port, usually 80 unless you do request through SSL
     * @param basePath   base path prefixing path if the API is serving from a non-standard path
     * @param suffix     generally ".json", but can be overwritten when needed
     */
    public RxNav(OkHttpClient httpClient, String scheme, String host, int port, String basePath, String suffix) {
        this.httpClient = httpClient;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
        this.suffix = suffix;
    }

    /**
     * Initialize RxNav to use the public API at http://rxnav.nlm.nih.gov
     */
    public RxNav(OkHttpClient httpClient) {
        this(httpClient, "http", "rxnav.nlm.nih.gov", 80, "/REST/", ".json");
    }

    /**
     * Request RxNav API and extract the JSON in the providen class using {@link Gson}.
     * <p/>
     * As recommended by the 'Terms Of Service', all requests are cached for a period of 24 hours
     * assuming that {@link java.net.ResponseCache} has been set correctly.
     *
     * @param classOfT class populated by {@link Gson}
     * @param path     requested path automatically prefixed by "/REST/" and suffixed by ".json"
     * @param query    HTTP query used to parametrize the request
     * @return an opened connection to the API that should be closed by the caller
     * @throws IOException always expect some I/O failure
     */
    protected <T> T request(Class<T> classOfT, String path, NameValuePair... query) throws IOException {
        URL url = new URL(scheme, host, port, basePath + path + suffix + "?" + URLEncodedUtils.format(Arrays.asList(query), "UTF-8"));

        // accepting staled response for up to 24 hours
        CacheControl cacheControl = new CacheControl.Builder()
                .maxStale(24, TimeUnit.HOURS)
                .build();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .cacheControl(cacheControl)
                .build();

        Response response = httpClient.newCall(request).execute();

        Log.i("RxNav", response.code() + " GET " + url);

        try {
            return gson.fromJson(response.body().charStream(), classOfT);
        } finally {
            response.body().close();
        }
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
     * @deprecated use openHttpURLConnection with {@link Gson}
     */
    @Deprecated
    protected JSONObject get(String path, NameValuePair... query) throws IOException, JSONException {
        URL url = new URL(scheme, host, port, basePath + path + suffix + "?" + URLEncodedUtils.format(Arrays.asList(query), "UTF-8"));

        // accepting staled response for up to 24 hours
        CacheControl cacheControl = new CacheControl.Builder()
                .maxStale(24, TimeUnit.HOURS)
                .build();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .cacheControl(cacheControl)
                .build();

        Response response = httpClient.newCall(request).execute();

        Log.i("RxNav", response.code() + " GET " + url);

        try {
            return (JSONObject) new JSONTokener(response.body().string()).nextValue();
        } finally {
            response.body().close();
        }
    }
}
