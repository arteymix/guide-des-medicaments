package ca.umontreal.iro.rxnav;

import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

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

    public static RxNorm newInstance(OkHttpClient httpClient) {
        return new RxNorm(httpClient);
    }

    public RxNorm(OkHttpClient httpClient) {
        super(httpClient);
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
     */
    public JSONArray filterByProperty(String rxcui, String propName, String... propValues) throws IOException, JSONException {
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

    public class Rxcui {

        public class IdGroup {

            public String name;
            public String[] rxnormId;
        }

        public IdGroup idGroup;
    }

    public static final int
            EXACT_MATCH = 0,
            NORMALIZED = 1,
            EXACT_MATCH_THEN_NORMALIZED = 2;

    /**
     * Search for a name in the RxNorm data set and return the RxCUIs of any concepts which have
     * that name as an RxNorm term or as a synonym of an RxNorm term.
     *
     * @param name    the search string. This must be specified.
     * @param srclist a list of the source vocabulary names to use. Only used when allsrc is set to
     *                1. See the /sourcetypes example for the valid source vocabularies. If not
     *                specified, all sources are used.
     * @param allsrc  a field indicating whether all RxCUIs are to be returned. If set to 0
     *                (the default), only RxCUIs which contain a non-suppressed RxNorm vocabulary
     *                term will be returned. If set to 1, all non-suppressed RxCUIs will be returned
     *                that match any of the sources specified, even if there is not a RxNorm
     *                vocabulary term for the concept.
     * @param search  a number indicating the type of search to be performed. If set to 0
     *                (the default), exact match search will be performed. If set to 1, a normalized
     *                string search will be done. When search = 2, then an exact match search will
     *                be done, and if no results are found, a normalized search will be done.
     * @return
     * @throws IOException
     */
    public Rxcui findRxcuiByString(String name, String[] srclist, boolean allsrc, int search) throws IOException {
        return srclist == null ?
                request(Rxcui.class, "rxcui",
                        new BasicNameValuePair("name", name),
                        new BasicNameValuePair("allsrc", allsrc ? "1" : "0"),
                        new BasicNameValuePair("search", Integer.toString(search))) :
                request(Rxcui.class, "rxcui",
                        new BasicNameValuePair("name", name),
                        new BasicNameValuePair("srclist", StringUtils.join(srclist, " ")),
                        new BasicNameValuePair("allsrc", allsrc ? "1" : "0"),
                        new BasicNameValuePair("search", Integer.toString(search)));
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
     */
    public JSONArray getAllProperties(String rxcui, String... prop) throws IOException, JSONException {
        if (prop.length > 0)
            return get("rxcui/" + rxcui + "/allProperties", new BasicNameValuePair("prop", StringUtils.join(prop, " ")))
                    .getJSONObject("propConceptGroup")
                    .getJSONArray("propConcept");

        return get("rxcui/" + rxcui + "/allProperties")
                .getJSONObject("propConceptGroup")
                .getJSONArray("propConcept");
    }

    public class AllRelatedInfo {

        public RelatedGroup allRelatedGroup;
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
    public AllRelatedInfo getAllRelatedInfo(String rxcui) throws IOException {
        return request(AllRelatedInfo.class, "rxcui/" + rxcui + "/allrelated");
    }

    public class ApproximateGroup {

        public class Candidate {

            public String rxcui;
            public String rxaui;
            public String score;
            public String rank;
        }

        public String inputTerm;
        public String maxEntries;
        public String comment;
        public Candidate[] candidate;
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
     */
    public ApproximateGroup getApproximateMatch(String term, int maxEntries, int option) throws IOException {
        return request(ApproximateGroup.class, "approximateTerm",
                new BasicNameValuePair("term", term),
                new BasicNameValuePair("maxEntries", Integer.toString(maxEntries)),
                new BasicNameValuePair("option", Integer.toString(option)));
    }

    public class DisplayTerms {

        public class DisplayTermsList {
            public String[] term;
        }

        public DisplayTermsList displayTermsList;
    }

    /**
     * Gets the names used by RxNav for auto completion. This is a large list which includes names
     * of ingredients, brands, and branded packs.
     *
     * @return
     * @throws IOException
     */
    public DisplayTerms getDisplayTerms() throws IOException {
        return request(DisplayTerms.class, "displaynames");
    }

    public class ConceptProperties {

        public String rxcui;
        public String name;
        public String synonym;
        public String tty;
        public String language;
        public String suppress;
        public String umlscui;
    }

    public class ConceptGroup {

        public String tty;
        public ConceptProperties[] conceptProperties;
    }

    public class Drugs {

        public class DrugGroup {

            public String name;
            public ConceptGroup[] conceptGroup;
        }

        public DrugGroup drugGroup;
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
    public Drugs getDrugs(String name) throws IOException {
        return request(Drugs.class, "drugs", new BasicNameValuePair("name", name));
    }

    public class RelatedGroup {

        public String rxcui;
        public String[] termType;
        public ConceptGroup[] conceptGroup;
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
    public RelatedGroup getRelatedByType(String rxcui, String... tty) throws IOException {
        return request(RelatedGroup.class, "rxcui/" + rxcui + "/allrelated");
    }

    public class RxConceptProperties {

        public ConceptProperties properties;
    }

    /**
     * Get the RxNorm concept properties.
     *
     * @param rxcui
     * @return
     * @throws IOException
     */
    public RxConceptProperties getRxConceptProperties(String rxcui) throws IOException {
        return request(RxConceptProperties.class, "rxcui/" + rxcui + "/properties");
    }

    public class SpellingSuggestions {

        public class SuggestionGroup {

            public class SuggestionList {

                public String[] suggestion;
            }

            public String name;
            public SuggestionList suggestionList;
        }

        public SuggestionGroup suggestionGroup;
    }

    /**
     * Get spelling suggestions for a given term. The suggestions are RxNorm terms contained in the
     * current version, listed in decreasing order of closeness to the original phrase.
     *
     * @param name the name for which spelling suggestions are to be generated. This field is
     *             required.
     * @return
     * @throws IOException
     */
    public SpellingSuggestions getSpellingSuggestions(String name) throws IOException {
        return request(SpellingSuggestions.class, "spellingsuggestions", new BasicNameValuePair("name", name));
    }

    public class TermTypes {

        public class TermTypeList {

            public String[] termType;
        }

        public TermTypeList termTypeList;
    }

    /**
     * Get the valid term types in the RxNorm data set.
     *
     * @return
     * @throws IOException
     */
    public TermTypes getTermTypes() throws IOException {
        return request(TermTypes.class, "termtypes");
    }
}
