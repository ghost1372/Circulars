package ir.mahdi.circulars

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.mahdi.circulars.Adapter.IntroSliderAdapter
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Model.IntroSliderModel

class IntroActivity : AppCompatActivity() {

    lateinit var introSliderViewPager: ViewPager2
    lateinit var indicatorsContainer: LinearLayout
    lateinit var btnNext: MaterialButton
    lateinit var txtSkip: MaterialTextView

    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSliderModel(
                "به اپلیکیشن بخشنامه خوش آمدید",
                "دریافت بخشنامه های آموزش و پرورش در گوشی همراه شما",
                R.drawable.appicon
            ),
            IntroSliderModel(
                "ساده، سریع، زیبا",
                "در یک محیط ساده و زیبا به سرعت بخشنامه های منطقه دلخواهت رو دریافت و مشاهده کن",
                R.drawable.slider1
            ),
            IntroSliderModel(
                "پشتیبانی از وزارت و شهرستان ها",
                "بخشنامه های وزارت آموزش پرورش و تمامی شهرستان هایی که بصورت آنلاین بخشنامه ها رو قرار میدن قابل دریافته",
                R.drawable.slider2
            ),
            IntroSliderModel(
                "مشاهده آفلاین",
                "بعد از دانلود بدون نیاز به اینترنت بخشنامه ها رو مشاهده کن",
                R.drawable.slider3
            ),
            IntroSliderModel(
                "اشتراک گذاری",
                "بخشنامه دلخواهت رو با دیگران به اشتراک بزار",
                R.drawable.slider4
            ),
            IntroSliderModel(
                "پشتیبانی سریع",
                "مشکل داری؟ ما در سریعترین زمان به سوالاتت پاسخ میدیم",
                R.drawable.slider5
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Prefs(applicationContext).getIsFirstRun()){
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
            setContentView(R.layout.intro_activity)
            initIntro()
        }else{
            startMainActivity()
        }
    }

    fun initIntro(){
        introSliderViewPager = findViewById(R.id.introSliderViewPager)
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        btnNext = findViewById(R.id.buttonNext)
        txtSkip = findViewById(R.id.textSkipIntro)

        introSliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)

        introSliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        btnNext.setOnClickListener{
            if (introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount){
                introSliderViewPager.currentItem += 1
            }else{
                startMainActivity()
            }
        }

        txtSkip.setOnClickListener{
            startMainActivity()
        }
    }

    fun startMainActivity(){
        Intent(applicationContext, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    fun setupIndicators(){
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8,0,8,0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(ContextCompat.getDrawable(applicationContext,
                    R.drawable.indicator_inactive)
                )
                this?.layoutParams = layoutParams
            }
            indicatorsContainer.addView(indicators[i])
        }
    }
    fun setCurrentIndicator(index: Int){
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount){
            val imageView = indicatorsContainer[i] as ImageView
            if (i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active)
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive)
                )
            }
        }
    }

    // Set Application Font
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }
}