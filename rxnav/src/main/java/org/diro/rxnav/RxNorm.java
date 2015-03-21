package org.diro.rxnav;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * The RxNorm API is a web service for accessing the current RxNorm data set.
 * <p/>
 * With one exception, no license is needed to use the RxNorm API. This is because the data returned
 * from the API is from the RxNorm vocabulary, a non-proprietary vocabulary developed by the
 * National Library of Medicine.
 * <p/>
 * http://rxnav.nlm.nih.gov/RxNormAPIs.html
 *
 * @author Guillaume Poirier-Morency
 */
public class RxNorm extends RxNav {

    /**
     * Determine if a property exists for a concept and (optionally) matches the specified property
     * value. Returns the RxCUI if the property name matches.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxNorm_REST_filterByProperty
     *
     * @param rxcui
     * @param propName   the property name. See /propnames for the list of valid property names
     * @param propValues (optional) the property value. If not specified, the RxCui is returned if
     *                   the property exists for the concept.
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public JSONArray filterByProperty(String rxcui, String propName, String... propValues) throws IOException, JSONException, URISyntaxException {
        List<NameValuePair> query = new ArrayList<>();

        query.add(new BasicNameValuePair("propName", propName));

        if (propValues.length > 0)
            query.add(new BasicNameValuePair("propValues", StringUtils.join(propValues, " ")));

        return get("rxcui/" + rxcui + "/filter", query)
                .getJSONObject("propConceptGroup")
                .getJSONArray("propConcept");
    }

    /**
     * Return the properties for a specified concept.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxNorm_REST_getAllProperties
     *
     * @param rxcui
     * @param prop  the property categories for the properties to be returned. This field is
     *              required. See the /propCategories example for the valid property categories.
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    public JSONArray getAllProperties(String rxcui, String... prop) throws IOException, JSONException, URISyntaxException {
        List<NameValuePair> query = new ArrayList<>();

        if (prop.length > 0)
            query.add(new BasicNameValuePair("prop", StringUtils.join(prop, " ")));

        return get("rxcui/" + rxcui + "/allProperties", query)
                .getJSONObject("propConceptGroup")
                .getJSONArray("propConcept");
    }

    /**
     * Get all the related RxNorm concepts for a given RxNorm identifier. This includes concepts of
     * term types "IN", "MIN", "PIN", "BN", "SBD", "SBDC", "SBDF", "SBDG", "SCD", "SCDC", "SCDF",
     * "SCDG", "DF", "DFG", "BPCK" and "GPCK". See default paths for the paths traveled to get
     * concepts for each term type.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxNorm_REST_getAllRelatedInfo
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray getAllRelatedInfo(String rxcui) throws IOException, JSONException {
        return get("rxcui/" + rxcui + "/allrelated", null)
                .getJSONObject("allRelatedGroup")
                .getJSONArray("conceptGroup");
    }

    /**
     * Do an approximate match search to determine the strings in the data set that most closely
     * match the search string. The approximate match algorithm is discussed in detail here.
     * <p/>
     * The returned comment field contains messages about the processing of the operation.
     *
     * @param term       the search string
     * @param maxEntries (optional, default=20) the maximum number of entries to returns
     * @param option     (optional, default=0) special processing options. Valid values:
     *                   0 - no special processing
     *                   1 - return only information for terms contained in valid RxNorm concepts.
     *                   That is, the term must either be from the RxNorm vocabulary or a synonym of
     *                   a (non-suppressed) term in the RxNorm vocabulary.
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray getApproximateMatch(String term, int maxEntries, int option) throws IOException, JSONException {
        List<NameValuePair> query = new ArrayList<NameValuePair>();

        query.add(new BasicNameValuePair("term", term));
        query.add(new BasicNameValuePair("maxEntries", Integer.toString(maxEntries)));
        query.add(new BasicNameValuePair("option", Integer.toString(option)));

        return get("approximateTerm", query)
                .getJSONObject("approximateGroup")
                .getJSONArray("candidate");
    }

    /**
     * Get the drug products associated with a specified name. The name can be an ingredient, brand
     * name, clinical dose form, branded dose form, clinical drug component, or branded drug
     * component.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxNorm_REST_getDrugs
     *
     * @param rxcui
     * @param name  an ingredient, brand, clinical dose form, branded dose form, clinical drug
     *              component or branded drug component name
     * @return
     */
    public JSONArray getDrugs(String rxcui, String name) throws IOException, JSONException {
        return get("drugs", null)
                .getJSONObject("drugGroup")
                .getJSONArray("conceptGroup");
    }

    /**
     * Get the related RxNorm identifiers of an RxNorm concept specified by one or more term types.
     * See default paths for the paths traveled to get concepts for each term type.
     *
     * @param rxcui
     * @param tty   a list of one or more RxNorm term types. This field is required. See the
     *              /termtypes example for the valid term types.
     * @return
     */
    public JSONArray getRelatedByType(String rxcui, String... tty) throws IOException, JSONException {
        return get("rxcui/" + rxcui + "/allrelated", null)
                .getJSONObject("relatedGroup")
                .getJSONArray("conceptGroup");
    }

    /**
     * Get the RxNorm concept properties.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject getRxConceptProperties(String rxcui) throws IOException, JSONException {
        return get("rxcui/" + rxcui + "/properties", null).getJSONObject("properties");
    }
}
