package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

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

        final ListView drugs = (ListView) findViewById(R.id.drugs);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);

            new AsyncTask<String, Void, RxNorm.SpellingSuggestions>() {

                @Override
                protected RxNorm.SpellingSuggestions doInBackground(String... params) {
                    try {
                        return RxNorm.newInstance().getSpellingSuggestions(params[0]);
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

                    Log.i("", "" + spellingSuggestions.suggestionGroup.suggestionList.suggestion);

                    setTitle("Results for " + spellingSuggestions.suggestionGroup.name);

                    drugs.setAdapter(new ArrayAdapter<String>(DrugsActivity.this,
                            R.layout.drug_item,
                            spellingSuggestions.suggestionGroup.suggestionList.suggestion) {

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            final View c = getLayoutInflater().inflate(R.layout.drug_item, parent, false);

                            TextView drugName = (TextView) c.findViewById(R.id.drug_name);

                            drugName.setText(spellingSuggestions.suggestionGroup.suggestionList.suggestion[position]);

                            // récupération de la vignette
                            new AsyncTask<String, Integer, Uri>() {

                                @Override
                                protected Uri doInBackground(String... params) {
                                    RxImageAccess image = new RxImageAccess();

                                    try {
                                        // TODO: use the rxcui to identify the images
                                        return Uri.parse(image.rxbase(new BasicNameValuePair("name", params[0]))
                                                .getJSONObject(0)
                                                .getString("imageUrl"));

                                    } catch (IOException e) {
                                        Log.e("", e.getMessage(), e);
                                    } catch (JSONException e) {
                                        Log.e("", e.getMessage(), e);
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Uri image) {
                                    if (image == null)
                                        return; // TODO: could not fetch images

                                    final ImageView drugIcon = (ImageView) c.findViewById(R.id.drug_icon);

                                    // charge l'icône du médicament
                                    Glide.with(DrugsActivity.this)
                                            .load(image)
                                            .crossFade()
                                            .fitCenter()
                                            .centerCrop()
                                            .into(drugIcon);
                                }
                            }.execute(spellingSuggestions.suggestionGroup.suggestionList.suggestion[position]);

                            // récupération de la vignette
                            new AsyncTask<String, Void, Bitmap>() {

                                @Override
                                protected Bitmap doInBackground(String... params) {
                                    try {
                                        RxImageAccess.newInstance().rxnav(new BasicNameValuePair("name", params[0]));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute(spellingSuggestions.suggestionGroup.suggestionList.suggestion[position]);

                            // todo: bookmarking live!
                            // c.findViewById(R.id.bookmark).setOnClickListener();

                            return c;
                        }
                    });

                    drugs.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // todo: faire correspond le champs id au rxcui
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + id)));
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

}
