package ca.umontreal.iro.rxnav;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * The RxClass API is a web service for accessing drug classes and drug members for a number of
 * different drug class types. No license is needed to use the RxClass API.
 * <p/>
 *
 * @author Guillaume Poirier-Morency
 */
public class RxClass extends RxNav {

    public static RxClass newInstance(OkHttpClient httpClient) {
        return new RxClass(httpClient);
    }

    public RxClass(OkHttpClient httpClient) {
        super(httpClient);
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
     * Retrieve class information from a class identifier.
     * <p/>
     * http://rxnav.nlm.nih.gov/RxClassAPIs.html#uLink=RxClass_REST_findClassById
     *
     * @param classId the class identifier.
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONArray findClassById(String classId) throws IOException, JSONException {
        return get("class/byId", new BasicNameValuePair("classId", classId))
                .getJSONObject("rxclassMinConceptList")
                .getJSONArray("rxclassMinConcept");
    }

    public class ClassByRxNormDrugId {

        public class UserInput {

            public String relaSource;
            public String relas;
            public String rxcui;
        }

        public class RxClassDrugInfoList {

            public class RxClassDrugInfo {

                public class RxClassMinConceptItem {

                    public String classId;
                    public String className;
                    public String classType;
                }

                public Interaction.InteractionTypeGroup.InteractionType.MinConceptItem minConcept;
                public RxClassMinConceptItem rxclassMinConceptItem;
                public String rela;
                public String relaSource;
            }

            public RxClassDrugInfo[] rxclassDrugInfo;
        }

        public UserInput userInput;
        public RxClassDrugInfoList rxclassDrugInfoList;
    }

    /**
     * Get the classes of a RxNorm drug identifier. The user can limit the classes returned by
     * specifying a list of sources of drug-class relations, as well as a list of relations.
     *
     * @param rxcui      the RxNorm identifier (RxCUI) of the drug. This must be an identifier for
     *                   an ingredient, precise ingredient or multiple ingredient.
     * @param relaSource (optional) a source of drug-class relationships. See /relaSources for the
     *                   list of sources of drug-class relations. If this field is omitted, all
     *                   sources of drug-class relationships will be used.
     * @param relas      (optional) a list of relationships of the drug to the class. This field is
     *                   ignored if relaSource is not specified.
     * @return
     * @throws IOException
     */
    public ClassByRxNormDrugId getClassByRxNormDrugId(String rxcui, String relaSource, String... relas) throws IOException {
        return relaSource == null ?
                request(ClassByRxNormDrugId.class, "rxclass/class/byRxcui", new BasicNameValuePair("rxcui", rxcui)) :
                request(ClassByRxNormDrugId.class, "rxclass/class/byRxcui",
                        new BasicNameValuePair("rxcui", rxcui),
                        new BasicNameValuePair("relaSource", relaSource),
                        new BasicNameValuePair("relas", StringUtils.join(relas, " ")));
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
