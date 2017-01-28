package org.j2page.freebachradio.task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.j2page.freebachradio.db.Contract.TrackColumns;
import org.j2page.freebachradio.db.DbHelper;
import org.j2page.freebachradio.db.Track;

import java.io.InputStream;
import java.util.Random;

public class ChooseTrackTask extends AsyncTask<String, Void, Track> {

    Context context;
    Random rand;

    public ChooseTrackTask(Context context) {
        this.context = context;
        this.rand = new Random();
    }

    @Override
    protected Track doInBackground(String... params) {
        InputStream is;
        String channel = params[0].toLowerCase();
        return selectTrack(channel);
    }

    private Track selectTrack(String channel) {
        // query for songs on this channel
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TrackColumns._ID,
                TrackColumns.URL,
                TrackColumns.TITLE,
                TrackColumns.COMPOSER,
                TrackColumns.PERFORMER,
                TrackColumns.RELEASE,
                TrackColumns.IMAGE,
                TrackColumns.LOADED
        };
        String sortOrder = TrackColumns.TITLE + " DESC"; // TODO: last heard?

        Cursor cursor = db.query(
                TrackColumns.TABLE_NAME,
                projection,
                null, // "loaded = 2", // AND channel = ?
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder,
                "10"
        );

        Track[] tracks = new Track[10];
        cursor.moveToFirst();
        int count = 0;
        while (cursor.isAfterLast() == false) {
            tracks[count++] = cursor2Track(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        if (count > 0) {
            db.close();
            return tracks[rand.nextInt(count)];
        }

//        // TODO: select an unloaded track...
//       cursor = db.query(
//                TrackColumns.TABLE_NAME,
//                projection,
//                null, // "loaded = 2", // AND channel = ?
//                null,                            // The values for the WHERE clause
//                null,                                     // don't group the rows
//                null,                                     // don't filter by row groups
//                sortOrder,
//                "10"
//        );
//
//        cursor.moveToFirst();
//        while (cursor.isAfterLast() == false) {
//            tracks[count++] = cursor2Track(cursor);
//            cursor.moveToNext();
//        }
//        cursor.close();
//
//        if (count > 0) {
//            db.close();
//            return tracks[rand.nextInt(count)];
//        }

        db.close(); // TODO: finally
        return null;
    }

    private Track cursor2Track(Cursor cursor) {
        Track ret = new Track();
        ret.setUrl(cursor.getString(1));
        ret.setTitle(cursor.getString(2));
        ret.setComposer(cursor.getString(3));
        ret.setPerformer(cursor.getString(4));
        ret.setRelease(cursor.getString(5));
        ret.setImageUrl(cursor.getString(6));
        ret.setLoaded(cursor.getInt(7) > 0);
        return ret;
    }
}
