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
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
     * Ids for different loaders in this fragment.
     */
    public static int
            NAME_LOADER = 0,
            DESCRIPTION_LOADER = 1,
            IMAGE_LOADER = 2,
            CLASS_LOADER = 5,
            SIMILAR_DRUGS_LOADER = 6,
            CONTRAINDICATIONS_LOADER = 7;

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

        final OkHttpClient httpClient = new OkHttpClient();

        httpClient.setCache(new com.squareup.okhttp.Cache(getActivity().getCacheDir(), 10 * 1024 * 1024));

        final String rxcui = getArguments() == null ?
                getActivity().getIntent().getData().getPathSegments().get(2) :
                getArguments().getString("rxcui");

        final TextView drugName = (TextView) getView().findViewById(R.id.drug_name);
        final ExpandableTextView drugDescription = (ExpandableTextView) getView().findViewById(R.id.drug_description);
        final TextView genericName = (TextView) getView().findViewById(R.id.generic_name);
        final TextView categories = (TextView) getView().findViewById(R.id.categories);
        final TextView administrationMethod = (TextView) getView().findViewById(R.id.administration_method);

        final ListView counterIndications = (ListView) getView().findViewById(R.id.counter_indications);
        final ListView similarDrugs = (ListView) getView().findViewById(R.id.similar_drugs);

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

        similarDrugs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // show the magnificent drug!
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + id)));
            }
        });

        getLoaderManager().initLoader(NAME_LOADER, null, new LoaderManager.LoaderCallbacks<RxNorm.RxConceptProperties>() {
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
                if (loader.isAbandoned()) {
                    // les données ne peuvent pas être récupérées
                    Toast.makeText(getActivity(), "Could not fetch drug details.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    return;
                }
                getActivity().setTitle(data.properties.name);
                drugName.setText(data.properties.name);

                // fetch potential images for the drug
                new AsyncTask<String, Void, RxImageAccess.ImageAccess>() {

                    @Override
                    protected RxImageAccess.ImageAccess doInBackground(String... params) {
                        try {
                            return RxImageAccess.newInstance(httpClient).rxbase(new BasicNameValuePair("name", params[0]));
                        } catch (IOException e) {
                            Log.e("", e.getMessage(), e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(final RxImageAccess.ImageAccess images) {
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
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.properties.name);

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
                    protected void onPostExecute(final JSONObject page) {
                        if (page == null) {
                            drugDescription.setText("No description were found.");
                            return;
                        }

                        // todo: show first paragraph rather than a limited number of lines
                        genericName.setText(page.optString("title"));
                        drugDescription.setText(Html.fromHtml(page.optString("extract")));
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.properties.name);
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.RxConceptProperties> loader) {

            }
        }).forceLoad();

        // fetch drug classes
        getLoaderManager().initLoader(CLASS_LOADER, null, new LoaderManager.LoaderCallbacks<RxClass.ClassByRxNormDrugId>() {

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
                Log.d("", "loaded drug classes");
                if (data.rxclassDrugInfoList == null) {
                    categories.setText("No categories found"); // collapse the categories
                    return; // no data :(
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
        getLoaderManager().initLoader(CONTRAINDICATIONS_LOADER, null, new LoaderManager.LoaderCallbacks<Interaction.DrugInteractions>() {
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
                    Toast.makeText(getActivity(), "No drug interactions were found.", Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> descriptions = new ArrayList<>();

                for (Interaction.InteractionTypeGroup interactionTypeGroup : drugInteractions.interactionTypeGroup)
                    for (Interaction.InteractionTypeGroup.InteractionType interactionType : interactionTypeGroup.interactionType)
                        for (Interaction.InteractionTypeGroup.InteractionType.InteractionPair interactionPair : interactionType.interactionPair)
                            for (Interaction.InteractionTypeGroup.InteractionType.InteractionPair.InteractionConcept interactionConcept : interactionPair.interactionConcept)
                                descriptions.add(interactionPair.description);

                counterIndications.setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        descriptions));
            }

            @Override
            public void onLoaderReset(Loader<Interaction.DrugInteractions> loader) {

            }
        }).forceLoad();

        getLoaderManager().initLoader(SIMILAR_DRUGS_LOADER, null, new LoaderManager.LoaderCallbacks<RxNorm.AllRelatedInfo>() {
            @Override
            public Loader<RxNorm.AllRelatedInfo> onCreateLoader(int id, Bundle args) {
                return new IOAsyncTaskLoader<RxNorm.AllRelatedInfo>(getActivity()) {
                    @Override
                    public RxNorm.AllRelatedInfo loadInBackgroundSafely() throws IOException {
                        return RxNorm.newInstance(httpClient).getAllRelatedInfo(rxcui);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<RxNorm.AllRelatedInfo> loader, RxNorm.AllRelatedInfo data) {
                if (data.allRelatedGroup.conceptGroup == null) {
                    Toast.makeText(getActivity(), "No related drugs were found.", Toast.LENGTH_LONG).show();
                    return;
                }

                MatrixCursor matrixCursor = new MatrixCursor(new String[]{BaseColumns._ID, "name", "rxcui"});

                // extract related concepts, the API group them by tty
                // TODO: this thing does not work well (only return a single entry when there are at least 20 for aspirin)
                for (RxNorm.ConceptGroup c : data.allRelatedGroup.conceptGroup)
                    if (c.conceptProperties != null) // bugfix.. (the Gson mapping is probably wrong)
                        for (RxNorm.ConceptProperties conceptProperties : c.conceptProperties)
                            matrixCursor.addRow(new Object[]{Long.parseLong(conceptProperties.rxcui), conceptProperties.name, conceptProperties.rxcui});

                similarDrugs.setAdapter(new SimpleCursorAdapter(getActivity(),
                        android.R.layout.simple_list_item_2,
                        matrixCursor,
                        new String[]{"name", "rxcui"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0x0));
            }

            @Override
            public void onLoaderReset(Loader<RxNorm.AllRelatedInfo> loader) {

            }
        }).forceLoad();
    }
}
