package ir.mahdi.circulars

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.pushpole.sdk.PushPole
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.mahdi.circulars.CustomView.SearchView
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Helper.Tools
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Navigation
    lateinit var navController: NavController
    lateinit var navigation: NavigationView

    lateinit var toolbar: Toolbar

    // MainActivity elements
    lateinit var txtVersion: MaterialTextView
    lateinit var txtBuild: MaterialTextView
    lateinit var txtCurrent_Region: MaterialTextView
    lateinit var icMoon: AppCompatImageView
    lateinit var swTheme: SwitchMaterial
    lateinit var drawer: DrawerLayout

    // SearchView
    lateinit var searchView: SearchView
    lateinit var close_Search: View
    lateinit var open_Search: View

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Prefs(applicationContext).getIsDark()){
            setTheme(R.style.Dark)
        }else{
            setTheme(R.style.Light)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Elements
        toolbar = findViewById(R.id.toolbar)
        txtVersion = findViewById(R.id.footer_version)
        txtBuild = findViewById(R.id.footer_build)
        drawer = findViewById(R.id.drawer_layout)
        navigation = findViewById(R.id.navigationView)
        txtCurrent_Region = findViewById(R.id.currentRegion)
        swTheme = findViewById(R.id.sw_theme)
        searchView = findViewById(R.id.searchView)
        close_Search = findViewById(R.id.close_search_button)
        open_Search = findViewById(R.id.open_search_button)


        val headerview: View = navigation.getHeaderView(0)
        icMoon =
            headerview.findViewById<View>(R.id.ic_moon) as AppCompatImageView


        setupNavigation()

        currentServerVisibility()

        Tools().isStoragePermissionGranted(this,applicationContext)

        //First Run
        if (Prefs(applicationContext).getIsFirstRun())
        {
            selectServer()
        }

        analyticsService()
        showEmergencyMessage()
    }

    // Setup Analytics Services
    fun analyticsService(){
        AppCenter.start(
            application, "8d84bc2c-968a-479f-83d4-ca891ab3bac1",
            Analytics::class.java, Crashes::class.java
        )

        PushPole.initialize(this,true);
    }

    private fun showEmergencyMessage() {
        if (Prefs(applicationContext).getSwitchMode()) {
            MaterialDialog(this).show {
                title(R.string.titleEMG)
                message(text = Prefs(applicationContext).getSwitchModeMessage())
                positiveButton(R.string.positiveEMG)
                cancelable(false)
                noAutoDismiss()
                positiveButton {
                    finishAffinity()
                    exitProcess(0)
                }
            }
        }
    }

    // hide or show current server text when SearchView is open/close
    fun currentServerVisibility(){
        close_Search.setOnClickListener{
            searchView.closeSearch()
            txtCurrent_Region.visibility = View.VISIBLE
            hideKeyboard()
        }
        open_Search.setOnClickListener{
            searchView.openSearch()
            txtCurrent_Region.visibility = View.GONE
            hideKeyboard()
        }
    }

    fun setupNavigation(){
        setSupportActionBar(toolbar)

        //Require API 17
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR){
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL)
            }
        }
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer)
        NavigationUI.setupWithNavController(navigation, navController)
        navigation.setNavigationItemSelectedListener(this)

        // Select First Item for Default
        onNavigationItemSelected(navigation.menu.getItem(0))

        txtCurrent_Region.setText(Tools().getCurrentRegion(applicationContext))

        // set Drawer Footer items text
        txtVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME))
        txtBuild.setText(getString(R.string.build, BuildConfig.VERSION_CODE.toString()))

        // Change Theme
        swTheme.isChecked = Prefs(applicationContext).getIsDark()
        swTheme.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            Prefs(applicationContext).setIsDark(isChecked)
            recreate()
        })

        // Change Theme When Clicking on Moon icon
        icMoon.setOnClickListener{
            swTheme.isChecked = !swTheme.isChecked
        }

    }

    // This Method can be Called from Setting Fragment
    fun changeTheme(isDark: Boolean){
        swTheme.isChecked = isDark
    }

    // hide or show SearchView and CurrentRegion text
    fun toolbarElementsVisiblity(isVisible: Boolean){
       if (isVisible){
           searchView.visibility = View.VISIBLE
           txtCurrent_Region.visibility = View.VISIBLE
       }else{
           searchView.visibility = View.GONE
           txtCurrent_Region.visibility = View.GONE
           hideKeyboard()
       }
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Handle Drawer item selected
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.setChecked(true)
        drawer.closeDrawers()
        val id: Int = item.getItemId()

        when(id){
            R.id.ministry -> {
                toolbarElementsVisiblity(false)
                navController.navigate(R.id.ministryFragment)
                item.setChecked(true)
            }
            R.id.stored -> {
                toolbarElementsVisiblity(false)
                navController.navigate(R.id.storedFragment)
            }
            R.id.setting -> {
                toolbarElementsVisiblity(false)
                navController.navigate(R.id.settingFragment)
            }
            R.id.about -> {
                toolbarElementsVisiblity(false)
                navController.navigate(R.id.aboutFragment)
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            Navigation.findNavController(this, R.id.nav_host_fragment),
            drawer
        )
        return false
    }

    // Set Application Font
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    // Choose Server in first Run
    fun selectServer(){
        MaterialDialog(this).show {
            title(R.string.select_region)
            cancelable(false)
            listItemsSingleChoice(R.array.server, initialSelection =  Prefs(context).getServerIndex()) { _, index, text ->
                Prefs(context).setServerIndex(index)
                if (Prefs(applicationContext).getIsFirstRun())
                {
                    Prefs(applicationContext).setIsFirstRun(false)
                }
            }
            positiveButton(R.string.select_location)
            negativeButton(R.string.NegativeButton){checked->
                if (Prefs(context).getIsFirstRun()){
                    finishAffinity()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
