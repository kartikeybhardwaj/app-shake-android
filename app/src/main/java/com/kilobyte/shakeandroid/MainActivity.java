package com.kilobyte.shakeandroid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {

    // elements on screen
    private ImageView iv_music;
    private TextView tv_settings_audio, tv_settings_sensitivity;
    private AppCompatSeekBar sb_sensitivity;

    // icon rotation animation
    private RotateAnimation rotate;

    // music type - mini or full
    private boolean isMiniAudio = false;

    // media player
    private MediaPlayer mp;

    // is music playing
    private boolean playing = false;

    // accelerometer sensor
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false;
    private double NOISE = 5.0;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeElements();

        sb_sensitivity.setOnSeekBarChangeListener(this);

        welcomeUser();

        initializeSensorAndMediaPlayer();
    }

    private void initializeElements() {
        iv_music = findViewById(R.id.main_music);
        iv_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudio();
            }
        });

        tv_settings_audio = findViewById(R.id.main_audio);
        tv_settings_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudio();
            }
        });

        tv_settings_sensitivity = findViewById(R.id.main_sensitivity);
        sb_sensitivity = findViewById(R.id.main_seekbar);

        rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(900);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);
    }

    private void welcomeUser() {
        final TextView tv_hello = findViewById(R.id.main_hello);
        final ImageView iv_next = findViewById(R.id.main_next);
        final LinearLayout ll_settings = findViewById(R.id.main_ll);

        tv_hello.setAlpha(0f);
        tv_hello.animate().alpha(1f).setDuration(1500);

        iv_next.setAlpha(0f);
        iv_next.animate().alpha(1f).setDuration(2000);

        // on click of right arrow icon
        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_hello.animate().alpha(0f).setDuration(1500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        tv_hello.setVisibility(View.GONE);
                    }
                });
                iv_next.animate().alpha(0f).setDuration(1500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        iv_next.setVisibility(View.GONE);

                        TextView tv_shake = findViewById(R.id.main_shake);
                        tv_shake.setVisibility(View.VISIBLE);
                        tv_shake.setAlpha(0f);
                        tv_shake.animate().alpha(1f).setDuration(1500);

                        iv_music.setVisibility(View.VISIBLE);
                        iv_music.setAlpha(0f);
                        iv_music.animate().alpha(1f).setDuration(1500);

                        ll_settings.setVisibility(View.VISIBLE);
                        ll_settings.setAlpha(0f);
                        ll_settings.animate().alpha(1f).setDuration(1500);

                        toggleAudio();
                    }
                });
            }
        });
    }

    private void initializeSensorAndMediaPlayer() {
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        mp = MediaPlayer.create(this, R.raw.soundmini);
        mp.setVolume(1.0f, 1.0f);

        previousNoise = new ArrayList<Double>();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String sensitivity_disp = this.getResources().getString(R.string.sensitivity) + progress;
        tv_settings_sensitivity.setText(sensitivity_disp);
        NOISE = (10 - progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void playImage() {
        mp.start();
        mp.setLooping(true);
        iv_music.startAnimation(rotate);
    }

    private void stopImage() {
        mp.pause();
        mp.seekTo(mp.getCurrentPosition());
        iv_music.clearAnimation();
    }

    public void onSensorChanged(SensorEvent event) {
        if (!initialized) {
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            initialized = true;
        } else {
            float dAX = xAccel - event.values[0];
            float dAY = yAccel - event.values[1];
            float dAZ = zAccel - event.values[2];
            double noiseVector = Math.sqrt(Math.pow(dAX, 2) + Math.pow(dAY, 2) + Math.pow(dAZ, 2));

            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            previousNoise.add(noiseVector);
            while (previousNoise.size() > MAXNOISECOUNT) {
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing) {
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum / MAXNOISECOUNT > NOISE) {
                    playImage();
                    playing = true;
                }
            } else if (playing) {
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum / MAXNOISECOUNT < NOISE) {
                    if (mp.isPlaying()) {
                        stopImage();
                    }
                    playing = false;
                }
            }
        }
    }

    void toggleAudio() {
        isMiniAudio = !isMiniAudio;
        if (isMiniAudio) {
            mp.stop();
            mp = MediaPlayer.create(this, R.raw.soundmini);
            if (playing) {
                mp.start();
            }
            tv_settings_audio.setText(this.getResources().getString(R.string.audio_mini));
        } else {
            mp.stop();
            mp = MediaPlayer.create(this, R.raw.soundfull);
            if (playing) {
                mp.start();
            }
            tv_settings_audio.setText(this.getResources().getString(R.string.audio_full));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        if (playing) {
            playImage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopImage();
        sm.unregisterListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopImage();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopImage();
        super.onDestroy();
    }
}
