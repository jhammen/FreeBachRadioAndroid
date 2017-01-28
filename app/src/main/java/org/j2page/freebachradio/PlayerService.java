package org.j2page.freebachradio;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.j2page.freebachradio.db.Track;
import org.j2page.freebachradio.task.ChooseTrackTask;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    public static int NOTIFICATION_ID = 102;

    private static final String TAG = PlayerService.class.getName();

    private String channel;
    private MediaPlayer mediaPlayer = null;
    private final IBinder binder = new PlayerServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.channel = intent.getStringExtra("channel");
        Log.d(TAG, "service start command");
        nextTrack();
        return START_STICKY;
    }

    private void playTrack(String urlString) {
        // release existing media player
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        try {
            mediaPlayer.setDataSource(urlString);
        } catch (IOException e) {
            // TODO: send error, nextTrack
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "mediaplayer start");
        mediaPlayer.start();
    }

    public boolean togglePause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            return false;
        } else {
            mediaPlayer.start();
            return true;
        }
    }

    public void nextTrack() {
        // TODO: load next track
        Log.d(TAG, "loading next track");
        new ChooseTrackTask(this) {
            @Override
            protected void onPostExecute(Track track) {
                // broadcast new track
                Intent intent = new Intent("nextTrack");
                intent.putExtra("track", track);
                LocalBroadcastManager.getInstance(PlayerService.this).sendBroadcast(intent);
                Log.d("nextTrack", track.getUrl());
                playTrack(track.getUrl());
            }
        }.execute(this.channel);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextTrack();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("error", "what = " + what);
        Log.e("error", "extra = " + extra);
        nextTrack();
        return true;
    }

    public class PlayerServiceBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroy", "onDestroy");
        // release mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //  cancel foreground notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
