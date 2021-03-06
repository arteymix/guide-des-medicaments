package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Present drugs issued from a search or a list of rxcuis like bookmarks.
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class DrugsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drugs);

        // show up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DrugsFragment drugsFragment = (DrugsFragment) getSupportFragmentManager().findFragmentById(R.id.drugs);

        drugsFragment.setEmptyText("No drug matches your request.");
    }

    @Override
    public void onNewIntent(final Intent intent) {
        final DrugsFragment drugsFragment = (DrugsFragment) getSupportFragmentManager().findFragmentById(R.id.drugs);

        if (getIntent().hasExtra(Intent.EXTRA_TITLE))
            setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));

        else if (getIntent().hasExtra(SearchManager.QUERY))
            setTitle("Results for " + getIntent().getStringExtra(SearchManager.QUERY));

        // reload the presented drugs in the fragment
        getSupportLoaderManager()
                .restartLoader(R.id.drugs_loader, intent.getExtras(), drugsFragment)
                .forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drugs, menu);
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

}
