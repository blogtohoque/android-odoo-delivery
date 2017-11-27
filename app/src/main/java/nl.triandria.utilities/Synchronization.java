package nl.triandria.utilities;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import database.StockPicking;

public class Synchronization {

    private static final String[] MODELS_TO_SYNC = {
            "stock_picking",
            "res_users",
            "res_company",
            "res_partner",
            "stock_location",
    };

    /**
     * The goal of the this function to replicate data of the remote server locally.
     * First it checks which records exist on the server that do not exist locally and fetches them
     * Then it checks whether any records that already exist locally have been updated on the server
     * by checking the last_update_date saved locally and the write_date of the records of the given
     * table.
     */
    public static boolean synchronize(Context context) throws JSONRPCException, JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SessionManager.SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        String last_sync_date = sharedPreferences.getString("last_sync_date", null);
        String url = sharedPreferences.getString("url", null);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null, SQLiteDatabase.OPEN_READWRITE);
        try {
            db.beginTransaction();
            for (final String model : MODELS_TO_SYNC) {
                final List<String> modelFields = StockPicking.TABLE_FIELDS.get(model);
                List<Integer> local_ids = new ArrayList<>();
                Cursor cr = db.rawQuery("SELECT id FROM " + model, null);
                if (cr.moveToFirst()) {
                    do {
                        local_ids.add(cr.getInt(0));
                    } while (cr.moveToNext());
                    cr.close();
                    JSONRPCHttpClient client = new JSONRPCHttpClient(url);
                    JSONArray remote_ids = client.callJSONArray(
                            "search",
                            model,
                            Arrays.asList("id", "not in", local_ids));
                    JSONArray records = client.callJSONArray("read", model, remote_ids, new HashMap() {{
                        put("fields", modelFields);
                    }});
                    db.execSQL("INSERT INTO " + model + modelFields + records);
                    // check if this is our first sync
                    if (last_sync_date != null) {
                        JSONArray remote_changed_record_ids = client.callJSONArray(
                                "search",
                                model,
                                Arrays.asList(
                                        Arrays.asList("id", "in", local_ids),
                                        Arrays.asList("write_date", ">", last_sync_date)));
                        JSONArray updated_records = client.callJSONArray(
                                "read",
                                model,
                                remote_changed_record_ids,
                                new HashMap() {{
                                    put("fields", modelFields);
                                }});
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject rec = records.getJSONObject(i);
                            db.execSQL("UPDATE " + model +
                                    " SET " + modelFields +
                                    " VALUES " +  rec.toString() +
                                    " WHERE id=" + rec.getInt("id"));
                        }
                    }
                }
                sharedPreferences.edit().putString("last_sync_date", format_date(new Date())).apply();
                db.setTransactionSuccessful();
                return true;
            }
        } finally {
            db.endTransaction();
        }
        return false;
    }

    private static String format_date(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(date);
    }
}
