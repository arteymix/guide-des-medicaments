package org.diro.rxnav;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
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
        public List<Candidate> candidate;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(inputTerm);
            dest.writeString(maxEntries);
            dest.writeString(comment);
            dest.writeParcelableArray(candidate.toArray(new Candidate[candidate.size()]), flags);
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
     * @throws JSONException
     */
    public ApproximateGroup getApproximateMatch(String term, int maxEntries, int option) throws IOException, JSONException {
        final HttpURLConnection connection = getHttpConnection("approximateTerm",
                new BasicNameValuePair("term", term),
                new BasicNameValuePair("maxEntries", Integer.toString(maxEntries)),
                new BasicNameValuePair("option", Integer.toString(option)));

        try {
            return this.gson.fromJson(new InputStreamReader(connection.getInputStream()), ApproximateGroup.class);
        } finally {
            connection.disconnect();
        }
    }

    /**
     *
     */
    public class DisplayTerms implements Parcelable {

        public class DisplayTermsList implements Parcelable {
            public List<String> term;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringList(term);
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
     * @throws JSONException
     */
    public DisplayTerms getDisplayTerms() throws IOException {
        final HttpURLConnection connection = getHttpConnection("displaynames");

        try {
            return this.gson.fromJson(new InputStreamReader(connection.getInputStream()), DisplayTerms.class);
        } finally {
            connection.disconnect();
        }
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

        public List<ConceptProperties> conceptProperties;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(tty);
            dest.writeParcelableArray(conceptProperties.toArray(new ConceptProperties[conceptProperties.size()]), flags);
        }

    }

    public class Drugs implements Parcelable {

        public class DrugGroup implements Parcelable {

            public String name;
            public List<ConceptGroup> conceptGroup;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(name);
                dest.writeParcelableArray(conceptGroup.toArray(new ConceptGroup[conceptGroup.size()]), flags);
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
        HttpURLConnection connection = getHttpConnection("drugs", new BasicNameValuePair("name", name));

        try {
            return this.gson.fromJson(new InputStreamReader(connection.getInputStream()), Drugs.class);
        } finally {
            connection.disconnect();
        }
    }

    public class RelatedGroup implements Parcelable {

        public String rxcui;
        public List<String> termType;
        public List<ConceptGroup> conceptGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(rxcui);
            dest.writeStringList(termType);
            dest.writeParcelableArray(conceptGroup.toArray(new ConceptGroup[conceptGroup.size()]), flags);
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
    public RelatedGroup getRelatedByType(String rxcui, String... tty) throws IOException, JSONException {
        HttpURLConnection connection = getHttpConnection("rxcui/" + rxcui + "/allrelated");

        try {
            return gson.fromJson(new InputStreamReader(connection.getInputStream()), RelatedGroup.class);
        } finally {
            connection.disconnect();
        }
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
        HttpURLConnection connection = getHttpConnection("rxcui/" + rxcui + "/properties");
        try {
            return gson.fromJson(new InputStreamReader(connection.getInputStream()), RxConceptProperties.class);
        } finally {
            connection.disconnect();
        }
    }
}
