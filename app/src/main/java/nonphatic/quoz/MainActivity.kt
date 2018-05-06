package nonphatic.quoz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceActivity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast

import nonphatic.quoz.preferences.SettingsActivity

import java.util.Random
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private val mHideHandler: Handler = Handler()
    private val random: Random = Random()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var timer: Timer = Timer()

    private lateinit var mContentView: View
    private lateinit var mFab: FloatingActionButton
    private lateinit var mainView: View

    private val saturationPercent
        get() = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.preferences_colour_key),
                        resources.getInteger(R.integer.preferences_colour_default))
    private val changeMode
        get() = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.preferences_mode_key),
                        getString(R.string.preferences_mode_default))

    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
    }
    private val mHideRunnable = Runnable { hide() }


    //region OVERRIDES
    private var yPosOnDown: Float = 0f
    private var yDelta: Float = 0f
    private var isSwiping: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        volumeControlStream = AudioManager.STREAM_MUSIC

        mContentView = findViewById(R.id.fullscreen_content)
        mFab = findViewById(R.id.fab)
        mainView = findViewById(R.id.main_layout)

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener {
            if (!isSwiping) {
                setColour()
            }
        }

        mContentView.setOnLongClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.app_name), (mContentView as TextView).text)
            clipboardManager.primaryClip = clip

            Toast.makeText(applicationContext, getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, INIT_ANIMATION_DELAY)
    }

    private fun show() {
        // Show the system bar
        mContentView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mFab.show()

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mFab.hide()

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                yPosOnDown = event.y
                isSwiping = false
            }
            MotionEvent.ACTION_UP -> if (isSwiping) {
                if (yDelta < 0) hide() else show()
            }
            MotionEvent.ACTION_MOVE -> {
                val yPosCurr = event.y
                yDelta = yPosCurr - yPosOnDown
                if (Math.abs(yDelta) > SWIPE_MIN_DIST) {
                    isSwiping = true
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    public override fun onResume() {
        super.onResume()
        setColour()
        setLoop()
    }

    public override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
        mediaPlayer.reset()
        timer.cancel()
    }
    //endregion


    //region HELPERS
    private fun setColour(hsv: FloatArray = generateRandomColour()) {
        val textHSV = floatArrayOf(0f, 0f, hsv[1] / 2 + 0.25f)
        val backgroundColour = Color.HSVToColor(hsv)
        val textColour = Color.HSVToColor(textHSV)

        mainView.setBackgroundColor(backgroundColour)
        (mContentView as TextView).text = "#${Integer.toHexString(backgroundColour and HEX_MASK)}"
        (mContentView as TextView).setTextColor(textColour)
    }

    private fun generateRandomColour(): FloatArray = floatArrayOf(
            (random.nextFloat() + PHI_RECIP) % 1 * 360, // random, nicely-spaced hue
            saturationPercent / 100f,                   // saturation
            1f                                          // value
    )

    private fun scheduleTimer(delay: Long, period: Long) {
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        setColour()
                    }
                }
            }, delay, period)
        }
    }

    private fun playMedia(resId: Int) {
        mediaPlayer.setDataSource(this, Uri.parse("android.resource://$packageName/$resId"))
        mediaPlayer.prepare()
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun setLoop() {
        when (changeMode) {
            "tap" -> {}
            "cycle" -> {
                scheduleTimer(0, 1000)
            }
            "leekspin" -> {
                scheduleTimer(300, 550)
                playMedia(R.raw.ievan_polkka)
            }
            "nyan" -> {
                scheduleTimer(160, 423)
                playMedia(R.raw.nyan)
            }
        }
    }
    //endregion


    //region ACTIVITIES
    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.PreferencesFragment::class.java.name)
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
        startActivity(intent)
    }
    //endregion


    companion object {
        private const val UI_ANIMATION_DELAY = 300L
        private const val INIT_ANIMATION_DELAY = 100L
        private const val SWIPE_MIN_DIST = 120
        private const val HEX_MASK = 0xFFFFFF
        private const val PHI_RECIP = 0.6180339f
    }
}
