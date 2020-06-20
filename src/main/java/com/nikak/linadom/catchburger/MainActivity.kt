package com.nikak.linadom.catchburger

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import com.nikak.linadom.catchburger.R.id.scoreText
import com.nikak.linadom.catchburger.R.id.timeText


class MainActivity : AppCompatActivity() {

    private var score: Int = 0
    var imageArray = ArrayList<ImageView>()
    var handler: Handler = Handler()
    var runnable: Runnable = Runnable { }
    var isStarted = false

    var boost = 5000
    var timer = 10000
    var millis = 1000
    var hideImageBoost = 0.97

    val PREFS_NAME = "MyPrefsFile"


    private lateinit var mInterstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       val  settings = getSharedPreferences(PREFS_NAME, 0);
        val dialogShown = settings.getBoolean("dialogShown", false);
        if (!dialogShown) {
            // AlertDialog code here
            create()

           val editor:SharedPreferences.Editor = settings.edit ()
            editor.putBoolean("dialogShown", true);
            editor.apply()
        }



        imageArray = arrayListOf(
            imageView1, imageView2, imageView3, imageView4,
            imageView5, imageView6, imageView7, imageView8, imageView9, imageView10, imageView11,
            imageView12
        )

        timeText.text = getString(R.string.ClickStart)
        scoreText.text = getString(R.string.Score) + " 0"

        for (image in imageArray) {
            image.isEnabled = true
        }
        MobileAds.initialize(this, "ca-app-pub-6830476776807304~9367953202")

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-6830476776807304/8758536972"
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

    }


    //для начала игры
    fun onClick(view: View) {
        if (!isStarted) {
            playGame()
        }

    }


    fun create() {
        val alert = AlertDialog.Builder(this@MainActivity)
        alert.setTitle("Privacy Policy")
        alert.setMessage(Html.fromHtml("By clicking \"Ok\" you agree with the following terms\n,<a href=\"https://github.com/LinaRise/LinaRise.github.io/blob/master/Privacy%20Policy\">link</a>"));

        alert.setPositiveButton("Ok") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        alert.setCancelable(false)
        alert.show()
    }


    //прячем изображения
    fun hidingImage() {
        runnable = Runnable {
            for (image in imageArray) {
                image.visibility = View.INVISIBLE
            }
            val index = (0..11).random()
            imageArray[index].visibility = View.VISIBLE
            handler.postDelayed(runnable, millis.toLong())

        }
        handler.post(runnable)

    }


    //increses Score
    fun increaseScore(view: View) {
        if (isStarted) {
            score++
            scoreText.text = getString(R.string.Score) + " $score"
        } else {
            playGame()
        }

    }


    fun playGame() {
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        isStarted = true
        for (image in imageArray) {
            image.isEnabled = true
        }
        score = 0
        scoreText.text = getString(R.string.Score) + " $score"
        hidingImage()
        object : CountDownTimer(timer.toLong(), 1000) {

            override fun onFinish() {
                timeText.text = getString(R.string.TimeOff)
                handler.removeCallbacks(runnable)
                for (image in imageArray) {
                    image.visibility = View.INVISIBLE
                }

                val alert = AlertDialog.Builder(this@MainActivity)
                alert.setTitle(getString(R.string.ScoreAlert))
                alert.setMessage(getString(R.string.ScoreNumAlert) + " $score")

                alert.setPositiveButton("Ok") { dialog: DialogInterface, which: Int ->
                    timeText.text = getString(R.string.ClickStart)
                    for (image in imageArray) {
                        image.visibility = View.VISIBLE
                    }

                    scoreText.text = getString(R.string.Score) + " 0"


                    dialog.dismiss()
                }
                alert.setCancelable(false)
                alert.show()
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                    isStarted = false

                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.")
                    isStarted = false

                }

                timer += boost
                millis = (millis * hideImageBoost).toInt()
            }

            override fun onTick(millisUntilFinished: Long) {
                timeText.text = getString(R.string.Time) + " " + millisUntilFinished / 1000
            }

        }.start()
    }
}

