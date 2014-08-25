package com.prostoradio.app;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class Shell extends Activity
    implements IMediaPlayerServiceClient {
        private StatefulMediaPlayer mMediaPlayer ;
        private StreamStation mSelectedStream = CONSTANTS.DEFAULT_STREAM_STATION;
        private MediaPlayerService mService ;
        private SeekBar seekBar;
        private boolean mBound ;
        private int soundStatus;
        private AudioManager audioManager;
        private Button  soundOffOnBtn;
        private Boolean _isRotate = false;
    public void SoundOffOn(View v)
    {
        String str = "black_sound_off";
        if (soundOffOnBtn.getTag().hashCode() == str.hashCode())
        {
            soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_on);
            soundOffOnBtn.setTag("black_sound_on");
            seekBar = (SeekBar) findViewById(R.id.soundValue);
            soundStatus = seekBar.getProgress();
            seekBar.setProgress(0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        }
        else
        {
            soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_off);
            soundOffOnBtn.setTag("black_sound_off");
            seekBar = (SeekBar) findViewById(R.id.soundValue);
            seekBar.setProgress(soundStatus);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, soundStatus, 0);

        }

    }

    private ProgressDialog mProgressDialog;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_shell);
            bindToService();
            mProgressDialog = new ProgressDialog(this);

            initializeButtons();
            initializeSoundValue();
            setupStationPicker();
        }
    public void initializeSoundValue()
    {
        audioManager= (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        seekBar = (SeekBar) findViewById(R.id.soundValue);
        seekBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 0 && progress <= seekBar.getMax()) {


                        if (progress==0)
                        {
                            soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_on);
                            soundStatus = seekBar.getProgress();
                        }
                        else
                        {
                            soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_off);
                            soundStatus = seekBar.getProgress();
                        }
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.shell, menu);
            return true;
        }



        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            // Handle item selection
            switch (item.getItemId())
            {
                case R.id.menuClose:
                    shutdownActivity();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        /**
         * Binds to the instance of MediaPlayerService. If no instance of MediaPlayerService exists, it first starts
         * a new instance of the service.
         */
    public void bindToService() {
        Intent intent = new Intent(this, MediaPlayerService.class);

        if (MediaPlayerServiceRunning()) {
            // Bind to LocalService
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        else {
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

    }

    /**
     * Sets up the stationPicker spinner
     */
    public void setupStationPicker() {
        Spinner stationPicker = (Spinner) findViewById(R.id.stationPicker);
        StreamStationSpinnerAdapter adapter = new StreamStationSpinnerAdapter(
                this, android.R.layout.simple_spinner_item);

        //populate adapter with stations
        for(StreamStation st : CONSTANTS.STATIONS)
        {
            adapter.add(st);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationPicker.setAdapter(adapter);
        stationPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                StreamStation selectedStreamStation = (StreamStation) parent.getItemAtPosition(pos);

                if (selectedStreamStation != mSelectedStream)
                {
                    try{
                    mService.stopMediaPlayer();
                    mSelectedStream = selectedStreamStation;
                    mService.initializePlayer(mSelectedStream);
                    }
                    catch (Exception ex){
                        _isRotate = true;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("_isRotate",_isRotate);
                        Intent i = new Intent(Shell.this, Shell.class);
                        i.putExtras(bundle);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Do nothing.
            }

        });
    }

    /**
     * Initializes buttons by setting even handlers and listeners, etc.
     */
    private void initializeButtons() {
          soundOffOnBtn = (Button) findViewById(R.id.soundOffOnBtn);
        // PLAY/PAUSE BUTTON
        final ToggleButton playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);

        playPauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (mBound) {
                        mMediaPlayer = mService.getMediaPlayer();

                        //pressed pause ->pause
                        if (!playPauseButton.isChecked()) {

                            if (mMediaPlayer.isStarted()) {
                                mService.pauseMediaPlayer();
                            }
                        }

                        //pressed play
                        else if (playPauseButton.isChecked()) {
                            // STOPPED, CREATED, EMPTY, -> initialize
                            if (mMediaPlayer.isStopped()
                                    || mMediaPlayer.isCreated()
                                    || mMediaPlayer.isEmpty()) {
                                mService.initializePlayer(mSelectedStream);
                            }

                            //prepared, paused -> resume play
                            else if (mMediaPlayer.isPrepared()
                                    || mMediaPlayer.isPaused()) {
                                try
                                {
                                    mService.startMediaPlayer();
                                }
                                catch (Exception ex)
                                {
                                    mProgressDialog.cancel();

                                    _isRotate = true;
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("_isRotate",_isRotate);
                                    Intent i = new Intent(Shell.this, Shell.class);
                                    i.putExtras(bundle);
                                }
                            }
                        }
                    }
                }
                catch(Exception ex)
                {
                    _isRotate = true;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("_isRotate",_isRotate);
                    Intent i = new Intent(Shell.this, Shell.class);
                    i.putExtras(bundle);
                }
            }
        });
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            Log.d("MainActivity", "service connected");

            //bound with Service. get Service instance
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) serviceBinder;
            mService = binder.getService();

            //send this instance to the service, so it can make callbacks on this instance as a client
            mService.setClient(Shell.this);
            mBound = true;

            //Set play/pause button to reflect state of the service's contained player
            final ToggleButton playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
            playPauseButton.setChecked(mService.getMediaPlayer().isPlaying());

            //Set station Picker to show currently set stream station
            Spinner stationPicker = (Spinner) findViewById(R.id.stationPicker);
            if(mService.getMediaPlayer() != null && mService.getMediaPlayer().getStreamStation() != null) {
                for (int i = 0; i < CONSTANTS.STATIONS.length; i++) {
                    if (mService.getMediaPlayer().getStreamStation().equals(CONSTANTS.STATIONS[i])) {
                        stationPicker.setSelection(i);
                        mSelectedStream = (StreamStation) stationPicker.getItemAtPosition(i);
                    }

                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /** Determines if the MediaPlayerService is already running.
     * @return true if the service is running, false otherwise.
     */
    private boolean MediaPlayerServiceRunning() {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.prostoradio.app.MediaPlayerService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void onInitializePlayerSuccess() {
        mProgressDialog.dismiss();

        final ToggleButton playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
        playPauseButton.setChecked(true);
    }

    public void onInitializePlayerStart(String message) {
        mProgressDialog = ProgressDialog.show(this, "", message, true);
        mProgressDialog.getWindow().setGravity(Gravity.TOP);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Shell.this.mService.resetMediaPlaer();
                final ToggleButton playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
                playPauseButton.setChecked(false);
            }

        });

    }

    @Override
    public void onError() {
        mProgressDialog.cancel();
    }

    /**
     * Closes unbinds from service, stops the service, and calls finish()
     */
    public void shutdownActivity() {

        if (mBound) {
            mService.stopMediaPlayer();
            // Detach existing connection.
            unbindService(mConnection);
            mBound = false;
        }

        Intent intent = new Intent(this, MediaPlayerService.class);
        stopService(intent);
        finish();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
            {
                seekBar = (SeekBar) findViewById(R.id.soundValue);
                if (seekBar.getProgress()==0)
                {
                    soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_on);
                    soundStatus = seekBar.getProgress();
                }
                else
                {
                    soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_off);
                    soundStatus = seekBar.getProgress();
                }
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                //Raise the Volume Bar on the Screen
                seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        + AudioManager.ADJUST_RAISE);
                return true;
            }

            case KeyEvent.KEYCODE_VOLUME_DOWN:
            {
                seekBar = (SeekBar) findViewById(R.id.soundValue);

                if (seekBar.getProgress()==0)
                {
                    soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_on);
                    soundStatus = seekBar.getProgress();
                }
                else
                {
                    soundOffOnBtn.setBackgroundResource(R.drawable.black_sound_off);
                    soundStatus = seekBar.getProgress();
                }
                //Adjust the Volume
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                //Lower the VOlume Bar on the Screen
                seekBar.setProgress(audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC)
                        + AudioManager.ADJUST_LOWER);

                return false;}

        }
        return super.onKeyDown(keyCode, event);
    }

}
