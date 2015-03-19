package org.diro.rxnav;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * The Interaction API is a web service for accessing drug-drug interactions. No license is needed
 * to use the Interaction API.
 *
 * @author Guillaume Poirier-Morency
 */
public class Interaction extends RxNav {

    public Interaction(HttpClient client) {
        super(client);
    }

    /**
     * Get the drug interactions for a specified drug.
     *
     * @param rxcui
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray findDrugInteractions(int rxcui, String... sources) throws IOException, JSONException, URISyntaxException {
        String query = "rxcui=" + rxcui;

        if (sources.length > 0)
            query += "&sources=" + StringUtils.join(sources, " ");

        return get("interaction", URLEncoder.encode(query, "UTF-8"))
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
     * @throws URISyntaxException
     */
    public JSONArray findInteractionsFromList(int[] rxcuis, String... sources) throws IOException, JSONException, URISyntaxException {
        String query = "rxcui=" + StringUtils.join(rxcuis, " ");

        if (sources.length > 0)
            query += "&sources=" + StringUtils.join(sources, " ");

        return get("interaction", URLEncoder.encode(query, "UTF-8"))
                .getJSONObject("fullInteractionTypeGroup")
                .getJSONArray("fullInteractionType");
    }
}
