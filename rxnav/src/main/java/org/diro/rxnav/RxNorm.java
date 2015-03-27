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

    public static RxNorm newInstance() {
        return new RxNorm();
    }

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
        if (propValues.length > 0)
            return get("rxcui/" + rxcui + "/filter",
                    new BasicNameValuePair("propName", propName),
                    new BasicNameValuePair("propValues", StringUtils.join(propValues, " ")))
                    .getJSONObject("propConceptGroup")
                    .getJSONArray("propConcept");

        return get("rxcui/" + rxcui + "/filter", new BasicNameValuePair("propName", propName))
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
        if (prop.length > 0)
            return get("rxcui/" + rxcui + "/allProperties", new BasicNameValuePair("prop", StringUtils.join(prop, " ")))
                    .getJSONObject("propConceptGroup")
                    .getJSONArray("propConcept");

        return get("rxcui/" + rxcui + "/allProperties")
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
        return get("rxcui/" + rxcui + "/allrelated")
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
        return get("approximateTerm",
                new BasicNameValuePair("term", term),
                new BasicNameValuePair("maxEntries", Integer.toString(maxEntries)),
                new BasicNameValuePair("option", Integer.toString(option)))
                .getJSONObject("approximateGroup")
                .getJSONArray("candidate");
    }

    /**
     * Gets the names used by RxNav for auto completion. This is a large list which includes names
     * of ingredients, brands, and branded packs.
     *
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray getDisplayTerms() throws IOException, JSONException {
        return get("displaynames")
                .getJSONObject("displayTermsList")
                .getJSONArray("term");
    }

    /**
     * Get the drug products associated with a specified name. The name can be an ingredient, brand
     * name, clinical dose form, branded dose form, clinical drug component, or branded drug
     * component.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxNorm_REST_getDrugs
     *
     * @param name an ingredient, brand, clinical dose form, branded dose form, clinical drug
     *             component or branded drug component name
     * @return
     */
    public JSONArray getDrugs(String name) throws IOException, JSONException {
        return get("drugs", new BasicNameValuePair("name", name))
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
        return get("rxcui/" + rxcui + "/allrelated")
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
        return get("rxcui/" + rxcui + "/properties").getJSONObject("properties");
    }
}
