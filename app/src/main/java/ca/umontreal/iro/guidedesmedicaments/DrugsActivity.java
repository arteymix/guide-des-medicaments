package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

import ca.umontreal.iro.guidedesmedicaments.util.JSONArrayCursor;

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

        List<String> rxcuis = new ArrayList<>();
        ListFragment drugs = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.drugs);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            setTitle("Résultats pour « " + query + " »");
            // TODO: perform a search on the API
        }

        if (intent.hasExtra(RXCUIS)) {
            rxcuis = intent.getStringArrayListExtra(RXCUIS);
            // TODO: afficher une liste prédéfinies de rxcuis
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drugs, menu);
        return true;
    }

}
