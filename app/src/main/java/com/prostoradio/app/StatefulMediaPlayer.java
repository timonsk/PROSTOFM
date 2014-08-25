package com.prostoradio.app;

/**
 * Created by timonsk on 16.05.14.
 */
import java.io.IOException;
import android.media.AudioManager;
import android.util.Log;


public class StatefulMediaPlayer extends android.media.MediaPlayer {
    /**
     * Set of states for StatefulMediaPlayer:<br>
     * EMPTY, CREATED, PREPARED, STARTED, PAUSED, STOPPED, ERROR
     */
    public enum MPStates {
        EMPTY, CREATED, PREPARED, STARTED, PAUSED, STOPPED, ERROR
    }

    private MPStates mState;
    private StreamStation mStreamStation;

    public StreamStation getStreamStation() {
        return mStreamStation;
    }

    /**
     * Sets a StatefulMediaPlayer's data source as the provided StreamStation
     * @param streamStation the StreamStation to set as the data source
     */
    public void setStreamStation(StreamStation streamStation) {
        this.mStreamStation = streamStation;
        try {
            setDataSource(streamStation.getStationUrl());
            setState(MPStates.CREATED);
        }
        catch (Exception e) {
            Log.e("StatefulMediaPlayer", "setDataSource failed");
            setState(MPStates.ERROR);
        }
    }

    /**
     * Instantiates a StatefulMediaPlayer object.
     */
    public StatefulMediaPlayer() {
        super();
        setState(MPStates.CREATED);
    }

    /**
     * Instantiates a StatefulMediaPlayer object with the Audio Stream Type
     * set to STREAM_MUSIC and the provided StreamStation's URL as the data source.
     * @param streamStation The StreamStation to use as the data source
     */
    public StatefulMediaPlayer(StreamStation streamStation) {
        super();
        this.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mStreamStation = streamStation;
        try {
            setDataSource(mStreamStation.getStationUrl());
            setState(MPStates.CREATED);
        }
        catch (Exception e) {
            Log.e("StatefulMediaPlayer", "setDataSourceFailed");
            setState(MPStates.ERROR);
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.mState = MPStates.EMPTY;
    }

    @Override
    public void start() {
        super.start();
        setState(MPStates.STARTED);
    }

    @Override
    public void pause() {

        super.pause();
        setState(MPStates.PAUSED);

    }

    @Override
    public void stop() {
        super.stop();
        setState(MPStates.STOPPED);
    }

    @Override
    public void release() {
        super.release();
        setState(MPStates.EMPTY);
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
        setState(MPStates.PREPARED);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
        setState(MPStates.PREPARED);
    }

    public MPStates getState() {
        return mState;
    }

    /**
     * @param state the state to set
     */
    public void setState(MPStates state) {
        this.mState = state;
    }

    public boolean isCreated() {
        return (mState == MPStates.CREATED);
    }

    public boolean isEmpty() {
        return (mState == MPStates.EMPTY);
    }

    public boolean isStopped() {
        return (mState == MPStates.STOPPED);
    }

    public boolean isStarted() {
        return (mState == MPStates.STARTED || this.isPlaying());
    }

    public boolean isPaused() {
        return (mState == MPStates.PAUSED);
    }

    public boolean isPrepared() {
        return (mState == MPStates.PREPARED);
    }
}
