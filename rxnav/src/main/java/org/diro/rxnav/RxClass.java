package org.diro.rxnav;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * The RxClass API is a web service for accessing drug classes and drug members for a number of
 * different drug class types. No license is needed to use the RxClass API.
 * <p/>
 *
 * @author Guillaume Poirier-Morency
 */
public class RxClass extends RxNav {

    public RxClass(HttpClient client) {
        super(client);
    }

    @Override
    public JSONObject get(String path, String query) throws JSONException, IOException, URISyntaxException {
        return super.get("rxclass/" + path, query);
    }

    /**
     * Get all classes for each specified class type.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxNormAPIs.html#uLink=RxClass_REST_getAllClasses
     *
     * @param classTypes
     * @return
     * @throws java.io.IOException
     * @throws org.json.JSONException
     */
    public JSONArray allClasses(String... classTypes) throws IOException, JSONException, URISyntaxException {
        return get("allClasses", classTypes.length > 0 ? "classTypes=" + URLEncoder.encode(StringUtils.join(classTypes, " "), "UTF-8") : null)
                .getJSONObject("rxclassMinConceptList")
                .getJSONArray("rxclassMinConcept");
    }

    /**
     * Get the spelling suggestions for a drug or class name.
     *
     * @param term the name of the drug or class.
     * @param type type of name. Valid values are "DRUG" or "CLASS" for a drug name or class name,
     *             respectively.
     * @return
     */
    public JSONArray getSpellingSuggestions(String term, String type) throws IOException, JSONException, URISyntaxException {
        return get("spellingsuggestions", URLEncoder.encode("term=" + term + "&type=" + type, "UTF-8"))
                .getJSONObject("suggestionList")
                .getJSONArray("suggestion");
    }
}
