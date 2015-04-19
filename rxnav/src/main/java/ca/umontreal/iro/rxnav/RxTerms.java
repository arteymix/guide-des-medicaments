package ca.umontreal.iro.rxnav;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * The RxTerms API is a web service for accessing the current RxTerms data set. No license is needed
 * to use the RxTerms API.
 * <p/>
 * http://rxnav.nlm.nih.gov/RxTermsAPIs.html
 *
 * @author Guillaume Poirier-Morency
 */
public class RxTerms extends RxNav {

    public static RxTerms newInstance(OkHttpClient httpClient) {
        return new RxTerms(httpClient);
    }

    public RxTerms(OkHttpClient httpClient) {
        super(httpClient);
    }

    /**
     * This resource gets the RxTerms information for a specified RxNorm concept.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject getAllRxTermInfo(int rxcui) throws IOException, JSONException {
        return get("RxTerms/rxcui/" + rxcui + "/allinfo")
                .getJSONObject("rxtermsProperties");
    }

    /**
     * Get the RxTerms display name for a specified RxNorm concept.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public String getRxTermDisplayName(int rxcui) throws IOException, JSONException {
        return get("RxTerms/rxcui/" + rxcui + "/name")
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
     */
    public String getRxTermsVersion() throws IOException, JSONException {
        return get("RxTerms/version").getString("rxtermsVersion");
    }

    /**
     * This resource returns concept information for all RxTerms concepts.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxTermsAPIs.html#uLink=RxTerms_REST_getAllConcepts
     *
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray getAllConcepts() throws IOException, JSONException {
        return get("RxTerms/allconcepts")
                .getJSONObject("minConceptGroup")
                .getJSONArray("minConcept");
    }
}
