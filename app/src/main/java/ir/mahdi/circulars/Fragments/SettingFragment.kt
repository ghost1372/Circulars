package ir.mahdi.circulars.Fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.SettingFragmentBinding


class SettingFragment : Fragment() {

    private lateinit var binding: SettingFragmentBinding

    companion object {
        fun newInstance(): SettingFragment {
            return SettingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).toolbarElementsVisiblity(false)
        init()
    }

    fun init(){
        binding.txtCurrentServer.text = Tools().getCurrentRegion(context, true)

        if (Prefs(context!!).getIsMultiServer())
            binding.lvMultiRegion.visibility = View.VISIBLE
        else
            binding.lvMultiRegion.visibility = View.GONE

        binding.txtCurrentMultiServer.text = Tools().getCurrentMultiRegion(context)

        binding.txtCurrentSkin.text = Prefs(context!!).getSkinName()

        binding.swMultiServer.isChecked = Prefs(context!!).getIsMultiServer()
        binding.lvRegion.setOnClickListener{
            setRegion()
        }

        binding.lvSkin.setOnClickListener{
            MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                var items = R.array.skin
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    items = R.array.skinQ
                }
                listItems(items) { _, index, text ->
                    Prefs(context).setSkin(index)
                    binding.txtCurrentSkin.text = Prefs(context).getSkinName()
                    activity!!.finish();
                    startActivity(activity!!.getIntent());
                }
                positiveButton(R.string.select_theme)
                negativeButton(R.string.NegativeButton)
            }
        }

        binding.lvMultiRegion.setOnClickListener{
            setMultiRegion()
        }

        binding.swMultiServer.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{buttonView, isChecked->
            Prefs(context!!).setIsMultiServer(isChecked)
            if (isChecked)
                binding.lvMultiRegion.visibility = View.VISIBLE
            else
                binding.lvMultiRegion.visibility = View.GONE
        })
    }

    fun setRegion(){
        MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.select_region)
            cancelable(false)
            listItemsSingleChoice(R.array.server, initialSelection =  Prefs(context).getServerIndex()) { _, index, text ->
                Prefs(context).setServerIndex(index)
                binding.txtCurrentServer.text = Tools().getCurrentRegion(context, true)
            }
            positiveButton(R.string.select_location)
            negativeButton(R.string.NegativeButton)
        }
    }

    fun setMultiRegion(){
        MaterialDialog(context!!, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.select_region)

            listItemsMultiChoice(
                R.array.server
            ) { _, indices, text ->
                Prefs(context).setMultiServers(indices.joinToString())
                binding.txtCurrentMultiServer.text = Tools().getCurrentMultiRegion(context)

            }
            positiveButton(R.string.select_location)
            negativeButton(R.string.NegativeButton)
        }
    }
}
