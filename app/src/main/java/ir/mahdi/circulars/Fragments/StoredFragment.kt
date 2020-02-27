package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.fileChooser
import com.google.android.material.textfield.TextInputEditText
import ir.mahdi.circulars.Adapter.MyItemDetailsLookup
import ir.mahdi.circulars.Adapter.OfflineAdapter
import ir.mahdi.circulars.Helper.DividerItemDecoration
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.Model.OfflineModel
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.StoredFragmentBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class StoredFragment : Fragment(), CoroutineScope, OfflineAdapter.CircularsAdapterListener {

    private lateinit var binding: StoredFragmentBinding

    lateinit var navController: NavController
    lateinit var optionMenu: Menu // We Need Hide or Show Some Menu

    // Multi Select
    var isMultiSelect = false // We need to Multi Select Item for Deleting so we need Selection State
    var isSelectAll = false // We Need back to Normal State when clicking Select All Button
    var longArray: ArrayList<Long> = ArrayList() // Store All Items for Selecting All
    private var tracker: SelectionTracker<Long>? = null // This dude handle Multi Selection

    var itemsData = ArrayList<OfflineModel>()
    val ofAdapter = OfflineAdapter(this)

    // Coroutine Stuffs
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        fun newInstance(): StoredFragment {
            return StoredFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_delete, menu)
        optionMenu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when(item.itemId) {
            R.id.action_delete -> {
                deleteItems()
                true
            }
            R.id.action_all-> {
                selectAll()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StoredFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)

        binding.rc.layoutManager = LinearLayoutManager(activity)
        binding.rc.setHasFixedSize(true)
        binding.rc.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL,10,10))
        binding.rc.adapter = ofAdapter

        initSearch()

        getLocalItems()

        // Implement Multi Select
        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            binding.rc,
            StableIdKeyProvider(binding.rc),
            MyItemDetailsLookup(binding.rc),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onItemStateChanged(key: Long, selected: Boolean) {
                    super.onItemStateChanged(key, selected)

                    // If User Selected Multiple Items, we need to Hide Toolbar (SearchView) and Show Menu Options
                    if (tracker!!.hasSelection()){
                        isMultiSelect = true
                        (activity as MainActivity).toolbarElementsVisiblity(false)
                        optionMenu.findItem(R.id.action_delete).isVisible = true
                        optionMenu.findItem(R.id.action_all).isVisible = true
                    }
                    else
                    {
                        // When User Deselected All Items We Need to Show Toolbar (SearchView) and Hide Menu Option
                        isMultiSelect = false
                        (activity as MainActivity).toolbarElementsVisiblity(true)
                        optionMenu.findItem(R.id.action_delete).isVisible = false
                        optionMenu.findItem(R.id.action_all).isVisible = false
                    }
                }
            })

        ofAdapter.tracker = tracker
    }


    // Delete Items and Directory
    fun deleteItems(){

        // if All Item Select we can easily Delete Whole Folder
        if (isSelectAll){
            val dir = File(Tools()._Path(context))
            if (dir.isDirectory()) {
                deleteAll(dir)
                itemsData.clear()
            }
        }
        else
        {
            // So user Selected Some Items, We need create a map from items that Selected and deleting it
            val list = tracker!!.selection.map {
                ofAdapter.itemsList[it.toInt()]
            }.toList()

            list.forEach{
                var dir = File(Tools()._Path(context) + it.name)
                if (dir.isDirectory){
                    deleteAll(dir)
                }else{
                    dir.delete()
                }
                itemsData.remove(it)
            }
        }
        ofAdapter.notifyDataSetChanged()
        tracker!!.clearSelection()
    }

    // This dude Delete whole File with Directory and SubDirectory
    fun deleteAll(dir: File){
        if (dir.isDirectory){
            for (child in dir.listFiles()){
                deleteAll(child)
            }
        }
        dir.delete()
    }

    // Handle Select All button, First Select All, Second Deselect All items
    fun selectAll(){

        // If LongArray is Not Empty that means we have selected All items so we must Deselect All
        if (longArray.isNotEmpty()){
            tracker!!.clearSelection()
            longArray.clear()
            isSelectAll = false
        }
        else
        {
            // LongArray is Empty So We Need To Select All Items
            isSelectAll = true
            itemsData.forEachIndexed { i, item ->
                longArray.add(i.toLong())
            }
            tracker!!.setItemsSelected(longArray.asIterable(),true)
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
                ofAdapter.filter.filter(s.toString())
            }
        })
    }

    // Find Stored Items
    fun getLocalItems(){
        binding.lytNoitem.visibility = View.GONE
        binding.rc.visibility = View.VISIBLE
        itemsData.clear()

        Tools().clearShareTemp(context) // Clear Temp.png File If Exist (Because of Tif Converted file)

        lifecycleScope.launch {
            val operation = async(Dispatchers.IO) {
                if (Tools().isStoragePermissionGranted(activity,context!!)){
                    var file: File = File(Tools()._Path(context))
                    if (!file.listFiles().isNullOrEmpty()){
                        if (file.isDirectory() == false) {
                            return@async;
                        }
                        for (f in file.listFiles()){
                            if (f.isFile() || f.isDirectory()) {
                                itemsData.add(OfflineModel(f.name))
                            }
                        }
                    }
                }
            }
            operation.await() // wait for result of I/O operation without blocking the main thread

            // update views
            withContext(Dispatchers.Main){
                if (itemsData.isEmpty()){
                    binding.rc.visibility = View.GONE
                    binding.lytNoitem.visibility = View.VISIBLE
                }else{
                    ofAdapter.itemsList = itemsData

                    // because of Search we need temp
                    ofAdapter.temp = itemsData
                    ofAdapter.notifyDataSetChanged()
                    binding.rc.visibility = View.VISIBLE
                    binding.lytNoitem.visibility = View.GONE
                }
            }
        }
    }

    // Open File Chooser
    private fun showFileChooser(initalPath: String) {
       if (Tools().isStoragePermissionGranted(activity,context!!)){
           var initDirectory: File = File(initalPath)
           MaterialDialog(context!!).show {
               fileChooser(context, allowFolderCreation = false,initialDirectory = initDirectory) { _, file ->
                   Tools().navigate(file.absolutePath, file.extension,navController, activity, view)
               }
               negativeButton(R.string.cancelTask)
               positiveButton(R.string.preview)
           }
       }
    }

    // On RecyclerView item Clicked
    override fun onCircularSelected(item: OfflineModel?) {

       // First we check if multi select is enabled or not, if not we can show dialog
       if (!isMultiSelect){

           var file: String = File(item!!.name).extension
           if (file == "pdf"){
               Tools().navigate(Tools()._Path(context) + item.name, file, navController, activity, view!!)
           }else{
               showFileChooser(Tools()._Path(context)+ item.name)
           }
       }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}