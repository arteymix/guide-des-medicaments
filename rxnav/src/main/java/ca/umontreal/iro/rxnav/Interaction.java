package ca.umontreal.iro.rxnav;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * The Interaction API is a web service for accessing drug-drug interactions. No license is needed
 * to use the Interaction API.
 *
 * @author Guillaume Poirier-Morency
 */
public class Interaction extends RxNav {

    public static Interaction newInstance() {
        return new Interaction();
    }

    public class InteractionTypeGroup {

        public class InteractionType {

            public class MinConcept {
                public String rxcui;
                public String name;
                public String tty;
            }

            public class InteractionPair {

                public class InteractionConcept {

                    public class MinConceptItem {
                        public String rxcui;
                        public String name;
                        public String tty;
                    }

                    public class SourceConceptItem {
                        public String id;
                        public String name;
                        public String url;
                    }

                    public MinConceptItem minConceptItem;
                    public SourceConceptItem sourceConceptItem;
                }

                public List<InteractionConcept> interactionConcept;
                public String description;
            }

            public String comment;
        }

        public String sourceDisclaimer;
        public List<InteractionType> interactionType;
    }

    public class DrugInteractions implements Parcelable {

        public class UserInput implements Parcelable {
            public List<String> sources;
            public String rxcui;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeStringList(sources);
                dest.writeString(rxcui);
            }
        }

        public String nlmDisclaimer;
        public UserInput userInput;
        public List<InteractionTypeGroup> interactionTypeGroup;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    /**
     * Get the drug interactions for a specified drug.
     *
     * @param rxcui
     * @return
     * @throws IOException
     */
    public DrugInteractions findDrugInteractions(String rxcui, String... sources) throws IOException {
        final HttpURLConnection connection = sources.length > 0 ?
                getHttpConnection("interaction/interaction",
                        new BasicNameValuePair("rxcui", rxcui),
                        new BasicNameValuePair("sources", StringUtils.join(sources, " "))) :
                getHttpConnection("interaction/interaction", new BasicNameValuePair("rxcui", rxcui));

        try {
            return gson.fromJson(new InputStreamReader(connection.getInputStream()), DrugInteractions.class);
        } finally {
            connection.disconnect();
        }

    }

    public class InteractionsFromList {

        public class UserInput {
            public List<String> sources;
            public List<String> rxcuis;
        }

        public String nlmDisclaimer;
        public UserInput userInput;
        public InteractionTypeGroup fullInteractionTypeGroup;
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
     */
    public InteractionsFromList findInteractionsFromList(String[] rxcuis, String... sources) throws IOException {
        HttpURLConnection connection = sources.length > 0 ?
                getHttpConnection("interaction/list",
                        new BasicNameValuePair("rxcuis", StringUtils.join(rxcuis, " ")),
                        new BasicNameValuePair("propValues", StringUtils.join(sources, " "))) :
                getHttpConnection("interaction/list", new BasicNameValuePair("rxcuis", StringUtils.join(rxcuis, " ")));

        try {
            return gson.fromJson(new InputStreamReader(connection.getInputStream()), InteractionsFromList.class);
        } finally {
            connection.disconnect();
        }
    }
}
