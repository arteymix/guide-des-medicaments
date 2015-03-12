package ca.umontreal.iro.guidedesmedicaments;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Provide search capabilities that initiate the application flow.
 *
 * Show latest searches and carts.
 *
 * @author Guillaume Poirier-Morency
 * @author Patrice Dumontier-Houle
 * @author Charles Deharnais
 * @author Aldo Lamarre
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
