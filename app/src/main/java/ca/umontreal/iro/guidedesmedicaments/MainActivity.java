package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Navigate through the {@link SearchFragment}, {@link DrugsFragment} and {@link CartFragment} on
 * the tip of the finger!
 * <p/>
 * The layout contains a {@link android.support.v4.app.Fragment} that is swapped on purpose for the
 * view we are interested in.
 * <p/>
 * TODO: prefetch rxcuis for suggestions
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, new SearchFragment())
                .commit();

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        MatrixCursor actionCursor = new MatrixCursor(new String[]{BaseColumns._ID, "title"});

        actionCursor.addRow(new Object[]{R.id.action_search, getString(R.string.search_a_drug)});
        actionCursor.addRow(new Object[]{R.id.action_bookmarks, getString(R.string.bookmarks)});
        actionCursor.addRow(new Object[]{R.id.action_cart, getString(R.string.cart)});

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new SimpleCursorAdapter(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        actionCursor,
                        new String[]{"title"},
                        new int[]{android.R.id.text1}, 0x0), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                return getSharedPreferences("cart", Context.MODE_PRIVATE)
                        .edit()
                        .remove("rxcuis")
                        .commit();

            case R.id.action_search:
                return onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        switch ((int) l) {
            case R.id.action_search:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, new SearchFragment())
                        .commit();

                return true;

            case R.id.action_bookmarks:
                Set<String> bookmarks = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
                        .getStringSet("rxcuis", new HashSet());

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, DrugsFragment.newInstance(new ArrayList<>(bookmarks)))
                        .commit();

                return true;

            case R.id.action_cart:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, CartFragment.newInstance())
                        .commit();

                return true;
        }
        return false;
    }
}
