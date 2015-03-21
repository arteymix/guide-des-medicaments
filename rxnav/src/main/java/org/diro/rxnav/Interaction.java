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
 * The Interaction API is a web service for accessing drug-drug interactions. No license is needed
 * to use the Interaction API.
 *
 * @author Guillaume Poirier-Morency
 */
public class Interaction extends RxNav {

    @Override
    public JSONObject get(String path, List<? extends NameValuePair> query) throws JSONException, IOException {
        return super.get("interaction/" + path, query);
    }

    /**
     * Get the drug interactions for a specified drug.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray findDrugInteractions(String rxcui, String... sources) throws IOException, JSONException {
        List<NameValuePair> query = new ArrayList<>();

        query.add(new BasicNameValuePair("rxcui", rxcui));

        if (sources.length > 0)
            query.add(new BasicNameValuePair("propValues", StringUtils.join(sources, " ")));

        return get("interaction", query)
                .getJSONObject("interactionTypeGroup")
                .getJSONArray("interactionType");
    }

    /**
     * Get the drug interactions between drugs in a list.
     *
     * @param rxcuis  the list of RxNorm drugs, specified by the RxNorm identifiers. The identifiers
     *                can represent ingredients (e.g. simvastatin), brand names (e.g. Tylenol) or
     *                branded or clinical drugs (e.g. Morphine 50 mg oral tablet). A maximum of 50
     *                identifiers is allowed.
     * @param sources the sources to use. If not specified, all sources will be used.
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray findInteractionsFromList(String[] rxcuis, String... sources) throws IOException, JSONException {
        List<NameValuePair> query = new ArrayList<>();

        query.add(new BasicNameValuePair("rxcuis", StringUtils.join(rxcuis, " ")));

        if (sources.length > 0)
            query.add(new BasicNameValuePair("propValues", StringUtils.join(sources, " ")));

        return get("interaction", query)
                .getJSONObject("fullInteractionTypeGroup")
                .getJSONArray("fullInteractionType");
    }
}
