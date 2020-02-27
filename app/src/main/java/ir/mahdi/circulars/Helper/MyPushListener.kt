package ir.mahdi.circulars.Helper

import com.pushpole.sdk.PushPoleListenerService
import org.json.JSONObject

class MyPushListener : PushPoleListenerService() {
    override fun onMessageReceived(customContent: JSONObject?, pushMessage: JSONObject?) {
        if (customContent == null || customContent.length() == 0) return  //json is empty
        //Turn off application if needed
        try {
            val s1 = customContent.getBoolean("status")
            val s2 = customContent.getString("content")
            Prefs(applicationContext).setSwitchMode(s1)
            Prefs(applicationContext).setSwitchModeMessage(s2)
        } catch (e: Exception) {
        }
    }
}