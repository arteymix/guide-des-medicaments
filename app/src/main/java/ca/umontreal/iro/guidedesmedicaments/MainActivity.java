package ca.umontreal.iro.guidedesmedicaments;

import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import org.apache.http.client.methods.HttpGet;
import org.diro.rxnav.RxClass;
import org.diro.rxnav.RxNorm;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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

        try {
            HttpResponseCache.install(getCacheDir(), 10 * 1024 * 1024);// 10 MiB
        } catch (IOException ioe) {
            Log.i("", "could not install the HTTP response cache", ioe);
        }

        final SearchView sv = (SearchView) findViewById(R.id.search_drug);

        final RxNorm norm = new RxNorm();
        final RxClass api = new RxClass();

        findViewById(R.id.action_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/131725")));
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new AsyncTask<String, Integer, JSONArray>() {
                    @Override
                    protected JSONArray doInBackground(String... params) {
                        // requête en API pour récupérer des suggestions
                        try {
                            return norm.getApproximateMatch(params[0], 10, 1);
                        } catch (IOException e) {
                            Log.e("", e.getMessage(), e);
                            e.printStackTrace();
                        } catch (JSONException e) {
                            Log.e("", e.getMessage(), e);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(JSONArray result) {
                        if (result != null)
                            sv.setSuggestionsAdapter(new SimpleCursorAdapter(MainActivity.this,
                                    android.R.layout.simple_list_item_1, new JSONArrayCursor(result),
                                    new String[]{"rxcui"}, new int[]{android.R.id.text1}, 0x0));
                    }
                }.execute(newText);

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
            case R.id.action_cart:
                startActivity(new Intent(this, DrugCartActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
