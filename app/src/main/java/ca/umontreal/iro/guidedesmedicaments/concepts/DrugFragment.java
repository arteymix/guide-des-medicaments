package ca.umontreal.iro.guidedesmedicaments.concepts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.http.message.BasicNameValuePair;
import org.diro.rxnav.RxImageAccess;
import org.diro.rxnav.RxNorm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.R;
import ca.umontreal.iro.guidedesmedicaments.util.JSONArrayCursor;

/**
 * Fragment presenting a drug concept.
 * <p/>
 * If this is not embedded in the the {@link ca.umontreal.iro.guidedesmedicaments.DrugActivity}, the
 * "rxcui" key has to be set in the fragment arguments.
 * <p/>
 * The layout is embedded in a {@link android.widget.ScrollView} and will expand after the screen
 * height.
 * <p/>
 * TODO: extract and present the missing information from the RxNav API.
 *
 * @author Guillaume Poirier-Morency
 */
public class DrugFragment extends Fragment {

    public static DrugFragment newInstance(String rxcui) {
        Bundle bundle = new Bundle();
        bundle.putString("rxcui", rxcui);

        DrugFragment drugFragment = new DrugFragment();
        drugFragment.setArguments(bundle);

        return drugFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.drug_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String rxcui = getArguments() == null ?
                getActivity().getIntent().getData().getPathSegments().get(2) :
                getArguments().getString("rxcui");

        final TextView drugName = (TextView) getView().findViewById(R.id.drug_name);
        final TextView genericName = (TextView) getView().findViewById(R.id.generic_name);
        final TextView administrationMethod = (TextView) getView().findViewById(R.id.administration_method);

        final ListFragment counterIndications = (ListFragment) getFragmentManager().findFragmentById(R.id.counter_indications);
        final ListFragment similarDrugs = (ListFragment) getFragmentManager().findFragmentById(R.id.similar_drugs);

        CheckBox bookmark = (CheckBox) getView().findViewById(R.id.bookmark);

        bookmark.setChecked(getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                .getStringSet("rxcuis", new HashSet<String>())
                .contains(rxcui));

        bookmark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Set<String> rxcuis = getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet<String>());

                if (isChecked)
                    rxcuis.add(rxcui);
                else
                    rxcuis.remove(rxcui);

                getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet("rxcuis", rxcuis)
                        .apply();
            }
        });

        /*
        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                startActivity(new Intent(view.getContext(), DrugActivity.class));
            }
        });
        */

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
                    getActivity().setTitle(result.getString("name"));
                    drugName.setText(result.getString("name"));

                    // fetch related drugs to the concept name
                    new AsyncTask<String, Void, JSONArray>() {

                        @Override
                        protected JSONArray doInBackground(String... params) {
                            RxNorm norm = new RxNorm();

                            try {
                                return norm.getDrugs(params[0]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONArray result) {

                            List<JSONArrayCursor> cursors = new ArrayList<JSONArrayCursor>();

                            // extract related concepts, the API group them by tty
                            try {
                                for (int i = 0; i < result.length(); i++) {
                                    if (result.getJSONObject(i).has("conceptProperties"))
                                        cursors.add(new JSONArrayCursor(result.getJSONObject(i).optJSONArray("conceptProperties")));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            ListFragment relatedDrugs = (ListFragment) getFragmentManager().findFragmentById(R.id.similar_drugs);

                            relatedDrugs.setListAdapter(new SimpleCursorAdapter(getActivity(), R.layout.drug_item, new MergeCursor(cursors.toArray(new Cursor[cursors.size()])), new String[]{"name"}, new int[]{R.id.drug_name}, 0x0));

                            /*
                            relatedDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                                @Override
                                public void onItemClick(LinearListView linearListView, View view, int i, long l) {
                                    // TODO: démarrer une activité pour afficher le médicament
                                }
                            });
                            */
                        }

                    };//.execute(result.getString("name"));
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

                final ImageView drugIcon = (ImageView) getView().findViewById(R.id.drug_icon);

                final Uri[] imageUris = new Uri[images.length()];

                try {
                    // charge l'icône du médicament
                    Glide.with(getActivity())
                            .load(images.getJSONObject(0).getString("imageUrl"))
                            .crossFade()
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
}
