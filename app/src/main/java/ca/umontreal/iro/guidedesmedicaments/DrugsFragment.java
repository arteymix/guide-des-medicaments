package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;
import org.mediawiki.api.json.ApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ca.umontreal.iro.guidedesmedicaments.loader.IOAsyncTaskLoader;
import ca.umontreal.iro.rxnav.RxImageAccess;
import ca.umontreal.iro.rxnav.RxNorm;


/**
 * Fragment presenting a list of drugs.
 *
 * @author Guillaume Poirier-Morency
 */
public class DrugsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<RxNorm.Rxcui>> {

    /**
     * Unique identifier for the {@link Loader} that loads drugs information into the
     * {@link ListFragment}.
     */
    public static final int DRUGS_LOADER = 6;

    /**
     * Intent key to present a list of concepts from their rxcui identifier.
     */
    public static final String RXCUIS = "rxcuis";

    /**
     * Intent key to filter results by a tty.
     */
    public static final String TTY = "tty";

    /**
     * Factory for the DrugsFragment created out of a list of rxcuis identifiers.
     * <p/>
     * This is used in the {@link MainActivity} to present bookmarks.
     *
     * @param rxcuis
     * @return
     */
    public static DrugsFragment newInstance(ArrayList<String> rxcuis) {
        DrugsFragment drugsFragment = new DrugsFragment();
        Bundle bundle = new Bundle();

        bundle.putStringArrayList(RXCUIS, rxcuis);

        drugsFragment.setArguments(bundle);

        return drugsFragment;
    }

    /**
     * HTTP client reused over requests.
     */
    private OkHttpClient httpClient;

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // initialize the client for reuse over intents
        httpClient = new OkHttpClient();
        httpClient.setCache(new com.squareup.okhttp.Cache(getActivity().getCacheDir(), 10 * 1024 * 1024));

        // todo: use only getArguments...
        Bundle arguments = getArguments() == null ?
                getActivity().getIntent().getExtras() : getArguments();

        getActivity().getSupportLoaderManager()
                .initLoader(DRUGS_LOADER, arguments, this)
                .forceLoad();
    }

    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);

        setEmptyText("No matches for the provided query.");
    }

    @Override
    public Loader<List<RxNorm.Rxcui>> onCreateLoader(int id, final Bundle args) {
        if (args.containsKey(SearchManager.QUERY))
            getActivity().setTitle("Results for " + args.getString(SearchManager.QUERY));

        return new IOAsyncTaskLoader<List<RxNorm.Rxcui>>(getActivity()) {

            @Override
            public List<RxNorm.Rxcui> loadInBackgroundSafely() throws IOException {
                List<RxNorm.Rxcui> rxcuis = new ArrayList<>();

                // search from the query
                if (args.containsKey(SearchManager.QUERY)) {
                    Log.i("", "Searching for " + args.getString(SearchManager.QUERY));
                    for (String term : args.getString(SearchManager.QUERY).split("[\\s]")) {
                        RxNorm.SpellingSuggestions spellingSuggestions = RxNorm.newInstance(httpClient).getSpellingSuggestions(term);
                        if (spellingSuggestions.suggestionGroup.suggestionList == null)
                            continue; // no suggestions for this term
                        for (String suggestedTerm : spellingSuggestions.suggestionGroup.suggestionList.suggestion)
                            rxcuis.add(RxNorm.newInstance(httpClient).findRxcuiByString(suggestedTerm, null, true, RxNorm.NORMALIZED)); // normalized search
                    }
                }

                // fetch extra rxcuis as well
                if (args.containsKey(RXCUIS)) {
                    for (String rxcui : args.getStringArrayList(RXCUIS)) {
                        String name = RxNorm.newInstance(httpClient).getRxConceptProperties(rxcui).properties.name;
                        rxcuis.add(RxNorm.newInstance(httpClient).findRxcuiByString(name, null, true, RxNorm.EXACT_MATCH));
                    }
                }

                return rxcuis;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<RxNorm.Rxcui>> loader, final List<RxNorm.Rxcui> rxcuis) {
        // generally a network error (already logged)
        if (rxcuis == null) {
            Toast.makeText(getActivity(), "Error while accessing the RxNav API.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // define a custom adapter
        setListAdapter(new ArrayAdapter<RxNorm.Rxcui>(getActivity(),
                R.layout.drug_item,
                R.id.drug_name,
                rxcuis) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final View c = super.getView(position, convertView, parent);

                final RxNorm.Rxcui rxcui = rxcuis.get(position);

                TextView drugName = (TextView) c.findViewById(R.id.drug_name);
                final TextView drugDescription = (TextView) c.findViewById(R.id.drug_description);
                CheckBox bookmark = (CheckBox) c.findViewById(R.id.bookmark);
                CheckBox cart = (CheckBox) c.findViewById(R.id.cart);

                drugName.setText(rxcui.idGroup.name);

                if (rxcui.idGroup.rxnormId == null)
                    return c; // .. sadly ;(

                bookmark.setChecked(getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet<String>())
                        .contains(rxcui.idGroup.rxnormId[0]));

                bookmark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Set<String> bookmarks = getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                                .getStringSet("rxcuis", new HashSet<String>());

                        if (isChecked)
                            bookmarks.add(rxcui.idGroup.rxnormId[0]);
                        else
                            bookmarks.remove(rxcui.idGroup.rxnormId[0]);

                        getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                                .edit()
                                .putStringSet("rxcuis", bookmarks)
                                .apply();
                    }
                });

                cart.setChecked(getActivity().getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet<String>())
                        .contains(rxcui.idGroup.rxnormId[0]));

                cart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Set<String> cart = getActivity().getSharedPreferences("cart", Context.MODE_PRIVATE)
                                .getStringSet("rxcuis", new HashSet<String>());

                        if (isChecked)
                            cart.add(rxcui.idGroup.rxnormId[0]);
                        else
                            cart.remove(rxcui.idGroup.rxnormId[0]);

                        getActivity().getSharedPreferences("cart", Context.MODE_PRIVATE)
                                .edit()
                                .putStringSet("rxcuis", cart)
                                .apply();
                    }
                });

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

                        drugDescription.setText(Html.fromHtml(page.optString("extract")));
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rxcui.idGroup.name);

                // fetch the image
                new AsyncTask<String, Void, RxImageAccess.ImageAccess>() {

                    @Override
                    protected RxImageAccess.ImageAccess doInBackground(String... name) {
                        try {
                            // TODO: use the rxcui to identify the images
                            return RxImageAccess.newInstance(httpClient).rxbase(new BasicNameValuePair("name", name[0]));
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
                        Glide.with(getActivity())
                                .load(image.nlmRxImages[0].imageUrl)
                                .crossFade()
                                .fitCenter()
                                .centerCrop()
                                .into(drugIcon);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rxcuis.get(position).idGroup.name);

                // todo: bookmarking live!
                // c.findViewById(R.id.bookmark).setOnClickListener();

                // présente le médicament au clic
                c.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rxcuis.get(position).idGroup.rxnormId == null) {
                            Log.w("", "no rxcui matching drug name " + rxcuis.get(position).idGroup.name);
                            Toast.makeText(getActivity(), "Could not identify this drug (no matching rxcui).", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // show the magnificent drug!
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://rxnav.nlm.nih.gov/REST/rxcui/" + rxcuis.get(position).idGroup.rxnormId[0])));
                    }
                });

                return c;
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<List<RxNorm.Rxcui>> loader) {

    }
}
