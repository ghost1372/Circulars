package ir.mahdi.circulars.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import ir.mahdi.circulars.BuildConfig
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.AboutFragmentBinding

class AboutFragment : Fragment() {

    private lateinit var binding: AboutFragmentBinding

    companion object {
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AboutFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    // Handle Item Selection
    fun init(){

        //get App Version from Build.gradle
        binding.txtVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        binding.txtBuild.text = getString(R.string.build, BuildConfig.VERSION_CODE.toString())

        binding.lvEmail.setOnClickListener{
            sendFeedback()
        }

        binding.lvStar.setOnClickListener{
            giveStar()
        }

        binding.lvTelegram.setOnClickListener{
            sendPMToTelegram()
        }

        binding.lvChangelog.setOnClickListener{
            getChangelog()
        }
    }

    // Show Changelog In a Dialog
    fun getChangelog(){
        MaterialDialog(context!!).show {
            title(R.string.changeLogTitle)
            listItems(R.array.changeLog)
            positiveButton(R.string.close)
        }
    }

    // Send PM In Telegram
    fun sendPMToTelegram(){
        val url = context!!.getString(R.string.telegram_user)
        val next = Intent(Intent.ACTION_VIEW)
        try {
            next.data = Uri.parse(url)
            context!!.startActivity(next)
        } catch (e: java.lang.Exception) {
        }
    }

    // Give star in Store
    fun giveStar(){
        try {
            val intents = Intent(Intent.ACTION_EDIT)
            intents.data = Uri.parse("bazaar://details?id=ir.mahdi.circulars")
            intents.setPackage("com.farsitel.bazaar")
            startActivity(intents)
        } catch (e: Exception) {
            Tools().snack(view!!,  getString(R.string.NotExist))
        }
    }

    // Send feedback with email
    fun sendFeedback() {
        var body: String =
            "لطفا اطلاعات را حذف نکنید\n----------------------\n نسخه برنامه: " + BuildConfig.VERSION_NAME + " شماره بیلد : " + BuildConfig.VERSION_CODE.toString() +
                "\n نسخه سیستم عامل : " + Build.VERSION.RELEASE + " , " + Build.VERSION.SDK_INT + "\n برند دستگاه : " + Build.BRAND + " , " +
                Build.MODEL + " , " + Build.MANUFACTURER
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf<String>(context!!.getString(R.string.email))
        )
        intent.putExtra(Intent.EXTRA_SUBJECT, "گزارش اشکال از اپ بخشنامه")
        intent.putExtra(Intent.EXTRA_TEXT, body)
        context!!.startActivity(
            Intent.createChooser(
                intent,
                context!!.getString(R.string.choose_email_client)
            )
        )
    }
}
