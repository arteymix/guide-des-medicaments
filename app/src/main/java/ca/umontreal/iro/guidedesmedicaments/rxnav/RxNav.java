package ca.umontreal.iro.guidedesmedicaments.rxnav;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * SDK pour l'API JSON de RxNav.
 *
 * @author Guillaume Poirier-Morency
 */
public class RxNav {

    private final HttpClient client;

    /**
     * Crée une instance du SDK pour pouvoir effectuer des requêtes avec un client donné.
     *
     * @param client client HTTP fournis pour effectuer les requêtes en API
     */
    public RxNav(HttpClient client) {
        this.client = client;
    }

    /**
     * Effectue une requête GET sur un endpoint de RxNav.
     *
     * @param path  chemin qui est accédé
     * @param query paramètres supplémentaires qui sont déjà encodé par {@link java.net.URLEncoder}
     * @return      un JSONObject représentant la ressource accédée
     * @throws IOException
     * @throws JSONException
     * @throws URISyntaxException
     */
    protected JSONObject get(String path, String query) throws IOException, JSONException, URISyntaxException {
        HttpResponse response = client.execute(new HttpGet(new URI("http", null, "rxnav.nlm.nih.gov", 80, "/REST/" + path + ".json", query, null)));

        // Recent JSON api support InputStream...
        String body = IOUtils.toString(response.getEntity().getContent());

        Log.d("", body);

        return (JSONObject) new JSONTokener(body).nextValue();
    }

}
