package ca.umontreal.iro.rxnav;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

/**
 * RxImageAccess Applications Programming Interface (API) is an online offering from the National
 * Library of Medicine (NLM) Lister Hill National Center for Biomedical Communications’ Office of
 * High Performance Computing and Communications (OHPCC) for searching for and retrieving digital
 * images and associated metadata for prescription oral solid dosage formulations (tablets, gel
 * caps and capsules, etc. – commonly known as pills) from a publicly available, increasingly
 * comprehensive, standardized, curated database. RxImageAccess has the following features:
 * <p/>
 * <ul>
 * <li>Curated image database of over 2,300 prescription products representing over 6,000 NDCs.</li>
 * <li>RxImageAccess is a RESTful (representational state transfer) Web API.</li>
 * <li>Results are returned in either JavaScript Object Notation (JSON) or Extensible Markup Language (XML) encoding.</li>
 * <li>Search parameters can include pill color, shape, imprint (text on a pill), and size, and NDC (National Drug Code), RxCUI (RxNorm Concept Unique Identifier), and Structured Product Label (SPL) identifier (SetID).</li>
 * <li>Images include watermarks to identify them as having originated with the NLM.</li>
 * <li>Images can include an information panel that provides the name, dosage, manufacturer, shape, size, color, and imprint.</li>
 * <li>Output control parameters include image resolution, number of results, number of images per page, page number, and whether returned metadata are to include active ingredients, inactive ingredients, trade names, and generic names.</li>
 * <li>RxImageAccess image files are standardized in two layout formats and available in multiple resolutions to support online, mobile, and desktop application development.</li>
 * </ul>
 * <p/>
 * http://rximage.nlm.nih.gov/docs/doku.php?id=start
 *
 * @author Guillaume Poirier-Morency
 */
public class RxImageAccess extends RxNav {

    public static RxImageAccess newInstance() {
        return new RxImageAccess();
    }

    /**
     * Initialize RxNav to use the public API at http://rxnav.nlm.nih.gov
     * <p/>
     * It is recommended to use your own API
     */
    public RxImageAccess() {
        super("http", "rximage.nlm.nih.gov", 80, "/api/", "");
    }

    /**
     * This is the recommended image collection due to the inclusion of an information panel.
     *
     * @param query
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray rxnav(NameValuePair... query) throws IOException, JSONException {
        return get("rximage/1/rxnav", query).getJSONArray("nlmRxImages");
    }

    /**
     * This collection is identical to the rxnav collection without the panel.
     *
     * @param query
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray rxbase(NameValuePair... query) throws IOException, JSONException {
        return get("rximage/1/rxbase", query).getJSONArray("nlmRxImages");
    }

}
