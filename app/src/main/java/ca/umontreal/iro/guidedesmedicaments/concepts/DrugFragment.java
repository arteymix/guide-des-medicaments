package ca.umontreal.iro.guidedesmedicaments.concepts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.R;
import ca.umontreal.iro.rxnav.Interaction;
import ca.umontreal.iro.rxnav.RxImageAccess;
import ca.umontreal.iro.rxnav.RxNorm;

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
        final TextView drugDescription = (TextView) getView().findViewById(R.id.drug_description);
        final TextView genericName = (TextView) getView().findViewById(R.id.generic_name);
        final TextView administrationMethod = (TextView) getView().findViewById(R.id.administration_method);

        final ListFragment counterIndications = (ListFragment) getChildFragmentManager().findFragmentById(R.id.counter_indications);
        final ListFragment similarDrugs = (ListFragment) getChildFragmentManager().findFragmentById(R.id.similar_drugs);

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

        // fetch drug properties
        new AsyncTask<String, Void, RxNorm.RxConceptProperties>() {
            @Override
            protected RxNorm.RxConceptProperties doInBackground(String... rxcui) {
                try {
                    return RxNorm.newInstance().getRxConceptProperties(rxcui[0]);
                } catch (IOException ioe) {
                    // les données ne peuvent pas être récupérées
                    Toast.makeText(getActivity(), ioe.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    // already handled...
                    return null;
                }
            }

            @Override
            protected void onPostExecute(RxNorm.RxConceptProperties result) {
                getActivity().setTitle(result.properties.name);
                drugName.setText(result.properties.name);

                // fetch description & additionnal information from Wikipedia
                new AsyncTask<String, Void, JSONObject>() {

                    @Override
                    protected JSONObject doInBackground(String... drugNames) {
                        try {
                            JSONObject pages = new Api("en.wikipedia.org")
                                    .action("query")
                                    .param("prop", "extracts")
                                    .param("titles", StringUtils.join(drugNames, "|"))
                                    .param("redirects", "")
                                    .get()
                                    .asObject()
                                    .getJSONObject("query")
                                    .getJSONObject("pages");

                            return pages.getJSONObject(pages.keys().next());
                        } catch (ApiException | JSONException e) {
                            Log.e("", e.getLocalizedMessage(), e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final JSONObject description) {
                        if (description == null) {
                            drugDescription.setText("No description were found.");
                            return;
                        }

                        // todo: show first paragraph rather than a limited number of lines
                        genericName.setText(description.optString("title"));
                        drugDescription.setText(Html.fromHtml(description.optString("extract")));

                        // expand/collapse on click
                        // todo: smooth animation
                        drugDescription.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (drugDescription.getLineCount() > 10) {
                                    // expanded, time to collapse
                                    Log.i("", "expanding description...");
                                    drugDescription.setMaxLines(10);
                                } else {
                                    // collapsed, expand and restitute the text :)
                                    Log.i("", "collapsing description...");
                                    drugDescription.setMaxLines(Integer.MAX_VALUE);
                                    drugDescription.setText(Html.fromHtml(description.optString("extract")));
                                }
                            }
                        });
                    }
                }.execute(result.properties.name);

                // fetch related drugs to the concept name
                new AsyncTask<String, Void, RxNorm.Drugs>() {

                    @Override
                    protected RxNorm.Drugs doInBackground(String... params) {
                        try {
                            return RxNorm.newInstance().getDrugs(params[0]);
                        } catch (IOException e) {
                            Log.e("", e.getLocalizedMessage(), e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(RxNorm.Drugs result) {
                        if (result == null) {
                            Toast.makeText(getActivity(), "No related drugs were found.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<RxNorm.ConceptProperties> conceptProperties = new ArrayList<>();

                        // extract related concepts, the API group them by tty
                        for (RxNorm.ConceptGroup c : result.drugGroup.conceptGroup)
                            if (c.conceptProperties != null)
                                conceptProperties.addAll(Arrays.asList(c.conceptProperties));

                        similarDrugs.setListAdapter(new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_list_item_1,
                                conceptProperties));
                    }

                }.execute(result.properties.name);
            }
        }.execute(rxcui);

        // todo: fetch contraindications (interactions?)
        new AsyncTask<String, Void, Interaction.DrugInteractions>() {

            @Override
            protected Interaction.DrugInteractions doInBackground(String... params) {
                try {
                    return Interaction.newInstance().findDrugInteractions(params[0]);
                } catch (IOException e) {
                    Log.e("", e.getLocalizedMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Interaction.DrugInteractions drugInteractions) {
                if (drugInteractions == null) {
                    Toast.makeText(getActivity(), "No drug interactions were found.", Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> comments = new ArrayList<>();

                for (Interaction.InteractionTypeGroup interactionTypeGroup : drugInteractions.interactionTypeGroup)
                    for (Interaction.InteractionTypeGroup.InteractionType interactionType : interactionTypeGroup.interactionType)
                        comments.add(interactionType.comment);

                counterIndications.setListAdapter(new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        comments));
            }
        }.execute(rxcui);

        // fetch potential images for the drug
        new AsyncTask<String, Void, RxImageAccess.ImageAccess>() {

            @Override
            protected RxImageAccess.ImageAccess doInBackground(String... params) {
                RxImageAccess image = new RxImageAccess();

                try {
                    // TODO: use the rxcui to identify the images
                    return image.rxbase(new BasicNameValuePair("name", "aspirin"));
                } catch (IOException e) {
                    Log.e("", e.getMessage(), e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(final RxImageAccess.ImageAccess images) {
                if (images == null)
                    return; // TODO: could not fetch images

                final ImageView drugIcon = (ImageView) getView().findViewById(R.id.drug_icon);

                // charge l'icône du médicament
                Glide.with(getActivity())
                        .load(images.nlmRxImages[0].imageUrl)
                        .crossFade()
                        .into(drugIcon);

                // au clic, lancer la galerie avec tous les images du médicament
                drugIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // display images in a gallery
                        Intent displayImages = new Intent(Intent.ACTION_VIEW);

                        displayImages.setDataAndType(Uri.parse(images.nlmRxImages[0].imageUrl), "image/*");

                        Uri[] imageUris = new Uri[images.nlmRxImages.length];

                        for (int i = 0; i < images.nlmRxImages.length; i++)
                            imageUris[i] = Uri.parse(images.nlmRxImages[i].imageUrl);

                        displayImages.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        displayImages.putExtra(Intent.EXTRA_STREAM, imageUris);

                        try {
                            startActivity(displayImages);
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            // TODO: display the gallery in a basic {@link ViewFlipper}
                            Toast.makeText(getActivity(), "Could not display the drug images in a gallery.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }.execute(rxcui);
    }
}
