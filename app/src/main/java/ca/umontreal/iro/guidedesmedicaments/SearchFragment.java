package ca.umontreal.iro.guidedesmedicaments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.UUID;

import ca.umontreal.iro.guidedesmedicaments.loader.IOAsyncTaskLoader;
import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Fragment providing a simple search interface.
 * <p/>
 * todo: fetch the termtypes to fill the {@link RadioGroup}
 * todo: fetch the latest disclaimer from the API
 *
 * @author Guillaume Poirier-Morency
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<RxNorm.RxNormVersion> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);

        final SearchView sv = (SearchView) getView().findViewById(R.id.search_drug);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        sv.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        RadioGroup searchType = (RadioGroup) getView().findViewById(R.id.search_type);

        searchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // updates the search hint so that we do not need a label for the radio group
                RadioButton checkedButton = (RadioButton) getView().findViewById(checkedId);
                sv.setQueryHint("Search a " + StringUtils.lowerCase(checkedButton.getText().toString()) + "…");
            }
        });

        getActivity().getSupportLoaderManager()
                .initLoader(R.id.rxnorm_version_loader, null, this)
                .forceLoad();
    }

    @Override
    public Loader<RxNorm.RxNormVersion> onCreateLoader(int id, Bundle args) {
        return new IOAsyncTaskLoader<RxNorm.RxNormVersion>(getActivity()) {
            @Override
            public RxNorm.RxNormVersion loadInBackgroundSafely() throws IOException {
                return RxNorm.newInstance(new OkHttpClient()).getRxNormVersion();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<RxNorm.RxNormVersion> loader, RxNorm.RxNormVersion data) {
        // todo: this is a ugly fix since the MainActivity can switch tab and thus the presented fragment
        if (getView() == null)
            return;
        TextView rxNormVersion = (TextView) getView().findViewById(R.id.rxnorm_version);
        rxNormVersion.setText("Latest data available since " + data.version + ".");
    }

    @Override
    public void onLoaderReset(Loader<RxNorm.RxNormVersion> loader) {

    }
}
