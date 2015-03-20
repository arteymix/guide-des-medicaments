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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import com.linearlistview.LinearListView;

import org.apache.http.impl.client.DefaultHttpClient;
import org.diro.rxnav.RxClass;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
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

        setTitle("Aspirin");
        setContentView(R.layout.activity_drug);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        findViewById(R.id.drug_description).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Description")
                        .setMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum")
                        .show();
            }
        });

        LinearListView counterIndications = (LinearListView) findViewById(R.id.counter_indications);

        counterIndications.setAdapter(new SimpleAdapter(this, new ArrayList<Map<String, String>>(), android.R.layout.simple_list_item_1, new String[0], new int[0]));

        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> entry = new HashMap<>();
        entry.put("drug_name", "Aspirin");

        for (int i = 0; i < 10; i++)
            data.add(entry);

        String[] stringIds = {"drug_name"};
        int[] intIds = {R.id.drug_name};

        final LinearListView similarDrugs = (LinearListView) findViewById(R.id.similar_drugs);
        similarDrugs.setAdapter(new SimpleAdapter(this, data, R.layout.item_drug, stringIds, intIds));

        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {

            @Override
            public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                startActivity(new Intent(view.getContext(), DrugActivity.class));

            }
        });

        final RxClass api = new RxClass();

        new AsyncTask<String, Integer, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... classTypes) {
                try {
                    return api.allClasses();
                } catch (IOException ioe) {
                    Log.e("", "", ioe);
                    // shit?
                } catch (JSONException je) {
                    Log.e("", "", je);
                    // damn...
                }

                // already handled though...
                return null;
            }

            @Override
            protected void onPostExecute(JSONArray result) {
                String[] fromIds = {"className"};
                int[] toIds = {R.id.drug_name};
                similarDrugs.setAdapter(new SimpleCursorAdapter(DrugActivity.this, R.layout.item_drug, new JSONArrayCursor(result), fromIds, toIds));
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_medicament, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return onSearchRequested();

        }
        return super.onOptionsItemSelected(item);
    }

}
