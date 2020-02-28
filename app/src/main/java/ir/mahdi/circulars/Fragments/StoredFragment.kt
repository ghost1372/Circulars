package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.StoredFragmentBinding

class StoredFragment : Fragment() {

    private lateinit var binding: StoredFragmentBinding

    companion object {
        fun newInstance(): StoredFragment {
            return StoredFragment()
        }
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

        binding.pager.setAdapter(ViewPager2Adapter(fragmentManager, lifecycle))
        TabLayoutMediator(binding.tabLayout, binding.pager, TabLayoutMediator.TabConfigurationStrategy{tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tabState)
                1 -> tab.text = getString(R.string.tabMinistry)
            }
        }).attach()

    }

    private inner class ViewPager2Adapter(fm: FragmentManager?, lifecycle: Lifecycle) : FragmentStateAdapter(fm!!, lifecycle) {
        private val int_items = 2

        override fun createFragment(position: Int): Fragment {
            var fragment: Fragment? = null
            when (position) {
                0 -> fragment = StoredContentFragment.newInstance(false)
                1 -> fragment = StoredContentFragment.newInstance(true)
            }
            return fragment!!
        }

        override fun getItemCount(): Int {
            return int_items
        }

    }
}