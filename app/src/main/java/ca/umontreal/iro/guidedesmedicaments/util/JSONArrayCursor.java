package ca.umontreal.iro.guidedesmedicaments.util;

import android.database.AbstractCursor;
import android.util.Log;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Cursor over a JSONArray of JSONObject, JSONArray or JSON primitive to provide simple integration
 * of JSON-based API data into {@link android.database.Cursor}.
 * <p/>
 * When using OBJECT, the name can be inferred.
 * <p/>
 * When using ARRAY, the names must be provided.
 * <p/>
 * When using PRIMITIVE as type, the data entry will be returned, no matter what column index or
 * name is provided. This is useful to cover APIs that return lists of JSON primitives.
 *
 * @author Guillaume Poirier-Morency
 */
public class JSONArrayCursor extends AbstractCursor {

    private final JSONArray data;

    private String[] columnNames = new String[]{"_id"};

    /**
     * JSONArray of the providen type.
     *
     * @param data
     */
    public JSONArrayCursor(JSONArray data) {
        this.data = data;
    }

    /**
     * JSONArray of JSONArray, similar to {@link android.database.MatrixCursor}.
     *
     * @param data
     * @param columnNames
     */
    public JSONArrayCursor(JSONArray data, String[] columnNames) {
        this(data);
        this.columnNames = columnNames;
    }

    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public String[] getColumnNames() {
        if (mPos == -1 && data.length() == 0)
            return columnNames;

        int pos = mPos == -1 ? 0 : mPos;

        if (data.opt(pos).getClass().isPrimitive())
            return new String[]{"_id"};

        if (data.opt(pos) instanceof JSONObject)
            columnNames = IteratorUtils.toArray(data.optJSONObject(pos).keys(), String.class);

        // some api already provide a "_id" column
        if (ArrayUtils.contains(columnNames, "_id"))
            return columnNames;

        // add "_id" to the end to preserve right indexing
        return ArrayUtils.add(columnNames, "_id");
    }

    @Override
    public String getString(int column) {
        try {
            if (data.get(mPos).getClass().isPrimitive())
                return data.getString(mPos);

            if (data.get(mPos) instanceof JSONArray)
                return data.getJSONArray(mPos).getString(column);

            return data.getJSONObject(mPos).getString(getColumnName(column));
        } catch (JSONException je) {
            Log.e("", "", je);
            return null;
        }
    }

    @Override
    public short getShort(int column) {
        return (short) getInt(column);
    }

    @Override
    public int getInt(int column) {
        if (data.opt(mPos).getClass().isPrimitive())
            return data.optInt(mPos);

        if (data.opt(mPos) instanceof JSONArray)
            return data.optJSONArray(mPos).optInt(column);

        return data.optJSONObject(mPos).optInt(getColumnName(column));
    }

    @Override
    public long getLong(int column) {
        // check for cases of undefined "_id"
        if (column == getColumnIndex("_id")) {
            // always use {@link mPos} as primitive id
            if (data.opt(mPos).getClass().isPrimitive())
                return mPos;

            // last index of the matrix
            if (data.opt(mPos) instanceof JSONArray && column == data.optJSONArray(mPos).length())
                return mPos;

            // object does not have the "_id" key
            if (!data.optJSONObject(mPos).has("_id"))
                return mPos;
        }

        if (data.opt(mPos).getClass().isPrimitive())
            return data.optLong(mPos);

        if (data.opt(mPos) instanceof JSONArray)
            return data.optJSONArray(mPos).optLong(column);

        return data.optJSONObject(mPos).optLong(getColumnName(column));
    }

    @Override
    public float getFloat(int column) {
        return (float) getDouble(column);
    }

    @Override
    public double getDouble(int column) {
        if (data.opt(mPos).getClass().isPrimitive())
            return data.optDouble(mPos);

        if (data.opt(mPos) instanceof JSONArray)
            return data.optJSONArray(mPos).optDouble(column);

        return data.optJSONObject(mPos).optDouble(getColumnName(column));

    }

    @Override
    public boolean isNull(int column) {
        if (data.opt(mPos).getClass().isPrimitive())
            return data.isNull(mPos);

        if (data.opt(mPos) instanceof JSONArray)
            return data.optJSONArray(mPos).isNull(column);

        return data.optJSONObject(mPos).isNull(getColumnName(column));

    }
}
