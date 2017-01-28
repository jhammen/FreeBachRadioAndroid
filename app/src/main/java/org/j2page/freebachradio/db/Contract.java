package org.j2page.freebachradio.db;

import android.provider.BaseColumns;

public final class Contract {

    public static abstract class TrackColumns implements BaseColumns {
        public static final String TABLE_NAME = "track";
        public static final String URL = "url";
        public static final String TITLE = "title";
        public static final String COMPOSER = "composer";
        public static final String PERFORMER = "performer";
        public static final String RELEASE = "release";
        public static final String IMAGE = "image";
        public static final String LOADED = "loaded";
    }

}
