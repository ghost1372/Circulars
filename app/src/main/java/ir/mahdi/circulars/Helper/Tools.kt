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
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.fileChooser
import com.google.android.material.snackbar.Snackbar
import com.hzy.libp7zip.P7ZipApi
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.R
import java.io.File
import java.net.URLConnection
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.util.*
import javax.net.ssl.*


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

    // Return Ministry Path
    fun _PathMinistry(context: Context?): String? {
        return if (Build.VERSION.SDK_INT >= 29) { // because of new storage privacy in Android >= Q
            context?.externalCacheDir!!.path + "/بخشنامه/وزارت/" + Prefs(context).getServerIndex() + "/"
        } else {
            "/sdcard/بخشنامه/وزارت/" + Prefs(context!!).getServerIndex() + "/"
        }
    }

    // Return MultiServer directory
    fun _PathMultiServer(context: Context?): String? {
        return if (Build.VERSION.SDK_INT >= 29) { // because of new storage privacy in Android >= Q
            context?.externalCacheDir!!.path + "/بخشنامه/چندسرور/" + Prefs(context).getServerIndex() + "/"
        } else {
            "/sdcard/بخشنامه/چندسرور/" + Prefs(context!!).getServerIndex() + "/"
        }
    }

    // Set Application Language for RTL or LTR for example we can pass "fa" and our app will be RTL
    fun setLanguage(lang: String, context: Context?) {
        val localeNew = Locale(lang)
        Locale.setDefault(localeNew)
        val res: Resources = context!!.resources
        val newConfig = Configuration(res.configuration)
        newConfig.locale = localeNew
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newConfig.setLayoutDirection(localeNew)
            res.updateConfiguration(newConfig, res.displayMetrics)
            newConfig.setLocale(localeNew)
            context.createConfigurationContext(newConfig)
        }
    }

    // Return Current Region Text
    fun getCurrentRegion(context: Context?, IsSettingView: Boolean) : String {
        return if (IsSettingView){
            context!!.resources.getStringArray(R.array.server)[Prefs(context).getServerIndex()]
        }else{
            if (Prefs(context!!).getIsMultiServer()){
                ""
            }else{
                context.resources.getStringArray(R.array.server)[Prefs(context).getServerIndex()]
            }
        }
    }

    // Return Current Multi Region Text
    fun getCurrentMultiRegion(context: Context?) : String {
        val arrServer: Array<String> = context!!.resources.getStringArray(R.array.server)
        var arrText: String = ""
        for (item in Prefs(context).getMultiServers()!!.iterator()){
            arrText += "\n" + arrServer[item]
        }
        return arrText
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
                val permissions = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(activity!!, permissions, 1)
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

        val myMime: MimeTypeMap = MimeTypeMap.getSingleton()
        val newIntent = Intent(Intent.ACTION_VIEW)

        val mimeType: String =
            myMime.getMimeTypeFromExtension(file.extension).toString()
        if (Build.VERSION.SDK_INT >= 24) {
            newIntent.setDataAndType(FileProvider.getUriForFile(activity!!.applicationContext, activity.applicationContext.packageName.toString() +
                ".provider", file), mimeType)
        }else{
            newIntent.setDataAndType(Uri.fromFile(file), mimeType)
        }
        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            activity!!.startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            snack(view,"برنامه ای برای نمایش فایل ورد/اکسل روی گوشی پیدا نشد، لطفا برنامه آفیس را روی گوشی خود نصب کنید")
        }
    }

    // Open Pdf, Images and Office files
    fun navigate(path: String, ext: String, navController: NavController, activity: Activity?, view: View){

        // we need to hide toolbar items we dont need searchview
        (activity as MainActivity).toolbarElementsVisiblity(false)

        var bundle = bundleOf(FILE_KEY to path) // passing argument to a bundle
        if (ext == "pdf"){
            navController.navigate(R.id.pdfFragment, bundle)
        }else if (ext == "xls" || ext == "xlsx" || ext == "csv" || ext == "doc" || ext == "docx" || ext == "rar" ||
                ext == "zip") {
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

    // Get Random Color
    fun getRandomMaterialColor(typeColor: String, resources: Resources, activity: Activity): Int {
        var returnColor: Int = Color.GRAY
        val arrayId = resources.getIdentifier(
            "mdcolor_$typeColor",
            "array",
            activity.packageName
        )
        if (arrayId != 0) {
            val colors = resources.obtainTypedArray(arrayId)
            val index = (Math.random() * colors.length()).toInt()
            returnColor = colors.getColor(index, Color.GRAY)
            colors.recycle()
        }
        return returnColor
    }

    // Extract Compressed File with P7ZipApi
    fun runCommand(
        path: String,
        titleForPdf: String,
        navController: NavController,
        activity: Activity,
        view: View,
        dialog : MaterialDialog
    ) {

        // Extract Compressed File
        var cmd: String = getExtractCmd(path + _RawFileName, path + titleForPdf)

        io.reactivex.Observable.create(ObservableOnSubscribe<Int?> { e ->
            val ret = P7ZipApi.executeCommand(cmd)
            e.onNext(ret)
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                try {
                    val file = File(path + titleForPdf)

                    if (file.exists()) {
                        val fileToDelete = File(path + _RawFileName)
                        fileToDelete.delete()
                    } else {
                        //if file not exist so it must be pdf because it cant extracted
                        //select raw file for renaming
                        val fileToMovePdf = File(path + _RawFileName)
                        //select path for pdf
                        val destination = File("$file.pdf")
                        //rename raw to pdf in new path
                        fileToMovePdf.renameTo(destination)

                        dialog.dismiss()
                        //load pdf
                        navigate(destination.toString(),"pdf" ,navController,activity,view)
                    }
                } catch (e:Exception){}
            }
    }

    // P7ZipApi Command
    fun getExtractCmd(archivePath: String, outPath: String): String {
        return String.format("7z x '%s' '-o%s' -aoa", archivePath, outPath);
    }

    // Fix IlegalCharacter and Limit to 120 Character
    fun FixIlegalCharacter(value: String): String { // if text contain / character it must be removed
        var value = value
        if (value.contains("/")) value = value.replace("/".toRegex(), "")
        // check if characters are more than 120 or not becuase folder name cant be more than 127 (127 chars is 254 bytes)
        if (value.length > 120) value = value.substring(0, 120)
        return value
    }

    // Show File Chooser
    fun showFileChooser(initalPath: String, activity: Activity, context: Context, navController: NavController) {
        if (isStoragePermissionGranted(activity,context)){
            var initDirectory: File = File(initalPath)
            MaterialDialog(context).show {
                fileChooser(context, allowFolderCreation = false,initialDirectory = initDirectory) { _, file ->
                    navigate(file.absolutePath, file.extension, navController, activity, view)
                }
                negativeButton(R.string.cancelTask)
                positiveButton(R.string.preview)
            }
        }
    }
    fun trustServer(): SSLSocketFactory {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })
            val sc: SSLContext = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            return sc.socketFactory
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

}