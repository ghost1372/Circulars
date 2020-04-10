package ir.mahdi.circulars.Fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.PdfFragmentBinding
import java.io.File

class PdfFragment : Fragment() {

    private lateinit var binding: PdfFragmentBinding
    lateinit var sharefile: File

    companion object {
        fun newInstance(): PdfFragment {
            return PdfFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when(item.itemId) {
            R.id.action_share -> {
                Tools().shareFile(sharefile, activity)
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
        binding = PdfFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            var pdf: File = File(arguments?.getString(Tools().FILE_KEY))
            sharefile = pdf

            if (Prefs(context!!).getIsDark()){
                binding.pdfView.fromFile(pdf)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .nightMode(true)
                    .load()
            }else{
                binding.pdfView.fromFile(pdf)
                    .enableDoubletap(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .load()
            }


        } catch (ex: Exception) {
            Tools().snack(view, "فایل خراب است")
        }
    }
}