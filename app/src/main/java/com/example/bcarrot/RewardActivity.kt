package com.example.bcarrot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_reward.*

class RewardActivity : AppCompatActivity() {

    lateinit var rewardedAd : RewardedAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        loadAd()
        showAd()

    }

    fun loadAd() {
        rewardedAd = RewardedAd( this, "ca-app-pub-3940256099942544/5224354917" )
        var callback : RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdFailedToLoad(p0: Int) {
                super.onRewardedAdFailedToLoad(p0)

            }

            override fun onRewardedAdLoaded() {
                super.onRewardedAdLoaded()
                Toast.makeText(this@RewardActivity, "Loaded", Toast.LENGTH_LONG).show()
            }
        }
        rewardedAd.loadAd( AdRequest.Builder().build(), callback )
    }

    fun showAd() {
        if ( rewardedAd.isLoaded ) {
            rewardedAd.show(this@RewardActivity,
            object : RewardedAdCallback() {
                override fun onRewardedAdFailedToShow(p0: Int) {
                    super.onRewardedAdFailedToShow(p0)
                    Toast.makeText(this@RewardActivity, "Failed", Toast.LENGTH_LONG).show()
                }
                override fun onUserEarnedReward(p0: RewardItem) {
                    super.onUserEarnedReward(p0)
                    Toast.makeText(this@RewardActivity, "100 Bcoins", Toast.LENGTH_LONG).show()
                }

                override fun onRewardedAdClosed() {
                    super.onRewardedAdClosed()
                }
            })
        }
    }
}