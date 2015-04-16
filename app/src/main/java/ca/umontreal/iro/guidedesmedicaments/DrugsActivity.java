package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;

import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Present drugs issued from a search or a list of rxcuis.
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class DrugsActivity extends ActionBarActivity {

    /**
     * Intent key to present a list of concepts from their rxcui identifier.
     */
    public final String RXCUIS = "rxcuis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drugs);

        final ListFragment drugs = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.drugs);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);

            new AsyncTask<String, Void, RxNorm.SpellingSuggestions>() {

                @Override
                protected RxNorm.SpellingSuggestions doInBackground(String... params) {
                    try {
                        return RxNorm.newInstance().getSpellingSuggestions(params[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(RxNorm.SpellingSuggestions spellingSuggestions) {

                    if (spellingSuggestions.suggestionGroup.suggestionList == null) {
                        Toast.makeText(DrugsActivity.this, "No matches for " + spellingSuggestions.suggestionGroup.name + ".",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    Log.i("", "" + spellingSuggestions.suggestionGroup.suggestionList.suggestion);

                    setTitle("Results for " + spellingSuggestions.suggestionGroup.name);

                    drugs.setListAdapter(new ArrayAdapter<>(DrugsActivity.this,
                            android.R.layout.simple_list_item_1,
                            spellingSuggestions.suggestionGroup.suggestionList.suggestion));
                }
            }.execute(query.split(" "));
        }

        if (intent.hasExtra(RXCUIS)) {
            // TODO: afficher une liste prédéfinies de rxcuis
            drugs.setListAdapter(new ArrayAdapter<>(DrugsActivity.this,
                    android.R.layout.simple_list_item_1,
                    intent.getStringArrayListExtra(RXCUIS)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drugs, menu);
        return true;
    }

}
