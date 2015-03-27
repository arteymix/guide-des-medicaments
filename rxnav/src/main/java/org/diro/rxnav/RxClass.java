package org.diro.rxnav;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * The RxClass API is a web service for accessing drug classes and drug members for a number of
 * different drug class types. No license is needed to use the RxClass API.
 * <p/>
 *
 * @author Guillaume Poirier-Morency
 */
public class RxClass extends RxNav {

    @Override
    public JSONObject get(String path, NameValuePair... query) throws JSONException, IOException {
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
    public JSONArray allClasses(String... classTypes) throws IOException, JSONException {
        if (classTypes.length > 0)
            return get("allClasses", new BasicNameValuePair("classTypes", StringUtils.join(classTypes, " ")))
                    .getJSONObject("rxclassMinConceptList")
                    .getJSONArray("rxclassMinConcept");

        return get("allClasses")
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
    public JSONArray getSpellingSuggestions(String term, String type) throws IOException, JSONException {
        return get("spellingsuggestions",
                new BasicNameValuePair("term", term),
                new BasicNameValuePair("type", type))
                .getJSONObject("suggestionList")
                .getJSONArray("suggestion");
    }
}
