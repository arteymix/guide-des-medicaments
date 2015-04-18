package ca.umontreal.iro.rxnav;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

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

    public class Rxcui implements Parcelable {

        public class IdGroup implements Parcelable {
            public String name;
            public String[] rxnormId;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(name);
                dest.writeStringArray(rxnormId);
            }
        }

        public IdGroup idGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(idGroup, flags);
        }
    }

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
        return request(Rxcui.class, "rxcui",
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

    public class ApproximateGroup implements Parcelable {

        public class Candidate implements Parcelable {
            public String rxcui;
            public String rxaui;
            public String score;
            public String rank;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(rxcui);
                dest.writeString(rxaui);
                dest.writeString(score);
                dest.writeString(rank);
            }
        }

        public String inputTerm;
        public String maxEntries;
        public String comment;
        public Candidate[] candidate;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(inputTerm);
            dest.writeString(maxEntries);
            dest.writeString(comment);
            dest.writeParcelableArray(candidate, flags);
        }
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

    public class DisplayTerms implements Parcelable {

        public class DisplayTermsList implements Parcelable {
            public String[] term;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringArray(term);
            }
        }

        public DisplayTermsList displayTermsList;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(displayTermsList, flags);
        }
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

    public class ConceptProperties implements Parcelable {
        public String rxcui;
        public String name;
        public String synonym;
        public String tty;
        public String language;
        public String suppress;
        public String umlscui;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(rxcui);
            dest.writeString(name);
            dest.writeString(synonym);
            dest.writeString(tty);
            dest.writeString(language);
            dest.writeString(suppress);
            dest.writeString(umlscui);
        }
    }

    public class ConceptGroup implements Parcelable {

        public String tty;

        public ConceptProperties[] conceptProperties;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(tty);
            dest.writeParcelableArray(conceptProperties, flags);
        }

    }

    public class Drugs implements Parcelable {

        public class DrugGroup implements Parcelable {

            public String name;
            public ConceptGroup[] conceptGroup;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(name);
                dest.writeParcelableArray(conceptGroup, flags);
            }
        }

        public DrugGroup drugGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(drugGroup, flags);
        }
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

    public class RelatedGroup implements Parcelable {

        public String rxcui;
        public String[] termType;
        public ConceptGroup[] conceptGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(rxcui);
            dest.writeStringArray(termType);
            dest.writeParcelableArray(conceptGroup, flags);
        }
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

    public class RxConceptProperties implements Parcelable {

        public ConceptProperties properties;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(properties, flags);
        }
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

    public class SpellingSuggestions implements Parcelable {

        public class SuggestionGroup implements Parcelable {

            public class SuggestionList implements Parcelable {

                public String[] suggestion;

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeStringArray(suggestion);
                }
            }

            public String name;
            public SuggestionList suggestionList;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(name);
                dest.writeParcelable(suggestionList, flags);
            }
        }

        public SuggestionGroup suggestionGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(suggestionGroup, flags);
        }
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

    public class TermTypes implements Parcelable {

        public class TermTypeList implements Parcelable {

            public String[] termType;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringArray(termType);
            }
        }

        public TermTypeList termTypeList;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(termTypeList, flags);
        }
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
