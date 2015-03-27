package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.diro.rxnav.Interaction;
import org.json.JSONArray;
import org.json.JSONException;

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

        final Interaction interaction = new Interaction();

        new AsyncTask<String, Integer, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... rxcuids) {
                try {
                    return interaction.findInteractionsFromList(rxcuids, "DRUG");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONArray interactions) {
            }
        }.execute(rxcuis.toArray(new String[rxcuis.size()]));

        // Set up the ViewPager with the sections adapter.
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);

        final List<String> cart = new ArrayList<>(getSharedPreferences("cart", Context.MODE_PRIVATE)
                .getStringSet("rxcuis", new HashSet<String>()));

        final FragmentPagerAdapter pa = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                if (position == 0)
                    return new DrugInteractionFragment();

                return DrugFragment.newInstance(cart.get(position - 1));
            }

            @Override
            public int getCount() {
                return 1 + cart.size();
            }
        };

        pager.setAdapter(pa);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0) {

                    TextView tv = (TextView) findViewById(R.id.drug_name);
                    setTitle(tv.getText());
                } else {
                    setTitle(getString(R.string.title_drug_cart_summary));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    /**
     * Fragment presenting interaction between a set of drugs.
     *
     * @author Guillaume Poirier-Morency
     */
    public static class DrugInteractionFragment extends android.support.v4.app.Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_interaction, container, false);
        }
    }

}
