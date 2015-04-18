package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Provide search capabilities that initiate the application flow.
 * <p/>
 * Show latest searches and carts.
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

        Set<String> rxcuis = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                .getStringSet("rxcuis", new HashSet<String>());

        findViewById(R.id.action_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/131725")));
            }
        });

        // fetch suggestions
        new AsyncTask<Void, Void, RxNorm.DisplayTerms>() {
            @Override
            protected RxNorm.DisplayTerms doInBackground(Void... params) {
                try {
                    // this request is pretty heavy, but once cached it should be fine.
                    return RxNorm.newInstance(httpClient).getDisplayTerms();
                } catch (IOException e) {
                    Log.e("", e.getMessage(), e);
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(RxNorm.DisplayTerms result) {
                // TODO!
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                startActivity(new Intent(this, DrugCartActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
