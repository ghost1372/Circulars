package ir.mahdi.circulars.Helper

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/IRANSansMobile.ttf")
                            .build()
                    )
                )
                .build()
        )
        Fresco.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this);
    }
}