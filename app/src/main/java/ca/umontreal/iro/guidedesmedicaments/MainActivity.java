package ca.umontreal.iro.guidedesmedicaments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterViewFlipper;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * Keys for shared preferences.
     */
    public static final String
            BOOKMARKS = "bookmarks",
            CART = "cart";

    /**
     * Fragment to display.
     */
    public static final String ACTION = "action";

    /**
     * Key used in cart and bookmarks shared preferences to hold the rxcuis identifiers of the
     * marked concepts.
     */
    public static final String RXCUIS = "rxcuis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final ActionBar.Tab searchTab = actionBar.newTab()
                .setText(getString(android.R.string.search_go))
                .setTabListener(this);

        final ActionBar.Tab bookmarksTab = actionBar.newTab()
                .setText(getString(R.string.bookmarks))
                .setTabListener(this);

        final ActionBar.Tab cartTab = actionBar.newTab()
                .setText(getString(R.string.cart))
                .setTabListener(this);

        actionBar.addTab(searchTab);
        actionBar.addTab(bookmarksTab);
        actionBar.addTab(cartTab);

        switch (getIntent().getIntExtra(ACTION, R.id.action_search)) {
            case R.id.action_search:
                actionBar.selectTab(searchTab);
                break;

            case R.id.action_bookmarks:
                actionBar.selectTab(bookmarksTab);
                break;

            case R.id.action_cart:
                actionBar.selectTab(cartTab);
                break;

            default:
                // raise a goddamn exception!
                finishActivity(RESULT_CANCELED);
        }
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
                if (getSupportActionBar().getSelectedNavigationIndex() == 1)
                    return getSharedPreferences(BOOKMARKS, Context.MODE_PRIVATE)
                            .edit()
                            .remove(RXCUIS)
                            .commit();

                if (getSupportActionBar().getSelectedNavigationIndex() == 2)
                    return getSharedPreferences(CART, Context.MODE_PRIVATE)
                            .edit()
                            .remove(RXCUIS)
                            .commit();

                break;

            case R.id.action_search:
                return onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // todo: destroy any running loaders?
        if (getSupportLoaderManager().hasRunningLoaders()) {
            Toast.makeText(this,
                    "Operation in progress, cannot switch tab right now.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (tab.getPosition()) {
            case 0:
                fragmentTransaction
                        .replace(R.id.main_content, new SearchFragment())
                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                break;

            case 1:
                Set<String> bookmarks = getSharedPreferences(BOOKMARKS, Context.MODE_PRIVATE)
                        .getStringSet(RXCUIS, new HashSet());

                fragmentTransaction
                        .replace(R.id.main_content, DrugsFragment.newInstance(new ArrayList<>(bookmarks), "No drugs in bookmarks."))
                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                break;

            case 2:
                fragmentTransaction
                        .replace(R.id.main_content, CartFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
