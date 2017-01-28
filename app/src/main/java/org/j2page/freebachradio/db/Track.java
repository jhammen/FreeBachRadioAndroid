package org.j2page.freebachradio.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

    private String url;
    private String title;
    private String composer;
    private String performer;
    private String release;
    private String imageUrl;
    private boolean loaded;

    public Track() {}

    protected Track(Parcel in) {
        url = in.readString();
        title = in.readString();
        composer = in.readString();
        performer = in.readString();
        release = in.readString();
        imageUrl = in.readString();
        loaded = in.readByte() != 0;
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return "Track " + url + "\nArtist:\t" + composer + "\nTitle:\t" + title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(title);
        parcel.writeString(composer);
        parcel.writeString(performer);
        parcel.writeString(release);
        parcel.writeString(imageUrl);
        parcel.writeByte((byte) (loaded ? 1 : 0));
    }
}
