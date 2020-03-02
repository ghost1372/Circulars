package ir.mahdi.circulars.Helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

class Prefs(val context: Context) {
    private val PREFS_NAME = "User_Data_KEY"
    private val SERVER = "SERVER_KEY"
    private val IS_FIRST_RUN = "IS_FIRST_RUN_KEY"
    private val IS_Dark = "IS_DARK_KEY"
    private val SWITCH_MODE = "SWITCH_MODE_KEY"
    private val SWITCH_MODE_MESSAGE = "SWITCH_MODE_MESSAGE_KEY"
    private val MULTI_SERVER_MODE = "MULTI_SERVER_MODE_KEY"
    private val MULTI_SERVER = "MULTI_SERVER_KEY"

    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setIsFirstRun(value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(IS_FIRST_RUN, value)
        editor.apply()
    }

    fun setIsDark(value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(IS_Dark, value)
        editor.apply()
    }

    fun setServerIndex(status: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(SERVER, status)
        editor.apply()
    }

    fun setSwitchMode(status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(SWITCH_MODE, status)
        editor.apply()
    }

    fun setSwitchModeMessage(status: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(SWITCH_MODE_MESSAGE, status)
        editor.apply()
    }

    fun setIsMultiServer(status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(MULTI_SERVER_MODE, status)
        editor.apply()
    }



    fun setMultiServers(serverArrayJoinToString: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(MULTI_SERVER, serverArrayJoinToString)
        editor.apply()
    }

    fun getMultiServers(): Array<Int>? {

        val ints = sharedPref.getString(MULTI_SERVER,"0")!!.split(",").map { it.trim().toInt() }.toTypedArray()
        return ints
    }

    fun getIsMultiServer(): Boolean {
        return sharedPref.getBoolean(MULTI_SERVER_MODE, false)
    }

    fun getSwitchModeMessage(): String? {
        return sharedPref.getString(SWITCH_MODE, "خدمات رایگان بخشنامه به پایان رسیده است شما دیگر مجوز استفاده از این برنامه را ندارید")
    }

    fun getSwitchMode(): Boolean {
        return sharedPref.getBoolean(SWITCH_MODE, false)
    }

    fun getIsFirstRun(): Boolean {
        return sharedPref.getBoolean(IS_FIRST_RUN, true)
    }

    fun getIsDark(): Boolean {
        return sharedPref.getBoolean(IS_Dark, false)
    }

    // 321 is Bezine Rod
    fun getServerIndex(): Int {
        return sharedPref.getInt(SERVER, 321)
    }
}