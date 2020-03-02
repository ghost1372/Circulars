package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.github.inflationx.calligraphy3.CalligraphyUtils
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

        binding.pager.setAdapter(ViewPager2Adapter())
        TabLayoutMediator(binding.tabLayout, binding.pager, TabLayoutMediator.TabConfigurationStrategy{tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tabState)
                1 -> tab.text = getString(R.string.tabMultiServer)
                2 -> tab.text = getString(R.string.tabMinistry)
            }
        }).attach()
        
        changeTabsFont()
    }

    // Calligraphy Cant Change TabLayout Font, so We Need Some Codes to do this
    fun changeTabsFont() {
        val vg = binding.tabLayout.getChildAt(0) as ViewGroup
        val tabsCount = vg.childCount
        for (j in 0 until tabsCount) {
            val vgTab = vg.getChildAt(j) as ViewGroup
            val tabChildsCount = vgTab.childCount
            for (i in 0 until tabChildsCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView) {
                    CalligraphyUtils.applyFontToTextView(
                        binding.tabLayout.context,
                        tabViewChild,
                        "fonts/IRANSansMobile.ttf"
                    )
                }
            }
        }
    }
    private inner class ViewPager2Adapter() : FragmentStateAdapter(this) {
        private val int_items = 3

        override fun createFragment(position: Int): Fragment {
            var fragment: Fragment? = null
            when (position) {
                0 -> fragment = StoredContentFragment.newInstance(0)
                1 -> fragment = StoredContentFragment.newInstance(1)
                2 -> fragment = StoredContentFragment.newInstance(2)
            }
            return fragment!!
        }

        override fun getItemCount(): Int {
            return int_items
        }

    }
}