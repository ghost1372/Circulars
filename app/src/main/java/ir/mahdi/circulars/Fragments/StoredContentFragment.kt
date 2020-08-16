package ir.mahdi.circulars.Fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mahdi.circulars.Adapter.MyItemDetailsLookup
import ir.mahdi.circulars.Adapter.OfflineAdapter
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.Model.OfflineModel
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.StoredContentFragmentBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class StoredContentFragment : Fragment(), CoroutineScope, OfflineAdapter.CircularsAdapterListener {

    private lateinit var binding: StoredContentFragmentBinding

    lateinit var navController: NavController
    lateinit var optionMenu: Menu // We Need Hide or Show Some Menu

    // for Argument Passed from another View
    private val STORED_SECTION_KEY = "STORED_SECTION_KEY"

    // Multi Select
    var isMultiSelect = false // We need to Multi Select Item for Deleting so we need Selection State
    var isSelectAll = false // We Need back to Normal State when clicking Select All Button
    var longArray: ArrayList<Long> = ArrayList() // Store All Items for Selecting All
    private var tracker: SelectionTracker<Long>? = null // This dude handle Multi Selection

    var itemsData = ArrayList<OfflineModel>()
    val ofAdapter = OfflineAdapter(this)
    var _path: String? = String()

    // Coroutine Stuffs
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    companion object {
        fun newInstance(Section_Key : Int): StoredContentFragment {
            return StoredContentFragment().apply {
                arguments = Bundle().apply {
                    putInt(STORED_SECTION_KEY, Section_Key)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getInt(STORED_SECTION_KEY)?.let {
            if (it == 0)
                _path = Tools()._Path(context)
            else if (it == 1)
                _path = Tools()._PathMultiServer(context)
            else
                _path = Tools()._PathMinistry(context)
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
        binding = StoredContentFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)

        binding.rc.layoutManager = LinearLayoutManager(activity)
        binding.rc.setHasFixedSize(true)
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
            val dir = File(_path)
            if (dir.isDirectory) {
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
                var dir = File(_path + it.name)
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

        var searchView = activity?.findViewById<SearchView>(R.id.searchView)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(s: String?): Boolean {
                ofAdapter.filter.filter(s.toString())
                return true
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
                    var file: File = File(_path)
                    if (!file.listFiles().isNullOrEmpty()){
                        if (!file.isDirectory) {
                            return@async
                        }
                        for (f in file.listFiles()){
                            if (f.isFile || f.isDirectory) {
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

    // On RecyclerView item Clicked
    override fun onCircularSelected(item: OfflineModel?) {

        // First we check if multi select is enabled or not, if not we can show dialog
        if (!isMultiSelect){

            var file: String = File(item!!.name).extension
            if (file == "pdf"){
                Tools().navigate(_path + item.name, file, navController, activity, view!!)
            }else{
                Tools().showFileChooser(_path + item.name, activity!!, context!!, navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}