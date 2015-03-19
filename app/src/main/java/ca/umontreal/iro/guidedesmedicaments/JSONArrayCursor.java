package ca.umontreal.iro.guidedesmedicaments;

import android.database.AbstractCursor;
import android.util.Log;

import org.apache.commons.collections4.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Cursor over a JSONArray of JSONObject.
 * <p/>
 * Allows simple integration of API data into {@link android.widget.Adapter}.
 *
 * @author Guillaume Poirier-Morency
 */
public class JSONArrayCursor extends AbstractCursor {

    private final JSONArray data;

    /**
     * @param data
     */
    public JSONArrayCursor(JSONArray data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        if (data.length() > 10)
            return 10;
        return data.length();
    }

    @Override
    public String[] getColumnNames() {
        try {
            List<String> columnNames = IteratorUtils.toList(data.getJSONObject(mPos == -1 ? 0 : mPos).keys());

            // some api already provide a "_id" column
            if (!columnNames.contains("_id"))
                columnNames.add(0, "_id");

            return columnNames.toArray(new String[columnNames.size()]);
        } catch (JSONException je) {
            Log.e("", je.getMessage(), je);
            return new String[0];
        }
    }

    @Override
    public String getString(int column) {
        try {
            return data.getJSONObject(mPos).getString(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return null;
        }
    }

    @Override
    public short getShort(int column) {
        try {
            return (short) data.getJSONObject(mPos).getInt(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return 0;
        }
    }

    @Override
    public int getInt(int column) {
        try {
            return data.getJSONObject(mPos).getInt(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return 0;
        }
    }

    @Override
    public long getLong(int column) {
        try {
            JSONObject obj = data.getJSONObject(mPos);

            // use "_id" if it's provided in the data, otherwise {@link mPos}
            if (column == getColumnIndex("_id"))
                return obj.has("_id") ? obj.getLong("_id") : mPos;

            return data.getJSONObject(mPos).getLong(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return 0L;
        }
    }

    @Override
    public float getFloat(int column) {
        try {
            return (float) data.getJSONObject(mPos).getDouble(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return 0.0F;
        }
    }

    @Override
    public double getDouble(int column) {
        try {
            return data.getJSONObject(mPos).getDouble(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return 0.0;
        }
    }

    @Override
    public boolean isNull(int column) {
        try {
            return data.getJSONObject(mPos).isNull(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return true;
        }
    }
}
