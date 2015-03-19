package ca.umontreal.iro.guidedesmedicaments;

import android.net.http.AndroidHttpClient;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

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
            HttpResponseCache.install(new File(getCacheDir(), "http"), 10 * 1024 * 1024);// 10 MiB
        } catch (IOException ioe) {
            Log.i("", "could not install the HTTP response cache", ioe);
        }

        final SearchView sv = (SearchView) findViewById(R.id.search_drug);

        final RxClass api = new RxClass(AndroidHttpClient.newInstance("", this));

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
                            return api.allClasses(params);
                        } catch (IOException e) {
                            Log.e("", e.getMessage(), e);
                            e.printStackTrace();
                        } catch (JSONException e) {
                            Log.e("", e.getMessage(), e);
                        } catch (URISyntaxException e) {
                            Log.e("", e.getMessage(), e);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(JSONArray result) {
                        if (result != null)
                            sv.setSuggestionsAdapter(new SimpleCursorAdapter(MainActivity.this,
                                    android.R.layout.simple_list_item_1, new JSONArrayCursor(result),
                                    new String[]{"className"}, new int[]{android.R.id.text1}, 0x0));
                    }
                }.execute(newText);

                return false;
            }
        });


    }

}
