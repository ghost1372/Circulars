package ir.mahdi.circulars.Helper

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.google.android.material.snackbar.Snackbar
import ir.mahdi.circulars.BuildConfig
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.R
import java.io.File
import java.net.URLConnection
import java.util.*


class Tools {

    var FILE_KEY = "FILE_NAME" //File name as a Key when passing argument between Views
    var _RawFileName = "download.raw" // Download Files with specific name
    var _TempFileName = "/temp.png" // To Create Temporary png file when sharing a Tif image

    // Return Application Directory, Because of API 29 limitation we need to check it and change directory
    fun _Path(context: Context?): String? {
        return if (Build.VERSION.SDK_INT >= 29) { // because of new storage privacy in Android >= Q
            context?.externalCacheDir!!.path + "/بخشنامه/" + Prefs(context).getServerIndex() + "/"
        } else {
            "/sdcard/بخشنامه/" + Prefs(context!!).getServerIndex() + "/"
        }
    }

    // Set Application Language for RTL or LTR for example we can pass "fa" and our app will be RTL
    fun setLanguage(lang: String, context: Context?) {
        val localeNew = Locale(lang)
        Locale.setDefault(localeNew)
        val res: Resources = context!!.resources
        val newConfig = Configuration(res.getConfiguration())
        newConfig.locale = localeNew
        newConfig.setLayoutDirection(localeNew)
        res.updateConfiguration(newConfig, res.getDisplayMetrics())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newConfig.setLocale(localeNew)
            context.createConfigurationContext(newConfig)
        }
    }

    // Return Current Region Text
    fun getCurrentRegion(context: Context?) : String {
        return context!!.resources.getStringArray(R.array.server)[Prefs(context).getServerIndex()]
    }

    // Check if Storage Permission Granted or not and if not ask from user to grant it
    fun isStoragePermissionGranted(
        activity: Activity?,
        context: Context
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                val PERMISSIONS = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(activity!!, PERMISSIONS, 1)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    // Show Snackbar with RTL Layout
    fun snack(view: View, text: String){
        val snackbar =
            Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val view =
                snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        snackbar.show()
    }

    // Open Excel and Word files in aa External Application
    private fun luanchOfficeReader(path: String, activity: Activity?, view: View){
        val file = File(path)
        val uri = FileProvider.getUriForFile(
            activity!!.applicationContext,
            BuildConfig.APPLICATION_ID.toString() + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)

        //file is word
        if (file.extension.contains("doc")){
            intent.setDataAndType(uri, "application/word")

        }else{
            // file is excel
            intent.setDataAndType(uri, "application/excel")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            snack(view,"برنامه ای برای نمایش فایل ورد/اکسل روی گوشی پیدا نشد، لطفا برنامه آفیس را روی گوشی خود نصب کنید")
        }
    }

    // Open Pdf, Images and Office files
    fun navigate(path: String, ext: String, navController: NavController, activity: Activity?, view: View){

        // we need to hide toolbar items we dont need searchview
        (activity as MainActivity).toolbarElementsVisiblity(false)

        var bundle = bundleOf(Tools().FILE_KEY to path) // passing argument to a bundle
        if (ext == "pdf"){
            navController.navigate(R.id.pdfFragment, bundle)
        }else if (ext == "xls" || ext == "xlsx" || ext == "csv" || ext == "doc" || ext == "docx") {
            luanchOfficeReader(path, activity, view)
        } else{
            // everything that isnt pdf and office like Images
            navController.navigate(R.id.imageFragment, bundle)
        }
    }

    // Share Files
    fun shareFile(file: File, activity: Activity?) {
        try {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            val intentShareFile = Intent(Intent.ACTION_SEND)
            intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
            intentShareFile.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse("file://" + file.absolutePath)
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "بخشنامه ${file.nameWithoutExtension}")
            activity?.startActivity(Intent.createChooser(intentShareFile, "اشتراک گذاری بخشنامه"))
        } catch (e: java.lang.Exception) {
        }
    }

    // Clear Generated Temp.png when sharing tif file
    fun clearShareTemp(context: Context?){
        val del =
            File(_Path(context) + _TempFileName)
        if (del.exists()) del.delete()
    }
}