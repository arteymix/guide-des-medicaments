package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linearlistview.LinearListView;

import org.apache.http.message.BasicNameValuePair;
import org.diro.rxnav.RxImageAccess;
import org.diro.rxnav.RxNorm;
import org.diro.rxnav.RxTerms;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
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

        // /REST/rxuid/{rxuid}/...
        final String rxcui = getIntent().getData().getPathSegments().get(2);

        Log.d("", "displaying " + rxcui);

        setContentView(R.layout.activity_drug);

        final TextView drugName = (TextView) findViewById(R.id.drug_name);
        final TextView genericName = (TextView) findViewById(R.id.generic_name);
        final TextView administrationMethod = (TextView) findViewById(R.id.administration_method);
        final LinearListView counterIndications = (LinearListView) findViewById(R.id.counter_indications);
        final LinearListView similarDrugs = (LinearListView) findViewById(R.id.similar_drugs);

        CheckBox bookmark = (CheckBox) findViewById(R.id.bookmark);

        bookmark.setChecked(getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                .getStringSet("rxcuis", new HashSet<String>())
                .contains(rxcui));

        bookmark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Set<String> rxcuis = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet<String>());

                if (isChecked)
                    rxcuis.add(rxcui);
                else
                    rxcuis.remove(rxcui);

                getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("rxcuis", rxcuis)
                        .apply();
            }
        });

        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                startActivity(new Intent(view.getContext(), DrugActivity.class));
            }
        });

        // récupère le nom du concept
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... rxcui) {
                RxNorm norm = new RxNorm();

                try {
                    return norm.getRxConceptProperties(rxcui[0]);
                } catch (IOException ioe) {
                    Log.e("", ioe.getMessage(), ioe);
                } catch (JSONException je) {
                    Log.e("", je.getMessage(), je);
                }
                // already handled though...
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                try {
                    setTitle(result.getString("name"));
                    drugName.setText(result.getString("name"));
                } catch (JSONException jse) {
                    Log.e("", jse.getMessage(), jse);
                }
            }
        }.execute(rxcui);

        /**
         * Fetch potential images for the drug
         */
        new AsyncTask<String, Integer, JSONArray>() {

            @Override
            protected JSONArray doInBackground(String... params) {
                RxImageAccess image = new RxImageAccess();

                try {
                    // TODO: use the rxcui to identify the images
                    return image.rxbase(new BasicNameValuePair("name", "aspirin"));
                } catch (IOException e) {
                    Log.e("", e.getMessage(), e);
                } catch (JSONException e) {
                    Log.e("", e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONArray images) {
                if (images == null)
                    return; // TODO: could not fetch images

                final ImageView drugIcon = (ImageView) findViewById(R.id.drug_icon);

                final Uri[] imageUris = new Uri[images.length()];

                try {
                    // charge l'icône du médicament
                    Glide.with(DrugActivity.this)
                            .load(images.getJSONObject(0).getString("imageUrl"))
                            .placeholder(android.R.drawable.spinner_background)
                            .into(drugIcon);

                    for (int i = 0; i < images.length(); i++)
                        imageUris[i] = Uri.parse(images.getJSONObject(i).getString("imageUrl"));
                } catch (JSONException e) {
                    Log.e("", "could not extract 'imageUrl' from the RxImageAccess api", e);
                }

                // au clic, lancer la galerie avec tous les images du médicament
                drugIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // display images in a gallery
                        Intent i = new Intent(Intent.ACTION_VIEW);

                        // TODO: put all images in the intent to display a nice gallery
                        i.setDataAndType(imageUris[0], "image/jpeg");

                        startActivity(i);
                    }
                });
            }
        }.execute(rxcui);
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
