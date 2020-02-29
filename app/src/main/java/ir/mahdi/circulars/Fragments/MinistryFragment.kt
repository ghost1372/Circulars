package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.downloader.OnCancelListener
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloader.download
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import ir.mahdi.circulars.Adapter.CircularAdapter
import ir.mahdi.circulars.Helper.DividerItemDecoration
import ir.mahdi.circulars.Helper.NullHostNameVerifier
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.Model.CircularModel
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.MinistryFragmentBinding
import kotlinx.android.synthetic.main.appbar_main.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.File
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

class MinistryFragment : Fragment(), CircularAdapter.CircularsAdapterListener, CoroutineScope, SwipeRefreshLayout.OnRefreshListener {

    // We Need This Variables Because of Fixing Fragment ReCreating View Issue
    var hasInitializedRootView = false
    private var rootView: View? = null

    private lateinit var binding: MinistryFragmentBinding
    lateinit var navController: NavController

    var itemsData = ArrayList<CircularModel>() // All Item Go here
    lateinit var adapter: CircularAdapter

    // Coroutine Stuffs
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        fun newInstance(): MinistryFragment {
            return MinistryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            // Inflate the layout for this fragment
            binding = MinistryFragmentBinding.inflate(inflater, container, false)
            rootView = binding.root
        } else {
            // Do not inflate the layout again.
            // The returned View of onCreateView will be added into the fragment.
            // However it is not allowed to be added twice even if the parent is same.
            // So we must remove rootView from the existing parent view group
            // (it will be added back).
            (rootView?.getParent() as? ViewGroup)?.removeView(rootView)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)

        initSearch()

        // Get Curent Region Text
        (activity as MainActivity).currentRegion.setText(Tools().getCurrentRegion(context))

        if (!hasInitializedRootView) {
            hasInitializedRootView = true
            // Change Application layout to RTL
            Tools().setLanguage("fa",context)

            binding.swipeRefresh.setOnRefreshListener(this)
            binding.swipeRefresh.setColorSchemeColors(Tools().getRandomMaterialColor("400",resources,activity!!))

            // Init ArrayList
            itemsData = ArrayList()

            // Fix SSl Certification Issue
            HttpsURLConnection.setDefaultHostnameVerifier(NullHostNameVerifier())

            binding.rc.apply {
                layoutManager = LinearLayoutManager(activity)
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(context,
                        LinearLayoutManager.VERTICAL, 60,0)
                )
                adapter = CircularAdapter(itemsData, this@MinistryFragment)
            }

            binding.swipeRefresh.post(
                kotlinx.coroutines.Runnable { onRefresh() }
            )
        }
    }

    // Init SearchView
    fun initSearch(){

        // Show SearchView
        (activity as MainActivity).toolbarElementsVisiblity(true)
        var searchView = activity?.findViewById<TextInputEditText>(R.id.search_input_text)
        searchView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int) {
            }
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int) {
                if (::adapter.isInitialized){
                    adapter.filter.filter(s.toString())
                }
            }
        })
    }

    // Get Circulars from Web with Coroutine
    fun getCirculars(){
        //set visibility
        binding.lytNoitem.visibility = View.GONE
        binding.rc.visibility = View.GONE
        binding.lytProgress.visibility = View.VISIBLE
        binding.progress.progress = 0;
        binding.progress.isIndeterminate = true

        itemsData.clear()

        var url: String = "http://edari.medu.ir"

        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                try {
                    var doc: Document = Jsoup.connect(url).timeout(0).maxBodySize(0).ignoreHttpErrors(true).get()
                    val table: Elements = doc.select("table[class=\"table table-striped table-hover\"]")
                    for (myTable in table) {
                        Log.d("asdads", myTable.id())

                        val rows: Elements = myTable.select("tr")
                        withContext(Dispatchers.Main){
                            binding.progress.isIndeterminate = false
                        }
                        for (i in 1 until rows.size) {
                            binding.progress.progress = i
                            val row: Element = rows.get(i)
                            val cols: Elements = row.select("td")
                            val href: Elements = row.select("a")
                            val strhref: String = href.attr("href")

                            var status: String = ""
                            var date: String = cols.get(3).text()
                            var title: String = Tools().FixIlegalCharacter(cols[2].text())

                            val existCirculars =
                                File(Tools()._PathMinistry(context).toString() + title)
                            val existCircularsPdf =
                                File(Tools()._PathMinistry(context).toString() + title + ".pdf")
                            if (existCirculars.exists() || existCircularsPdf.exists()) {
                                status = getString(R.string.downloaded_Message)
                            }

                            itemsData.add(CircularModel(title,status,date,strhref,Tools().getRandomMaterialColor("400",resources,activity!!)))
                        }
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        navController.navigate(R.id.noVPNFragment)
                    }
                }
            }
            operation.await() // wait for result of I/O operation without blocking the main thread

            // update views
            withContext(Dispatchers.Main){
                binding.lytProgress.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (itemsData.isEmpty()){
                    binding.lytNoitem.visibility = View.VISIBLE
                }else{
                    adapter = CircularAdapter(itemsData, this@MinistryFragment)
                    binding.rc.adapter = adapter
                    adapter.notifyDataSetChanged()
                    binding.rc.visibility = View.VISIBLE
                }
            }
        }
    }
    // On RecyclerView item Clicked
    override fun onCircularSelected(item: CircularModel?) {
        var file: String = item!!.title
        var exist_pdf: File = File(Tools()._PathMinistry(context) + file + ".pdf")
        var exist_compressed: File = File(Tools()._PathMinistry(context) + file)

        // If PDF File Exist, So Selected File is a PDF and We Can Navigate to Show pdf
        if (exist_pdf.exists()) {
            Tools().navigate( exist_pdf.absolutePath, "pdf",navController,activity,view!!);
        }
        else
        {
            // If PDF File Not Exist So Selected item is a Compressed File
            if (exist_compressed.exists()){

                // If Compressed File Exist we can Show File Chooser
                Tools().showFileChooser(Tools()._PathMinistry(context) + file, activity!!, context!!, navController)
            }
            else
            {
                // If Compressed File Not Exist So We Need To Download it
                if (Tools().isStoragePermissionGranted(activity,context!!)) {
                    is_File_Exist = false
                    showCustomViewDialog(file, item.link)
                }
            }
        }
    }

    // Download Section

    var downloadId: Int = 0 // Download Id That We Can Cancel It
    var is_File_Exist = false // We Need This Because after Downloading We Need To Open File With Same Button
    var is_Busy = false // If File is Downloading We Cant Use Same Button Again
    lateinit var dialog: MaterialDialog

    private fun showCustomViewDialog(title: String, link: String) {
        dialog = MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT))

        // Setup custom view content
        dialog.show {
            title(R.string.sheet_title)
            customView(R.layout.bottom_sheet, scrollable = true, horizontalPadding = true)

            positiveButton(R.string.download) { dialog ->

                // is_Busy = False So We Need to Download or Show File Chooser, If is_Busy = True, Download Button Not Work Until Download Finish
                if (is_Busy == false) {
                    // If File Not Exist We Need To Download it
                    if (is_File_Exist == false) {
                        val status: MaterialTextView =
                            dialog.getCustomView().findViewById(R.id.sheet_status)
                        val sheet_progress: ContentLoadingProgressBar =
                            dialog.getCustomView().findViewById(R.id.sheet_progress)

                        dialog.cancelable(false) // While Downloading User Cant Cancel Dialog, Only Cancel Button Work for Canceling
                        noAutoDismiss() // Disable Auto Dismiss after Clicking Button

                        status.setText(R.string.downloading)

                        downloadId = download(
                            "http://edari.medu.ir/" + link,
                            Tools()._PathMinistry(context),
                            Tools()._RawFileName
                        )
                            .build()
                            .setOnProgressListener { progress ->
                                val progressPercent =
                                    progress.currentBytes * 100 / progress.totalBytes
                                sheet_progress.setProgress(progressPercent.toInt())
                                positiveButton(R.string.wait)
                                is_Busy = true
                            }
                            .setOnCancelListener(object : OnCancelListener {
                                override fun onCancel() {
                                    status.setText(R.string.cancel)
                                    binding.progress.setProgress(0)
                                    is_File_Exist = false
                                    is_Busy = false
                                }
                            })
                            .start(object : OnDownloadListener {
                                override fun onDownloadComplete() {
                                    try {
                                        dialog.cancelable(true)
                                        status.setText(getString(R.string.downloadCompleted))
                                        positiveButton(R.string.preview)

                                        Tools().runCommand(
                                            Tools()._PathMinistry(context).toString(),
                                            title,navController,activity!!,view,dialog
                                        )

                                        is_File_Exist = true
                                        is_Busy = false

                                    } catch (ex: Exception) {
                                    }
                                }

                                override fun onError(error: com.downloader.Error?) {
                                    status.setText(getString(R.string.error))
                                    binding.progress.setProgress(0)
                                    is_File_Exist = false
                                    is_Busy = false
                                }
                            })
                    } else {
                        // File Exist And We Show File Chooser
                        dismiss()
                        Tools().showFileChooser(Tools()._PathMinistry(context) + title, activity!!, context, navController)
                    }
                }
            }

            negativeButton(R.string.cancelTask) {
                PRDownloader.cancel(downloadId);
                is_File_Exist = false
                dismiss()
            }
        }

        val customView = dialog.getCustomView()
        val titleDesc: MaterialTextView = customView.findViewById(R.id.sheet_Desc)
        titleDesc.setText(title)
    }

    // Swipe Refresh
    override fun onRefresh() {
        getCirculars()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }
}
