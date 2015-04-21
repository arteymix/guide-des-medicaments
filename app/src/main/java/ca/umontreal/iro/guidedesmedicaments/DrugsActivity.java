package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.OkHttpClient;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.loader.IOAsyncTaskLoader;
import ca.umontreal.iro.rxnav.RxImageAccess;
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

    public static final int DRUGS_LOADER = 0;

    /**
     * Intent key to present a list of concepts from their rxcui identifier.
     */
    public static final String RXCUIS = "rxcuis";

    /**
     * HTTP client reused over requests.
     */
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drugs);

        // initialize the client for reuse over intents
        httpClient = new OkHttpClient();
        httpClient.setCache(new com.squareup.okhttp.Cache(getCacheDir(), 10 * 1024 * 1024));

        getSupportLoaderManager()
                .initLoader(DRUGS_LOADER, getIntent().getExtras(), new DrugsLoaderCallbacks())
                .forceLoad();
    }

    @Override
    public void onNewIntent(final Intent intent) {
        getSupportLoaderManager()
                .restartLoader(DRUGS_LOADER, intent.getExtras(), new DrugsLoaderCallbacks())
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
            case R.id.action_cart:
                startActivity(new Intent(this, DrugCartActivity.class));
                break;
            case R.id.action_search:
                return onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Load drugs into the drugs {@link ListFragment} from a search or a predefined set of rxcuis.
     * <p/>
     * It will automatically lookup the providen {@link Bundle} to perform the right operation.
     */
    public class DrugsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<RxNorm.Rxcui>> {

        @Override
        public Loader<List<RxNorm.Rxcui>> onCreateLoader(int id, final Bundle args) {
            return new IOAsyncTaskLoader<List<RxNorm.Rxcui>>(DrugsActivity.this) {

                @Override
                public List<RxNorm.Rxcui> loadInBackgroundSafely() throws IOException {
                    List<RxNorm.Rxcui> rxcuis = new ArrayList<>();

                    // search from the query
                    if (args.containsKey(SearchManager.QUERY)) {
                        setTitle("Results for " + args.getString(SearchManager.QUERY));
                        for (String term : args.getString(SearchManager.QUERY).split("[\\s]")) {
                            RxNorm.SpellingSuggestions spellingSuggestions = RxNorm.newInstance(httpClient).getSpellingSuggestions(term);
                            if (spellingSuggestions.suggestionGroup.suggestionList == null)
                                continue; // no suggestions for this term
                            for (String suggestedTerm : spellingSuggestions.suggestionGroup.suggestionList.suggestion)
                                rxcuis.add(RxNorm.newInstance(httpClient).findRxcuiByString(suggestedTerm, null, true, RxNorm.NORMALIZED)); // normalized search
                        }
                    }

                    // fetch extra rxcuis as well
                    if (args.containsKey(RXCUIS)) {
                        setTitle(getString(R.string.bookmarks));
                        for (String rxcui : args.getStringArrayList(RXCUIS))
                            rxcuis.add(RxNorm.newInstance(httpClient).findRxcuiByString(rxcui, null, true, RxNorm.EXACT_MATCH));
                    }

                    return rxcuis;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<RxNorm.Rxcui>> loader, final List<RxNorm.Rxcui> rxcuis) {
            // generally a network error (already logged)
            if (rxcuis == null) {
                Toast.makeText(DrugsActivity.this, "Error while accessing the RxNav API.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            if (rxcuis.isEmpty()) {
                Toast.makeText(DrugsActivity.this, "No matches for the provided query.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            final ListFragment drugs = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.drugs);

            // setTitle("Results for " + rxcuis);

            // define a custom adapter
            drugs.setListAdapter(new ArrayAdapter<RxNorm.Rxcui>(DrugsActivity.this,
                    R.layout.drug_item,
                    R.id.drug_name,
                    rxcuis) {

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    final View c = super.getView(position, convertView, parent);

                    TextView drugName = (TextView) c.findViewById(R.id.drug_name);

                    drugName.setText(rxcuis.get(position).idGroup.name);

                    // fetch the image
                    new AsyncTask<String, Void, RxImageAccess.ImageAccess>() {

                        @Override
                        protected RxImageAccess.ImageAccess doInBackground(String... name) {
                            try {
                                // TODO: use the rxcui to identify the images
                                return RxImageAccess.newInstance(httpClient).rxbase(new BasicNameValuePair("name", name[0]));
                            } catch (IOException e) {
                                Log.e("", e.getMessage(), e);
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(RxImageAccess.ImageAccess image) {
                            if (image.replyStatus.imageCount == 0)
                                return;

                            final ImageView drugIcon = (ImageView) c.findViewById(R.id.drug_icon);

                            // charge l'icône du médicament
                            Glide.with(DrugsActivity.this)
                                    .load(image.nlmRxImages[0].imageUrl)
                                    .crossFade()
                                    .fitCenter()
                                    .centerCrop()
                                    .into(drugIcon);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rxcuis.get(position).idGroup.name);

                    // todo: bookmarking live!
                    // c.findViewById(R.id.bookmark).setOnClickListener();

                    // présente le médicament au clic
                    c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (rxcuis.get(position).idGroup.rxnormId == null) {
                                Log.w("", "no rxcui matching drug name " + rxcuis.get(position).idGroup.name);
                                Toast.makeText(DrugsActivity.this, "Could not identify this drug (no matching rxcui).", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // show the magnificent drug!
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + rxcuis.get(position).idGroup.rxnormId[0])));
                        }
                    });

                    return c;
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<List<RxNorm.Rxcui>> loader) {

        }

    }

}
