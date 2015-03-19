package org.diro.rxnav;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * The RxNorm API is a web service for accessing the current RxNorm data set.
 * <p/>
 * With one exception, no license is needed to use the RxNorm API. This is because the data returned
 * from the API is from the RxNorm vocabulary, a non-proprietary vocabulary developed by the
 * National Library of Medicine.
 *
 * http://rxnav.nlm.nih.gov/RxNormAPIs.html
 *
 * @author Guillaume Poirier-Morency
 */
public class RxNorm extends RxNav {

    public RxNorm(HttpClient client) {
        super(client);
    }

    @Override
    public JSONObject get(String path, String query) throws JSONException, IOException, URISyntaxException {
        return super.get("rxcui/" + path, query);
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
    public JSONArray filterByProperty(int rxcui, String propName, String... propValues) throws IOException, JSONException, URISyntaxException {
        String query = "propName=" + propName;

        if (propValues.length > 0)
            query += "&propValues=" + StringUtils.join(propValues, " ");

        return get(rxcui + "/filter", URLEncoder.encode(query, "UTF-8"))
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
    public JSONArray getAllProperties(int rxcui, String... prop) throws IOException, JSONException, URISyntaxException {
        return get(rxcui + "/allProperties", URLEncoder.encode("prop=" + StringUtils.join(prop, " "), "UTF-8"))
                .getJSONObject("propConceptGroup")
                .getJSONArray("propConcept");
    }
}
