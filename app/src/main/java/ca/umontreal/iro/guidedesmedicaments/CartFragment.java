package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.loader.IOAsyncTaskLoader;
import ca.umontreal.iro.rxnav.Interaction;

/**
 * Fragment presenting a drugs interactions and individual drugs in the cart.
 *
 * @author Guillaume Poirier-Morency
 */
public class CartFragment extends Fragment {

    /**
     * Factory
     *
     * @return
     */
    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.cart_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up the ViewPager with the sections adapter.
        final ViewPager pager = (ViewPager) getView().findViewById(R.id.pager);

        pager.setAdapter(new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                final ArrayList<String> rxcuis = new ArrayList<>(getActivity()
                        .getSharedPreferences(MainActivity.CART, Context.MODE_PRIVATE)
                        .getStringSet(MainActivity.RXCUIS, new HashSet<String>()));

                if (position == 0) {
                    return DrugsFragment.newInstance(rxcuis, "Your cart is empty.");
                }

                if (position == 1)
                    return DrugInteractionFragment.newInstance(rxcuis);

                return DrugFragment.newInstance(rxcuis.get((position - 2)), false);
            }

            @Override
            public int getCount() {
                return getActivity()
                        .getSharedPreferences(MainActivity.CART, Context.MODE_PRIVATE)
                        .getStringSet(MainActivity.RXCUIS, new HashSet<String>()).size() + 2;
            }
        });
    }

    /**
     * Fragment presenting interaction between a set of drugs.
     *
     * @author Guillaume Poirier-Morency
     */
    public static class DrugInteractionFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Interaction.InteractionsFromList> {

        /**
         * Key in the arguments bundle that contains the rxcuis in the {@link Interaction} API
         * request.
         * <p/>
         * This is a mandatory field to instantiate this fragment.
         */
        public static final String RXCUIS = "RXCUIS";

        public static final int INTERACTION_LOADER = 7;

        public static DrugInteractionFragment newInstance(ArrayList<String> rxcuis) {
            DrugInteractionFragment drugInteractionFragment = new DrugInteractionFragment();

            Bundle bundle = new Bundle();
            bundle.putStringArrayList(RXCUIS, rxcuis);

            drugInteractionFragment.setArguments(bundle);

            return drugInteractionFragment;
        }

        private OkHttpClient httpClient;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.interaction_fragment, container, false);
        }

        public void onCreate(Bundle savedInstance) {
            super.onCreate(savedInstance);

            // initialize the HTTp client and setup the cache
            httpClient = new OkHttpClient();
            httpClient.setCache(new com.squareup.okhttp.Cache(getActivity().getCacheDir(), 10 * 1024 * 1024));
        }

        @Override
        public void onActivityCreated(Bundle savedInstance) {
            super.onActivityCreated(savedInstance);

            final ListFragment interactionsList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.interactions);
            interactionsList.setEmptyText("No interaction found.");

            getActivity().getSupportLoaderManager()
                    .initLoader(INTERACTION_LOADER, getArguments(), this)
                    .forceLoad();
        }

        @Override
        public Loader<Interaction.InteractionsFromList> onCreateLoader(int id, final Bundle args) {
            return new IOAsyncTaskLoader<Interaction.InteractionsFromList>(getActivity()) {

                @Override
                public Interaction.InteractionsFromList loadInBackgroundSafely() throws IOException {
                    final ArrayList<String> rxcuis = args.getStringArrayList(RXCUIS);

                    // todo: utiliser un code de couleur (gradation) pour la sévérité
                    return Interaction.newInstance(httpClient)
                            .findInteractionsFromList(rxcuis.toArray(new String[rxcuis.size()]));
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Interaction.InteractionsFromList> loader, Interaction.InteractionsFromList interactions) {
            final TextView nlmDisclaimer = (TextView) getView().findViewById(R.id.nlm_disclaimer);
            final ListFragment interactionsList = (ListFragment) getChildFragmentManager().findFragmentById(R.id.interactions);

            nlmDisclaimer.setText(interactions.nlmDisclaimer);

            if (interactions.fullInteractionTypeGroup == null) {
                // put an empty adapter
                interactionsList.setListAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.interaction_item,
                        new Object[0]));

                return;
            }

            // todo: populate the MatrixCursor
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "drug_1_name", "drug_2_name", "description", "severity"});

            int i = 0;
            for (Interaction.InteractionsFromList.FullInteractionTypeGroup fullInteractionTypeGroup : interactions.fullInteractionTypeGroup) {
                for (Interaction.InteractionsFromList.FullInteractionTypeGroup.FullInteractionType fullInteractionType : fullInteractionTypeGroup.fullInteractionType)
                    for (Interaction.InteractionPair interactionPair : fullInteractionType.interactionPair)
                        matrixCursor.addRow(new Object[]{
                                i++,
                                interactionPair.interactionConcept[0].minConceptItem.name,
                                interactionPair.interactionConcept[1].minConceptItem.name,
                                interactionPair.description,
                                "Severity: " + interactionPair.severity
                        });
            }

            // TODO: assembler toutes les interactions
            interactionsList.setListAdapter(new SimpleCursorAdapter(getActivity(),
                    R.layout.interaction_item, matrixCursor,
                    new String[]{"drug_1_name", "drug_2_name", "description", "severity"},
                    new int[]{R.id.drug_1_name, R.id.drug_2_name, R.id.description, R.id.severity},
                    0x0));
        }

        @Override
        public void onLoaderReset(Loader<Interaction.InteractionsFromList> loader) {

        }
    }
}
