package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashSet;
import java.util.Set;

/**
 * Present information about a specific drug.
 * <p/>
 * List similar counter-indications and similar/related concepts.
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class DrugActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug);

        // /REST/rxuid/{rxuid}/...
        Log.i("", "loading drug with rxuid" + getIntent().getData().getPathSegments().get(2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return onSearchRequested();
            case R.id.action_cart:
                // add to cart!
                Set<String> rxcuis = getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet<String>());

                rxcuis.add(getIntent().getData().getPathSegments().get(2));

                getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("rxcuis", rxcuis)
                        .apply();

                startActivity(new Intent(this, DrugCartActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
