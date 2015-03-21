package ca.umontreal.iro.guidedesmedicaments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.diro.rxnav.RxTerms;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int rxuid = Integer.parseInt(getIntent().getData().getPathSegments().get(2));

        Log.d("", "displaying " + rxuid);

        setContentView(R.layout.activity_drug);

        final TextView drugName = (TextView) findViewById(R.id.drug_name);
        final TextView genericName = (TextView) findViewById(R.id.generic_name);
        final TextView administrationMethod = (TextView) findViewById(R.id.administration_method);
        final LinearListView counterIndications = (LinearListView) findViewById(R.id.counter_indications);
        final LinearListView similarDrugs = (LinearListView) findViewById(R.id.similar_drugs);

        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                startActivity(new Intent(view.getContext(), DrugActivity.class));
            }
        });

        final RxNorm norm = new RxNorm();
        final RxTerms terms = new RxTerms();

        /**
         * Récupère les données du concept affiché.
         */
        new AsyncTask<Integer, Integer, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Integer... rxcui) {
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
                startActivity(new Intent(this, DrugCartActivity.class));
            case R.id.action_search:
                return onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

}
