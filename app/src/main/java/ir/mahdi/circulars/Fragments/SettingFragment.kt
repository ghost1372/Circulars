package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.textview.MaterialTextView
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.MainActivity
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.SettingFragmentBinding
import kotlinx.android.synthetic.main.image_fragment.*

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
        binding.txtCurrentServer.setText(Tools().getCurrentRegion(context))
        binding.swTheme.isChecked = Prefs(context!!).getIsDark()
        binding.lvRegion.setOnClickListener{
            setRegion()
        }

        // Change Theme
        binding.swTheme.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            Prefs(context!!).setIsDark(isChecked)
            (activity as MainActivity).changeTheme(isChecked)
        })
    }

    fun setRegion(){
        var txtCurrent = activity?.findViewById<MaterialTextView>(R.id.txtCurrentServer)

        MaterialDialog(context!!).show {
            title(R.string.select_region)
            cancelable(false)
            listItemsSingleChoice(R.array.server, initialSelection =  Prefs(context).getServerIndex()) { _, index, text ->
                Prefs(context).setServerIndex(index)
                txtCurrent?.setText(Tools().getCurrentRegion(context))
            }
            positiveButton(R.string.select_location)
            negativeButton(R.string.NegativeButton)
        }
    }
}
