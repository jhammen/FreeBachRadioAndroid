package org.j2page.freebachradio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.j2page.freebachradio.db.Track;
import org.j2page.freebachradio.task.LoadChannelTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class PlayActivity extends AppCompatActivity {

    private static final String CHANNEL_NAME = "freebachradio";
    private static final String TAG = PlayActivity.class.getName();

    private PlayerService playerService;
    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // controls
        final Button forwardButton = (Button) findViewById(R.id.button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceBound) { // TODO: replace with disabling button when service unbound
                    playerService.nextTrack();
                }
            }
        });
        final Button pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceBound) {
                    if (playerService.togglePause()) {
                        pauseButton.setText("Pause");
                        forwardButton.setEnabled(true);
                    } else {
                        pauseButton.setText("Play");
                        forwardButton.setEnabled(false);
                    }
                }
            }
        });

        // register to receive events
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("nextTrack"));

        Log.d(TAG, "loading channel");
        // load channel info then start playing
        new LoadChannelTask(this) {
            protected void onPostExecute(Boolean result) {
                // start player service
                Intent intent = new Intent(PlayActivity.this, PlayerService.class);
                intent.putExtra("channel", CHANNEL_NAME);
                startService(intent);
            }
        }.execute(CHANNEL_NAME);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((PlayerService.PlayerServiceBinder) service).getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
            serviceBound = false;
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(loadingImage());
            Track track = intent.getParcelableExtra("track");
            new LoadImageTask(imageView).execute(track.getImageUrl());
            showInfo(track);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!serviceBound) {
            Intent bindIntent = new Intent(this, PlayerService.class);
            serviceBound = bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        serviceBound = false;
    }

    @Override
    public void onBackPressed() {
        // avoid call to onDestroy
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            Intent intent = new Intent(this, PlayerService.class);
            stopService(intent);
        }
    }

    private void showInfo(Track track) {
        ((TextView) findViewById(R.id.pieceText)).setText(track.getTitle());
        ((TextView) findViewById(R.id.composerText)).setText(track.getComposer());
        ((TextView) findViewById(R.id.performerText)).setText(track.getPerformer());
        ((TextView) findViewById(R.id.releaseText)).setText(track.getRelease());
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView image;

        public LoadImageTask(ImageView image) {
            this.image = image;
        }

        protected Bitmap doInBackground(String... urlString) {
            try {
                URL url = new URL(urlString[0]);
                URLConnection connection = url.openConnection();
                connection.setUseCaches(true);
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return Bitmap.createScaledBitmap(bitmap, 300, 300, true);
            } catch (IOException e) {
                return loadingImage();
            }
        }

        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
    }

    private Bitmap loadingImage() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.cover_loading);
    }
}
