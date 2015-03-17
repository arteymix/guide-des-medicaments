package ca.umontreal.iro.guidedesmedicaments.rxnav;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * <p/>
 * Pour optimiser les appels, l'api peut être instancié avec un HttpClient qui supporte la mise en
 * cache des réponses.
 *
 * @author Guillaume Poirier-Morency
 */
public class RxNorm extends RxNav {

    public RxNorm(HttpClient client) {
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
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray allClasses(String... classTypes) throws IOException, JSONException, URISyntaxException {
        return get("allClasses", classTypes.length > 0 ? "classTypes=" + URLEncoder.encode(StringUtils.join(classTypes, " "), "UTF-8") : null)
                .getJSONObject("rxclassMinConceptList")
                .getJSONArray("rxclassMinConcept");
    }
}
