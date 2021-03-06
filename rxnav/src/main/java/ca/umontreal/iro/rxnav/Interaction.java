package ca.umontreal.iro.rxnav;

import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;

/**
 * The Interaction API is a web service for accessing drug-drug interactions. No license is needed
 * to use the Interaction API.
 *
 * @author Guillaume Poirier-Morency
 */
public class Interaction extends RxNav {

    public static Interaction newInstance(OkHttpClient httpClient) {
        return new Interaction(httpClient);
    }

    public Interaction(OkHttpClient httpClient) {
        super(httpClient);
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

        public InteractionConcept[] interactionConcept;
        public String severity;
        public String description;
    }

    public class DrugInteractions {

        public class UserInput {
            public String[] sources;
            public String rxcui;
        }

        public class InteractionTypeGroup {

            public class InteractionType {

                public class MinConceptItem {
                    public String rxcui;
                    public String name;
                    public String tty;
                }

                public String comment;
                public MinConceptItem minConceptItem;
                public InteractionPair[] interactionPair;
            }

            public String sourceDisclaimer;
            public String sourceName;
            public InteractionType[] interactionType;
        }

        public String nlmDisclaimer;
        public UserInput userInput;
        public InteractionTypeGroup[] interactionTypeGroup;
    }

    /**
     * Get the drug interactions for a specified drug.
     *
     * @param rxcui
     * @param sources the sources to use. If not specified, all sources will be used.
     * @return
     * @throws IOException
     */
    public DrugInteractions findDrugInteractions(String rxcui, String... sources) throws IOException {
        return sources.length > 0 ?
                request(DrugInteractions.class, "interaction/interaction",
                        new BasicNameValuePair("rxcui", rxcui),
                        new BasicNameValuePair("sources", StringUtils.join(sources, " "))) :
                request(DrugInteractions.class, "interaction/interaction", new BasicNameValuePair("rxcui", rxcui));
    }

    public class InteractionsFromList {

        public class UserInput {
            public String[] sources;
            public String[] rxcuis;
        }

        public class FullInteractionTypeGroup {

            public class FullInteractionType {

                public class MinConcept {
                    public String rxcui;
                    public String name;
                    public String tty;
                }

                public String comment;
                public MinConcept[] minConcept;
                public InteractionPair[] interactionPair;
            }

            public String sourceDisclaimer;
            public String sourceName;
            public FullInteractionType[] fullInteractionType;
        }

        public String nlmDisclaimer;
        public UserInput userInput;
        public FullInteractionTypeGroup[] fullInteractionTypeGroup;
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
        return sources.length > 0 ?
                request(InteractionsFromList.class, "interaction/list",
                        new BasicNameValuePair("rxcuis", StringUtils.join(rxcuis, " ")),
                        new BasicNameValuePair("propValues", StringUtils.join(sources, " "))) :
                request(InteractionsFromList.class, "interaction/list",
                        new BasicNameValuePair("rxcuis", StringUtils.join(rxcuis, " ")));
    }
}
