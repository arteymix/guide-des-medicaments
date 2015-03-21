package ca.umontreal.iro.guidedesmedicaments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.linearlistview.LinearListView;

import org.diro.rxnav.RxNorm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Present information about a specific drug.
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
        String rxuid = getIntent().getData().getPathSegments().get(2);

        Log.d("", "displaying " + rxuid);

        setContentView(R.layout.activity_drug);

        final TextView drugName = (TextView) findViewById(R.id.drug_name);
        final LinearListView counterIndications = (LinearListView) findViewById(R.id.counter_indications);
        final LinearListView similarDrugs = (LinearListView) findViewById(R.id.similar_drugs);

        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                startActivity(new Intent(view.getContext(), DrugActivity.class));
            }
        });

        final RxNorm api = new RxNorm();

        /**
         * Récupère les données du concept affiché.
         */
        new AsyncTask<String, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... rxcui) {
                try {
                    return api.getRxConceptProperties(rxcui[0]);
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
        }.execute(rxuid);

        /*
        new AsyncTask<Integer, Integer, JSONArray>() {

            @Override
            protected JSONArray doInBackground(Integer... params) {

                try {
                    api.getAllRelatedInfo(params[0]);
                } catch (IOException ioe) {
                    Log.e("", ioe.getMessage(), ioe);
                } catch (JSONException je) {
                    Log.e("", je.getMessage(), je);
                }

                return null;
            }

            @Override
            protected void onPostExecute(JSONArray result) {
                try {

                    JSONArrayCursor jac = new JSONArrayCursor(result);
                    similarDrugs.setAdapter(new SimpleCursorAdapter(DrugActivity.this, R.layout.item_drug, jac, new String[]{}, new int[]{}, 0x0));

                } catch (JSONException jse) {
                    Log.e("", jse.getMessage(), jse);
                }
            }
        }.execute(rxuid);
        */
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

                Log.i("", "about to write " + rxcuids + " for key cart");

                getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("rxcuids", rxcuids)
                        .apply();

                startActivity(new Intent(this, DrugCartActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
