package com.prostoradio.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by timonsk on 16.05.14.
 */



public class MediaPlayerService extends Service implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private StatefulMediaPlayer mMediaPlayer            = new StatefulMediaPlayer();
    private final Binder mBinder                        = new MediaPlayerBinder();
    private IMediaPlayerServiceClient mClient;

    /**
     * A class for clients binding to this service. The client will be passed an object of this class
     * via its onServiceConnected(ComponentName, IBinder) callback.
     */
    public class MediaPlayerBinder extends Binder {
        /**
         * Returns the instance of this service for a client to make method calls on it.
         * @return the instance of this service.
         */
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }

    }

    /**
     * Returns the contained StatefulMediaPlayer
     * @return
     */
    public StatefulMediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * Initializes a StatefulMediaPlayer for streaming playback of the provided StreamStation
     * @param station The StreamStation representing the station to play
     */
    public void initializePlayer(StreamStation station) {
        mClient.onInitializePlayerStart("Connecting...");
        mMediaPlayer = new StatefulMediaPlayer(station);

        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync();
    }

    /**
     * Initializes a StatefulMediaPlayer for streaming playback of the provided stream url
     * @param streamUrl The URL of the stream to play.
     */
    public void initializePlayer(String streamUrl) {

        mMediaPlayer = new StatefulMediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(streamUrl);
        }
        catch (Exception e) {
            Log.e("MediaPlayerService", "error setting data source");
            mMediaPlayer.setState(StatefulMediaPlayer.MPStates.ERROR);
        }
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer player, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        mMediaPlayer.reset();
        mClient.onError();
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        mClient.onInitializePlayerSuccess();
        startMediaPlayer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Pauses the contained StatefulMediaPlayer
     */
    public void pauseMediaPlayer() {
        Log.d("MediaPlayerService","pauseMediaPlayer() called");
        mMediaPlayer.pause();
        stopForeground(true);

    }

    /**
     * Sets the client using this service.
     * @param client The client of this service, which implements the IMediaPlayerServiceClient interface
     */
    public void setClient(IMediaPlayerServiceClient client) {
        this.mClient = client;
    }

    /**
     * Starts the contained StatefulMediaPlayer and foregrounds the service to support
     * persisted background playback.
     */
    public void startMediaPlayer() {
        Context context = getApplicationContext();

        //set to foreground
        Notification notification = new Notification(android.R.drawable.ic_media_play, "PROSTO FM is playing!",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, Shell.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        CharSequence contentTitle = "PROSTO FM";
        CharSequence contentText = mMediaPlayer.getStreamStation().getStationLabel();
        notification.setLatestEventInfo(context, contentTitle,
                contentText, pendingIntent);
        startForeground(1, notification);

        Log.d("MediaPlayerService","startMediaPlayer() called");
        mMediaPlayer.start();
    }

    /**
     * Stops the contained StatefulMediaPlayer.
     */
    public void stopMediaPlayer() {
        try{
        stopForeground(true);
        mMediaPlayer.stop();
        mMediaPlayer.release();}
        catch (Exception e)
        {}
    }

    public void resetMediaPlaer() {
        stopForeground(true);
        mMediaPlayer.reset();
    }

}