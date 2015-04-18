package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.OkHttpClient;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ca.umontreal.iro.rxnav.RxImageAccess;
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

        final OkHttpClient httpClient = new OkHttpClient();

        httpClient.setCache(new com.squareup.okhttp.Cache(getCacheDir(), 10 * 1024 * 1024));

        final ListView drugs = (ListView) findViewById(R.id.drugs);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);

            new AsyncTask<String, Void, RxNorm.SpellingSuggestions>() {

                @Override
                protected RxNorm.SpellingSuggestions doInBackground(String... params) {
                    try {
                        return RxNorm.newInstance(httpClient).getSpellingSuggestions(params[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(final RxNorm.SpellingSuggestions spellingSuggestions) {
                    if (spellingSuggestions.suggestionGroup.suggestionList == null) {
                        Toast.makeText(DrugsActivity.this, "No matches for " + spellingSuggestions.suggestionGroup.name + ".",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    setTitle("Results for " + spellingSuggestions.suggestionGroup.name);

                    drugs.setAdapter(new ArrayAdapter<String>(DrugsActivity.this,
                            R.layout.drug_item,
                            R.id.drug_name,
                            spellingSuggestions.suggestionGroup.suggestionList.suggestion) {

                        @Override
                        public View getView(final int position, View convertView, ViewGroup parent) {
                            final View c = super.getView(position, convertView, parent);

                            c.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // récupération de l'id
                                    // todo: utiliser des rxcui comme id

                                    new AsyncTask<String, Void, RxNorm.Rxcui>() {

                                        @Override
                                        protected RxNorm.Rxcui doInBackground(String... params) {
                                            try {
                                                return RxNorm.newInstance(httpClient).findRxcuiByString(params[0], new String[]{}, false, 0);
                                            } catch (IOException e) {
                                                Log.e("", e.getLocalizedMessage(), e);
                                                return null;
                                            }
                                        }

                                        protected void onPostExecute(RxNorm.Rxcui rxcui) {
                                            if (rxcui.idGroup.rxnormId != null)
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + rxcui.idGroup.rxnormId[0])));
                                        }
                                    }.execute(spellingSuggestions.suggestionGroup.suggestionList.suggestion[position]);
                                }
                            });

                            // récupération de la vignette
                            new AsyncTask<String, Void, RxImageAccess.ImageAccess>() {

                                @Override
                                protected RxImageAccess.ImageAccess doInBackground(String... params) {
                                    try {
                                        // TODO: use the rxcui to identify the images
                                        return RxImageAccess.newInstance(httpClient).rxbase(new BasicNameValuePair("name", params[0]));
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
                            }.execute(spellingSuggestions.suggestionGroup.suggestionList.suggestion[position]);

                            // todo: bookmarking live!
                            // c.findViewById(R.id.bookmark).setOnClickListener();

                            return c;
                        }
                    });
                }
            }.execute(query.split(" "));
        }

        // affiche une liste prédéfinie (généralement les bookmarks)
        if (intent.hasExtra(RXCUIS)) {
            // TODO: afficher une liste prédéfinies de rxcuis
            drugs.setAdapter(new ArrayAdapter<>(DrugsActivity.this,
                    R.layout.drug_item,
                    intent.getStringArrayListExtra(RXCUIS)));
        }
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
            case R.id.action_cart:
                // add to cart!
                Set<String> rxcuids = getSharedPreferences("cart", Context.MODE_PRIVATE).getStringSet("rxcuids", new HashSet<String>());

                rxcuids.add(getIntent().getData().getPathSegments().get(2));

                getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("rxcuis", rxcuids)
                        .apply();

                startActivity(new Intent(this, DrugCartActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
