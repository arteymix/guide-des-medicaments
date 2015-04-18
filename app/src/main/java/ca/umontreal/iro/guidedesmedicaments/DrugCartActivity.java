package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.cache.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.tobishiba.circularviewpager.library.BaseCircularViewPagerAdapter;
import com.tobishiba.circularviewpager.library.CircularViewPagerHandler;

import org.apache.commons.lang3.ArrayUtils;

import ca.umontreal.iro.rxnav.Interaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.concepts.DrugFragment;

/**
 * Present the drug cart and its content using a {@link android.support.v4.view.ViewPager}.
 * <p/>
 * The first page presents a summary of all drug carts and subsequent pages (swipe right) present
 * each cart individually.
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class DrugCartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_cart);

        final Set<String> rxcuis = getSharedPreferences("cart", Context.MODE_PRIVATE)
                .getStringSet("rxcuis", new HashSet<String>());

        Log.i("", "rxcuis dans le panier " + rxcuis);

        // Set up the ViewPager with the sections adapter.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        final List<String> cart = new ArrayList<String>();

        // first page contains the interactions
        cart.add(null);

        cart.addAll(rxcuis);

        pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return (position == 0) ?
                        DrugInteractionFragment.newInstance(rxcuis) :
                        DrugFragment.newInstance(cart.get(position % cart.size()));
            }

            @Override
            public int getCount() {
                return cart.size();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment presenting interaction between a set of drugs.
     *
     * @author Guillaume Poirier-Morency
     */
    public static class DrugInteractionFragment extends android.support.v4.app.Fragment {

        /**
         * Key in the arguments bundle that contains the rxcuis in the {@link Interaction} API
         * request.
         * <p/>
         * This is a mandatory field to instantiate this fragment.
         */
        public static final String RXCUIS = "RXCUIS";

        public static DrugInteractionFragment newInstance(Set<String> rxcuis) {
            DrugInteractionFragment drugInteractionFragment = new DrugInteractionFragment();

            Bundle bundle = new Bundle();
            bundle.putStringArray(RXCUIS, rxcuis.toArray(new String[rxcuis.size()]));

            drugInteractionFragment.setArguments(bundle);

            return drugInteractionFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.interaction_fragment, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstance) {
            super.onActivityCreated(savedInstance);

            final OkHttpClient httpClient = new OkHttpClient();

            httpClient.setCache(new com.squareup.okhttp.Cache(getActivity().getCacheDir(), 10 * 1024 * 1024));

            final TextView nlmDisclaimer = (TextView) getView().findViewById(R.id.nlm_disclaimer);
            final ListView interactionsList = (ListView) getView().findViewById(R.id.interactions);

            String[] rxcuis = getArguments().getStringArray(RXCUIS);

            if (rxcuis.length == 0)
                Toast.makeText(getActivity(), "The cart is empty, add some drugs in it first.",
                        Toast.LENGTH_SHORT).show();

            new AsyncTask<String, Void, Interaction.InteractionsFromList>() {

                @Override
                protected Interaction.InteractionsFromList doInBackground(String... rxcuis) {
                    try {
                        // todo: utiliser un code de couleur (gradation) pour la sévérité
                        return Interaction.newInstance(httpClient).findInteractionsFromList(rxcuis);
                    } catch (IOException e) {
                        Log.e("", e.getMessage(), e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Interaction.InteractionsFromList interactions) {
                    nlmDisclaimer.setText(interactions.nlmDisclaimer);

                    if (interactions.fullInteractionTypeGroup == null) {
                        // todo: avertir  l'usager qu'aucune interactions a été trouvée
                        Toast.makeText(getActivity(), "No drug interactions found.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: assembler toutes les interactions
                    interactionsList.setAdapter(new ArrayAdapter<>(
                            getActivity(),
                            R.layout.interaction_item,
                            interactions.fullInteractionTypeGroup.interactionType));
                }
            }.execute(rxcuis);
        }
    }
}
