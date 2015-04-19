package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import ca.umontreal.iro.guidedesmedicaments.loaders.RxNavAsyncTaskLoader;
import ca.umontreal.iro.rxnav.RxNorm;

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

    public static final int SUGGESTION_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final OkHttpClient httpClient = new OkHttpClient();

        httpClient.setCache(new Cache(getCacheDir(), 10 * 1024 * 1024));

        final Trie<String, Long> termsTrie = new PatriciaTrie();

        final SearchView sv = (SearchView) findViewById(R.id.search_drug);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        sv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // charge les termes dans la trie
        getSupportLoaderManager().initLoader(SUGGESTION_LOADER, null, new LoaderManager.LoaderCallbacks<RxNorm.DisplayTerms>() {
            @Override
            public Loader<RxNorm.DisplayTerms> onCreateLoader(int id, Bundle args) {

                return new RxNavAsyncTaskLoader<RxNorm, RxNorm.DisplayTerms>(MainActivity.this, RxNorm.newInstance(httpClient)) {

                    @Override
                    public RxNorm.DisplayTerms loadInBackgroundSafely() throws IOException {
                        return rxNav.getDisplayTerms();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxNorm.DisplayTerms> loader, RxNorm.DisplayTerms data) {
                // populate the index
                for (int i = 0; i < data.displayTermsList.term.length; i++) {
                    termsTrie.put(data.displayTermsList.term[i], (long) i);
                }
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.DisplayTerms> loader) {

            }
        }).forceLoad();

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SortedMap<String, Long> matchingTerms = termsTrie.prefixMap(newText);

                MatrixCursor matrixCursor = new MatrixCursor(
                        new String[]{"_id", "term"},
                        matchingTerms.size()); // avoid resize :P

                for (Map.Entry<String, Long> e : matchingTerms.entrySet()) {
                    matrixCursor.addRow(new Object[]{e.getValue(), e.getKey()});
                }

                // fetch from the index!
                sv.setSuggestionsAdapter(new SimpleCursorAdapter(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        matrixCursor,
                        new String[]{"term"},
                        new int[]{android.R.id.text1},
                        0x0));

                return false;
            }
        });
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
