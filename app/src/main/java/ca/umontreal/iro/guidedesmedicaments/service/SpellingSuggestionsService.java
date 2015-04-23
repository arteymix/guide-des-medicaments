package ca.umontreal.iro.guidedesmedicaments.service;

import android.service.textservice.SpellCheckerService;
import android.util.Log;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

import ca.umontreal.iro.rxnav.RxNorm;

/**
 * Service that propose spelling suggestions from RxNorm API.
 *
 * @author Guillaume Poirier-Morency
 */
public class SpellingSuggestionsService extends SpellCheckerService {

    /**
     *
     */
    public class SpellingSuggestionsSession extends Session {

        private OkHttpClient httpClient;

        @Override
        public void onCreate() {
            httpClient = new OkHttpClient();
            httpClient.setCache(new com.squareup.okhttp.Cache(getCacheDir(), 10 * 1024 * 1024));
        }

        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {
            try {
                String[] suggestions = RxNorm.newInstance(httpClient).getSpellingSuggestions(textInfo.getText())
                        .suggestionGroup
                        .suggestionList
                        .suggestion;

                return new SuggestionsInfo(SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO, suggestions);
            } catch (IOException e) {
                Log.e("", e.getLocalizedMessage(), e);
                return null;
            }
        }
    }

    @Override
    public Session createSession() {
        return new SpellingSuggestionsSession();
    }
}