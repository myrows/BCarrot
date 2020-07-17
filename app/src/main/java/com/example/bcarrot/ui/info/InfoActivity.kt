package com.example.bcarrot.ui.info

import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.bcarrot.R
import com.example.bcarrot.SliderAdapter


class InfoActivity : AppCompatActivity() {
    lateinit var mSlideViewPager: ViewPager
    lateinit var mDotLayout: LinearLayout
    lateinit var mDots: Array<TextView?>
    lateinit var sliderAdapter: SliderAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        mDotLayout = findViewById(R.id.dotsLayout)
        mSlideViewPager = findViewById(R.id.slideViewPager)
        sliderAdapter = SliderAdapter()
        mSlideViewPager!!.adapter = sliderAdapter;

        addDotsIndicator(0);

        mSlideViewPager!!.addOnPageChangeListener(viewListener)
    }

    fun addDotsIndicator(position: Int) {
        mDots = arrayOfNulls(4)
        mDotLayout!!.removeAllViews()
        for (i in mDots.indices) {
            mDots[i] = TextView(this)
            mDots[i]!!.text = Html.fromHtml("&#8226;")
            mDots[i]!!.textSize = 35f
            mDots[i]!!
                .setTextColor(resources.getColor(R.color.fbutton_color_cloudsTransparent))
            mDotLayout!!.addView(mDots[i])
        }
        if (mDots.isNotEmpty()) {
            mDots[position]!!.setTextColor(resources.getColor(R.color.fbutton_color_clouds))
        }
    }

    var viewListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            addDotsIndicator(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }
}