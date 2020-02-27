package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.mahdi.circulars.databinding.NoVpnFragmentBinding

class NoVPNFragment : Fragment() {

    private lateinit var binding: NoVpnFragmentBinding

    companion object {
        fun newInstance(): NoVPNFragment {
            return NoVPNFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NoVpnFragmentBinding.inflate(inflater, container,false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btRetry.setOnClickListener{
            activity?.onBackPressed()
        }
    }
}