package nonphatic.quoz;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private FloatingActionButton mFab;
    private View mainView;
    private Timer timer;
    private TimerTask timerTask;
    private MediaPlayer mediaPlayer;
    private Random random;

    private int saturationPercent;
    private String changeMode;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getPreferences();
        mContentView = findViewById(R.id.fullscreen_content);
        mFab = (FloatingActionButton)findViewById(R.id.fab);
        mainView = findViewById(R.id.main_layout);
        timer = new Timer();
        random = new Random();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSwiping) {
                    setBackgroundToRandomColour();
                }
            }
        });

        mContentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("QUOZ Colour", ((TextView)mContentView).getText());
                clipboardManager.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle(float yDelta) {
        if (yDelta < 0) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mFab.hide();

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mFab.show();

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    //region OVERRIDES
    private float yPosOnDown;
    private float yDelta;
    private boolean isSwiping;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                yPosOnDown = event.getY();
                isSwiping = false;
                break;
            case MotionEvent.ACTION_UP:
                if (isSwiping) {
                    toggle(yDelta);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float yPosCurr = event.getY();
                yDelta = yPosCurr - yPosOnDown;
                if (Math.abs(yDelta) > 120) {
                    isSwiping = true;
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences();
        setBackgroundToRandomColour();
        setTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    //endregion

    //region HELPERS
    public void getPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        saturationPercent = preferences.getInt("saturation", 30);
        changeMode = preferences.getString("change_mode", "tap");
    }

    public void resetPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().clear().commit();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
    }

    private void setBackgroundToRandomColour() {
        float[] hsv = generateRandomColour();
        float[] hsvText = new float[] { 0, 0, hsv[1] / 2 + 0.25f };
        int randomColour = Color.HSVToColor(hsv);
        int textColour = Color.HSVToColor(hsvText);

        mainView.setBackgroundColor(randomColour);
        ((TextView)mContentView).setText(String.format("#%s", Integer.toHexString(randomColour).substring(2)));
        ((TextView)mContentView).setTextColor(textColour);
    }

    private float[] generateRandomColour() {
        float phiRecip = Float.parseFloat(getResources().getText(R.string.phiRecip).toString());
        return new float[] {
                (random.nextFloat() + phiRecip) % 1 * 360, // random, nicely-spaced hue
                saturationPercent / 100f, // saturation
                1.0f // value
        };
    }

    private void setTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }

        switch (changeMode) {
            case "tap":
                break;
            case "cycle":
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBackgroundToRandomColour();
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 0, 1000);
                break;
            case "leekspin":
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBackgroundToRandomColour();
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 350, 555);
                mediaPlayer = MediaPlayer.create(this, R.raw.ievan_polkka);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                break;
            case "nyan":
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBackgroundToRandomColour();
                            }
                        });
                    }
                };
                timer.schedule(timerTask, 200, 424);
                mediaPlayer = MediaPlayer.create(this, R.raw.nyan);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                break;
        }
    }
    //endregion

    //region ACTIVITIES
    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.PreferencesFragment.class.getName());
        intent.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true);
        startActivity(intent);
    }
    //endregion
}
