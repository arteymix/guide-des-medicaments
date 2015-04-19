package ca.umontreal.iro.guidedesmedicaments.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;

import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Provider for DisplayTerms from {@link RxNorm} API.
 * <p/>
 * This provider does a single API request and store all results in a {@link Trie} for fast prefix
 * search.
 */
public class DisplayTermsProvider extends ContentProvider {

    /**
     * Trie used for fast prefix search.
     */
    private Trie<String, Long> termsTrie;

    @Override
    public boolean onCreate() {
        // initialize the trie
        termsTrie = new PatriciaTrie();
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (termsTrie.isEmpty()) {
            // initialization
            try {
                RxNorm.DisplayTerms data = RxNorm.newInstance(new OkHttpClient()).getDisplayTerms();

                for (int i = 0; i < data.displayTermsList.term.length; i++) {
                    termsTrie.put(data.displayTermsList.term[i].toLowerCase(), (long) i);
                }

            } catch (IOException e) {
                Log.e("", e.getLocalizedMessage(), e);
            }
        }

        SortedMap<String, Long> matchingTerms = termsTrie.prefixMap(uri.getLastPathSegment().toLowerCase());

        MatrixCursor matrixCursor = new MatrixCursor(
                new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1},
                matchingTerms.size()); // avoid resize :P

        for (Map.Entry<String, Long> e : matchingTerms.entrySet()) {
            matrixCursor.addRow(new Object[]{e.getValue(), e.getKey()});
        }

        return matrixCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
