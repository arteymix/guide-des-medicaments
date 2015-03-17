package ca.umontreal.iro.guidedesmedicaments;

import android.database.AbstractCursor;
import android.util.Log;

import org.apache.commons.collections4.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Cursor over a JSONArray of JSONObject.
 *
 * @author Guillaume Poirier-Morency
 */
public class JSONArrayCursor extends AbstractCursor {

    private final JSONArray data;
    private final String idColumn;

    /**
     * @param data
     * @param idColumn column in the dataset used as the "_id", it must be an integer
     */
    public JSONArrayCursor(JSONArray data, String idColumn) {
        this.data = data;
        this.idColumn = idColumn;
    }

    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public String[] getColumnNames() {
        try {
            List<String> columnNames = IteratorUtils.toList(data.getJSONObject(mPos == -1 ? 0 : mPos).keys());
            return columnNames.toArray(new String[columnNames.size()]);
        } catch (JSONException je) {
            Log.e("", je.getMessage(), je);
            return new String[0];
        }
    }

    @Override
    public int getColumnIndex(String columnName) {
        if (columnName.equals("_id"))
            return super.getColumnIndex(idColumn);
        return super.getColumnIndex(columnName);
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
