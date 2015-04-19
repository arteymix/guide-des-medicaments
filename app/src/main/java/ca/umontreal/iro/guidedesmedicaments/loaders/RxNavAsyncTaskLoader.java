package ca.umontreal.iro.guidedesmedicaments.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import ca.umontreal.iro.rxnav.RxNav;

/**
 * AsyncTaskLoader that targets a specific RxNav API and provide safe execution.
 */
public abstract class RxNavAsyncTaskLoader<A extends RxNav, T> extends AsyncTaskLoader<T> {

    protected final A rxNav;

    /**
     * @param context
     * @param rxNavApi
     */
    public RxNavAsyncTaskLoader(Context context, A rxNavApi) {
        super(context);
        this.rxNav = rxNavApi;
    }

    @Override
    public T loadInBackground() {
        try {
            return loadInBackgroundSafely();
        } catch (IOException e) {
            Log.e("", e.getLocalizedMessage(), e);
            this.abandon();
            return null;
        }
    }

    /**
     * @return
     * @throws IOException
     */
    public abstract T loadInBackgroundSafely() throws IOException;
}
