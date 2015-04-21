package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Provide search capabilities that initiate the application flow.
 * <p/>
 * TODO: prefetch rxcuis for suggestions
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class MainActivity extends ActionBarActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final OkHttpClient httpClient = new OkHttpClient();

        httpClient.setCache(new Cache(getCacheDir(), 10 * 1024 * 1024));

        final SearchView sv = (SearchView) findViewById(R.id.search_drug);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        sv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bookmarks:
                Intent showBookmarks = new Intent(this, DrugsActivity.class);

                Set<String> bookmarks = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet());

                if (bookmarks.isEmpty()) {
                    Toast.makeText(this, "You do not have any bookmarks.", Toast.LENGTH_SHORT).show();
                    break;
                }

                showBookmarks.putStringArrayListExtra(DrugsActivity.RXCUIS, new ArrayList<>(bookmarks));

                startActivity(showBookmarks);
                break;
            case R.id.action_search:
                return onSearchRequested();
            case R.id.action_cart:
                startActivity(new Intent(this, DrugCartActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
