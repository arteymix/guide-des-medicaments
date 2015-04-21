package ca.umontreal.iro.guidedesmedicaments.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

/**
 * AsyncTaskLoader designed to perform I/O operations safely.
 */
public abstract class IOAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

    /**
     * {@inheritDoc}
     */
    public IOAsyncTaskLoader(Context context) {
        super(context);
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
     * Perform a loadInBackground safely from {@link IOException}.
     *
     * @return
     * @throws IOException
     */
    public abstract T loadInBackgroundSafely() throws IOException;
}