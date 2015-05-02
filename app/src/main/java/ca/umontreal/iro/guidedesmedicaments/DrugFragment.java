package ca.umontreal.iro.guidedesmedicaments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linearlistview.LinearListView;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.loader.IOAsyncTaskLoader;
import ca.umontreal.iro.rxnav.Interaction;
import ca.umontreal.iro.rxnav.RxClass;
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
 * TODO: be more uniform and require the rxcui provided by arguments
 *
 * @author Guillaume Poirier-Morency
 */
public class DrugFragment extends Fragment {

    /**
     * Identifier of the drug to present in the fragment.
     */
    public static final String RXCUI = "rxcui";

    /**
     * Use the drug name as the current activity title.
     * <p/>
     * Set this argument key if and you want the current activity title to be replaced by the drug
     * name. This is convenient if the activity is essentially presenting the drug like the
     * {@link DrugActivity}.
     * <p/>
     * The default behiaviour is to replace the title.
     */
    public static final String USE_DRUG_NAME_AS_TITLE = "use_drug_name_as_title";

    public static DrugFragment newInstance(String rxcui, boolean useDrugNameAsTitle) {
        Bundle bundle = new Bundle();

        bundle.putString(RXCUI, rxcui);
        bundle.putBoolean(USE_DRUG_NAME_AS_TITLE, useDrugNameAsTitle);

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

        final OkHttpClient httpClient = new OkHttpClient();

        httpClient.setCache(new com.squareup.okhttp.Cache(getActivity().getCacheDir(), 10 * 1024 * 1024));

        final String rxcui = getArguments() == null ?
                getActivity().getIntent().getData().getPathSegments().get(2) :
                getArguments().getString(RXCUI);

        final TextView drugName = (TextView) getView().findViewById(R.id.drug_name);
        final ExpandableTextView drugDescription = (ExpandableTextView) getView().findViewById(R.id.drug_description);
        final TextView categories = (TextView) getView().findViewById(R.id.categories);
        final TextView doseForms = (TextView) getView().findViewById(R.id.dose_forms);

        final LinearListView contraIndications = (LinearListView) getView().findViewById(R.id.counter_indications);
        final LinearListView similarDrugs = (LinearListView) getView().findViewById(R.id.similar_drugs);

        CheckBox bookmark = (CheckBox) getView().findViewById(R.id.bookmark);

        bookmark.setChecked(getActivity().getSharedPreferences(MainActivity.BOOKMARKS, Context.MODE_PRIVATE)
                .getStringSet(MainActivity.RXCUIS, new HashSet<String>())
                .contains(rxcui));

        bookmark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Set<String> rxcuis = getActivity()
                        .getSharedPreferences(MainActivity.BOOKMARKS, Context.MODE_PRIVATE)
                        .getStringSet(MainActivity.RXCUIS, new HashSet<String>());

                if (isChecked)
                    rxcuis.add(rxcui);
                else
                    rxcuis.remove(rxcui);

                getActivity().getSharedPreferences(MainActivity.BOOKMARKS, Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet(MainActivity.RXCUIS, rxcuis)
                        .apply();
            }
        });

        contraIndications.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int position, long id) {
                // show the interaction drug
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + id)));
            }
        });

        similarDrugs.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int position, long id) {
                // show the magnificent drug!
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + id)));
            }
        });

        getLoaderManager().initLoader(R.id.name_loader, null, new LoaderManager.LoaderCallbacks<RxNorm.RxConceptProperties>() {
            @Override
            public Loader<RxNorm.RxConceptProperties> onCreateLoader(int id, Bundle args) {
                return new IOAsyncTaskLoader<RxNorm.RxConceptProperties>(getActivity()) {

                    @Override
                    public RxNorm.RxConceptProperties loadInBackgroundSafely() throws IOException {
                        return RxNorm.newInstance(httpClient).getRxConceptProperties(rxcui);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxNorm.RxConceptProperties> loader, RxNorm.RxConceptProperties data) {
                if (data == null) {
                    // les données ne peuvent pas être récupérées
                    Toast.makeText(getActivity(), "Error fetching drug details.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    return;
                }

                final TextView termType = (TextView) getView().findViewById(R.id.term_type);

                if (getArguments() == null || getArguments().getBoolean(USE_DRUG_NAME_AS_TITLE, true))
                    getActivity().setTitle(data.properties.name);

                drugName.setText(data.properties.name);
                termType.setText(data.properties.tty);

                // todo: fetch administration methods

                // fetch potential images for the drug
                new AsyncTask<RxNorm.RxConceptProperties, Void, RxImageAccess.ImageAccess>() {

                    @Override
                    protected RxImageAccess.ImageAccess doInBackground(RxNorm.RxConceptProperties... params) {
                        try {
                            return RxImageAccess.newInstance(httpClient).rxbase(
                                    new BasicNameValuePair("name", params[0].properties.name)
                                    // todo: fetch image by rxcui rather than name
                                    // new BasicNameValuePair("rxcui", params[0].properties.rxcui)
                            );
                        } catch (IOException e) {
                            Log.e("", e.getMessage(), e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final RxImageAccess.ImageAccess images) {
                        if (images == null) {
                            Toast.makeText(getActivity(), "Could not fetch the drug image.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (images.replyStatus.imageCount == 0)
                            return;

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
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);

                // fetch description & additionnal information from Wikipedia
                new AsyncTask<RxNorm.RxConceptProperties, Void, JSONObject>() {

                    @Override
                    protected JSONObject doInBackground(RxNorm.RxConceptProperties... drugNames) {
                        try {
                            JSONObject pages = new Api("en.wikipedia.org")
                                    .action("query")
                                    .param("prop", "extracts")
                                    .param("titles", drugNames[0].properties.name)
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
                    protected void onPostExecute(final JSONObject page) {
                        if (page == null) {
                            drugDescription.setText("No description were found.");
                            return;
                        }

                        // todo: show first paragraph rather than a limited number of lines
                        drugDescription.setText(Html.fromHtml(page.optString("extract")));
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.RxConceptProperties> loader) {

            }
        }).forceLoad();

        getLoaderManager().initLoader(R.id.administration_method_loader, null, new LoaderManager.LoaderCallbacks<RxNorm.RelatedByType>() {
            @Override
            public Loader<RxNorm.RelatedByType> onCreateLoader(int id, Bundle args) {
                return new IOAsyncTaskLoader<RxNorm.RelatedByType>(getActivity()) {
                    @Override
                    public RxNorm.RelatedByType loadInBackgroundSafely() throws IOException {
                        return RxNorm.newInstance(httpClient).getRelatedByType(rxcui, "DF");
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxNorm.RelatedByType> loader, final RxNorm.RelatedByType data) {
                List<String> names = new ArrayList<>();

                // the only requested group is "DF" and it will be indexed at 0
                if (data.relatedGroup.conceptGroup[0].conceptProperties == null) {
                    // no administration methods, so we just hide the label
                    getView().findViewById(R.id.dose_forms_label).setVisibility(View.GONE);
                    getView().findViewById(R.id.dose_forms).setVisibility(View.GONE);
                    return;
                }

                for (RxNorm.ConceptProperties conceptProperties : data.relatedGroup.conceptGroup[0].conceptProperties) {
                    names.add(conceptProperties.name);
                }

                // todo: humanize
                doseForms.setText(StringUtils.join(names, ", "));

                doseForms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> rxcuis = new ArrayList<>();

                        for (RxNorm.ConceptGroup conceptGroup : data.relatedGroup.conceptGroup) {
                            for (RxNorm.ConceptProperties conceptProperties : conceptGroup.conceptProperties) {
                                rxcuis.add(conceptProperties.rxcui);
                            }
                        }

                        Intent showDoseForms = new Intent(getActivity(), DrugsActivity.class);

                        // todo: use the data from the API rather than the UI for the concept name
                        showDoseForms.putExtra(Intent.EXTRA_TITLE, drugName.getText() + " dose forms");
                        showDoseForms.putStringArrayListExtra(DrugsFragment.RXCUIS, rxcuis);

                        startActivity(showDoseForms);
                    }
                });
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.RelatedByType> loader) {

            }
        }).forceLoad();

        // fetch drug classes
        getLoaderManager().initLoader(R.id.class_loader, null, new LoaderManager.LoaderCallbacks<RxClass.ClassByRxNormDrugId>() {

            @Override
            public Loader<RxClass.ClassByRxNormDrugId> onCreateLoader(int id, final Bundle args) {
                return new IOAsyncTaskLoader<RxClass.ClassByRxNormDrugId>(getActivity()) {

                    @Override
                    public RxClass.ClassByRxNormDrugId loadInBackgroundSafely() throws IOException {
                        Log.d("", "requesting!");
                        return RxClass.newInstance(httpClient).getClassByRxNormDrugId(rxcui, null);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxClass.ClassByRxNormDrugId> loader, RxClass.ClassByRxNormDrugId data) {
                if (data.rxclassDrugInfoList == null) {
                    // no categories for the concept
                    categories.setVisibility(View.GONE);
                    return;
                }

                List<String> classNames = new ArrayList<>();

                for (RxClass.ClassByRxNormDrugId.RxClassDrugInfoList.RxClassDrugInfo rxclassDrugInfo : data.rxclassDrugInfoList.rxclassDrugInfo)
                    classNames.add(rxclassDrugInfo.rxclassMinConceptItem.className);

                categories.setText(StringUtils.join(classNames, ", ") /*oxford(classNames)*/);
            }

            @Override
            public void onLoaderReset(Loader<RxClass.ClassByRxNormDrugId> loader) {

            }
        }).forceLoad();

        // todo: fetch contraindications (interactions?)
        getLoaderManager().initLoader(R.id.contraindications_loader, null, new LoaderManager.LoaderCallbacks<Interaction.DrugInteractions>() {
            @Override
            public Loader<Interaction.DrugInteractions> onCreateLoader(int id, Bundle args) {
                return new IOAsyncTaskLoader<Interaction.DrugInteractions>(getActivity()) {

                    @Override
                    public Interaction.DrugInteractions loadInBackgroundSafely() throws IOException {
                        return Interaction.newInstance(httpClient).findDrugInteractions(rxcui);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Interaction.DrugInteractions> loader, Interaction.DrugInteractions drugInteractions) {
                if (drugInteractions.interactionTypeGroup == null) {
                    // no interaction :(
                    getView().findViewById(R.id.label_counter_indicator).setVisibility(View.GONE);
                    contraIndications.setVisibility(View.GONE);
                    return;
                }

                MatrixCursor descriptions = new MatrixCursor(new String[]{BaseColumns._ID, "name", "description"});

                for (Interaction.DrugInteractions.InteractionTypeGroup interactionTypeGroup : drugInteractions.interactionTypeGroup)
                    for (Interaction.DrugInteractions.InteractionTypeGroup.InteractionType interactionType : interactionTypeGroup.interactionType)
                        for (Interaction.InteractionPair interactionPair : interactionType.interactionPair) {
                            descriptions.addRow(new Object[]{
                                    Long.parseLong(interactionPair.interactionConcept[1].minConceptItem.rxcui),
                                    interactionPair.interactionConcept[1].minConceptItem.name, // related concept in interaction
                                    interactionPair.description}); // description of the interaction
                        }

                contraIndications.setAdapter(new SimpleCursorAdapter(
                        getActivity(),
                        android.R.layout.simple_list_item_2,
                        descriptions,
                        new String[]{"name", "description"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0x0));
            }

            @Override
            public void onLoaderReset(Loader<Interaction.DrugInteractions> loader) {

            }
        }).forceLoad();

        getLoaderManager().initLoader(R.id.similar_drugs_loader, null, new LoaderManager.LoaderCallbacks<RxNorm.RelatedByType>() {
            @Override
            public Loader<RxNorm.RelatedByType> onCreateLoader(int id, Bundle args) {
                return new IOAsyncTaskLoader<RxNorm.RelatedByType>(getActivity()) {
                    @Override
                    public RxNorm.RelatedByType loadInBackgroundSafely() throws IOException {
                        return RxNorm.newInstance(httpClient).getRelatedByType(rxcui, "BN", "IN");
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxNorm.RelatedByType> loader, RxNorm.RelatedByType data) {
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "name", "tty"});

                // extract related concepts, the API group them by tty
                // TODO: this thing does not work well (only return a single entry when there are at least 20 for aspirin)
                for (RxNorm.ConceptGroup c : data.relatedGroup.conceptGroup)
                    if (c.conceptProperties != null) // bugfix.. (the Gson mapping is probably wrong)
                        for (RxNorm.ConceptProperties conceptProperties : c.conceptProperties)
                            matrixCursor.addRow(new Object[]{Long.parseLong(conceptProperties.rxcui), conceptProperties.name, conceptProperties.tty});

                if (matrixCursor.getCount() == 0) {
                    getView().findViewById(R.id.label_similar_drugs).setVisibility(View.GONE);
                    similarDrugs.setVisibility(View.GONE);
                    return;
                }

                similarDrugs.setAdapter(new SimpleCursorAdapter(getActivity(),
                        android.R.layout.simple_list_item_2,
                        matrixCursor,
                        new String[]{"name", "tty"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0x0));
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.RelatedByType> loader) {

            }
        }).forceLoad();
    }
}
