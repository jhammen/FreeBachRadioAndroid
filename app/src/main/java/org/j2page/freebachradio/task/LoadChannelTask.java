package org.j2page.freebachradio.task;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.JsonReader;

import org.j2page.freebachradio.db.Contract.TrackColumns;
import org.j2page.freebachradio.db.DbHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoadChannelTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private String channel;

    public LoadChannelTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        InputStream is;
        try {
            channel = params[0].toLowerCase();
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String siteBase = (String) ai.metaData.get("siteBase");
            URL url = new URL(siteBase + "/" + channel + ".json");
            loadJson(url); // TODO: if necessary
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadJson(URL url) throws IOException {
        InputStream is = null;
        SQLiteDatabase db = null;
        try {
            // get database
            DbHelper dbHelper = new DbHelper(context);
            db = dbHelper.getWritableDatabase();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(true);
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(is));
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                readEntry(jsonReader, db);
            }
            jsonReader.endArray();
        } finally {
            if (is != null) {
                is.close();
            }
            db.close();
        }
    }

    private void readEntry(JsonReader jsonReader, SQLiteDatabase db) throws IOException {
        jsonReader.beginObject();
        ContentValues values = new ContentValues();
        // values.put(TrackColumns.CHANNEL, channel);
        String baseUrl = "";
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if (key.equals("base")) { // TODO: magic strings
                baseUrl = jsonReader.nextString();
            } else if (key.equals("composer")) {
                values.put(TrackColumns.COMPOSER, jsonReader.nextString());
            } else if (key.equals("performer")) {
                values.put(TrackColumns.PERFORMER, jsonReader.nextString());
            } else if (key.equals("release")) {
                values.put(TrackColumns.RELEASE, jsonReader.nextString());
            } else if (key.equals("image")) {
                values.put(TrackColumns.IMAGE, jsonReader.nextString());
            } else if (key.equals("tracks")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String trackKey = jsonReader.nextName();
                        if (trackKey.equals("url")) {
                            String fullUrl = baseUrl + jsonReader.nextString();
                            values.put(TrackColumns.URL, fullUrl);
                        } else if (trackKey.equals("title")) {
                            values.put(TrackColumns.TITLE, jsonReader.nextString());
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    // store in database
                    values.put(TrackColumns.LOADED, 0);
                    long id = db.insertWithOnConflict(TrackColumns.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    jsonReader.endObject();
                }
                jsonReader.endArray();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }
}