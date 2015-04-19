package ca.umontreal.iro.guidedesmedicaments.util;

import android.database.AbstractCursor;
import android.util.Log;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Cursor over a JSONArray of JSONObject, JSONArray or JSON primitive to provide simple integration
 * of JSON-based API data into {@link android.database.Cursor}.
 * <p/>
 * If specified, the "_id" column will be used from the JSONObject or fallback to the internal
 * position attribute.
 * <p/>
 * TODO: way to specify '_id' column at construct-time
 *
 * @author Guillaume Poirier-Morency
 * @deprecated moving to {@link com.google.gson.Gson} in progress...
 */
@Deprecated
public class JSONArrayCursor extends AbstractCursor {

    private final JSONArray data;

    private String[] columnNames = new String[]{"_id"};

    /**
     * Cursor over a {@link JSONArray} of {@link JSONObject} or primitives.
     * <p/>
     * In case of primitive, all columns in the cursor will point to the same value.
     *
     * @param data
     */
    public JSONArrayCursor(JSONArray data) {
        this.data = data;
    }

    /**
     * Cursor over a {@link JSONArray} of {@link JSONArray} using predefined column names.
     * <p/>
     * This is functionnaly similar to {@link android.database.MatrixCursor}.
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
        // empty data!
        if (data.length() == 0)
            return columnNames;

        int pos = mPos == -1 ? 0 : mPos;

        // primitive have no way of providing a ID
        if (data.opt(pos).getClass().isPrimitive() || data.opt(mPos) instanceof String)
            return columnNames;

        // extract the column names from the keys of the current object
        if (data.opt(pos) instanceof JSONObject)
            columnNames = IteratorUtils.toArray(data.optJSONObject(pos).keys(), String.class);

        // some rxNav already provide a "_id" column, use it
        if (ArrayUtils.contains(columnNames, "_id"))
            return columnNames;

        // add "_id" to the end to preserve right indexing
        return ArrayUtils.add(columnNames, "_id");
    }

    @Override
    public String getString(int column) {
        if (data.opt(mPos).getClass().isPrimitive() || data.opt(mPos) instanceof String)
            return data.optString(mPos);

        if (data.opt(mPos) instanceof JSONArray)
            return data.optJSONArray(mPos).optString(column);

        return data.optJSONObject(mPos).optString(getColumnName(column));
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
            if (data.opt(mPos).getClass().isPrimitive() || data.opt(mPos) instanceof String)
                return mPos;

            // JSONArray does not specify "_id" key
            if (data.opt(mPos) instanceof JSONArray/* && column == data.optJSONArray(mPos).length()*/)
                return mPos;

            Log.i("", data.opt(mPos).toString());

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
