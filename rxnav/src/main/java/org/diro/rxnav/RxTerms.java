package org.diro.rxnav;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The RxTerms API is a web service for accessing the current RxTerms data set. No license is needed
 * to use the RxTerms API.
 * <p/>
 * http://rxnav.nlm.nih.gov/RxTermsAPIs.html
 *
 * @author Guillaume Poirier-Morency
 */
public class RxTerms extends RxNav {

    public RxTerms(HttpClient client) {
        super(client);
    }

    @Override
    public JSONObject get(String path, String query) throws JSONException, IOException, URISyntaxException {
        return super.get(path + "RxTerms/", query);
    }

    /**
     * This resource gets the RxTerms information for a specified RxNorm concept.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public JSONObject getAllRxTermInfo(int rxcui) throws IOException, JSONException, URISyntaxException {
        return get("rxcui/" + rxcui + "/allinfo", null)
                .getJSONObject("rxtermsProperties");
    }

    /**
     * Get the RxTerms display name for a specified RxNorm concept.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public String getRxTermDisplayName(int rxcui) throws IOException, JSONException, URISyntaxException {
        return get("rxcui/" + rxcui + "/name", null)
                .getJSONObject("displayGroup")
                .getString("displayName");
    }

    /**
     * Get the RxTerms version
     * <p/>
     * http://rxnav.nlm.nih.gov/RxTermsAPIs.html#uLink=RxTerms_REST_getRxTermsVersion
     *
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public String getRxTermsVersion() throws IOException, JSONException, URISyntaxException {
        return get("version", null).getString("rxtermsVersion");
    }

    /**
     * This resource returns concept information for all RxTerms concepts.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxTermsAPIs.html#uLink=RxTerms_REST_getAllConcepts
     *
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public JSONArray getAllConcepts() throws IOException, JSONException, URISyntaxException {
        return get("allconcepts", null)
                .getJSONObject("minConceptGroup")
                .getJSONArray("minConcept");
    }
}
