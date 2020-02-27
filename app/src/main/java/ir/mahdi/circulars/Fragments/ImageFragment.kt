package ir.mahdi.circulars.Fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import ir.mahdi.circulars.Helper.Tools
import ir.mahdi.circulars.R
import ir.mahdi.circulars.databinding.ImageFragmentBinding
import org.beyka.tiffbitmapfactory.IProgressListener
import org.beyka.tiffbitmapfactory.TiffBitmapFactory
import org.beyka.tiffbitmapfactory.TiffConverter
import java.io.File

class ImageFragment : Fragment() {

    var isFileTif = false // if file is tiff we need to convert it to png so we must to know is file tif or not
    lateinit var shareFile: File // share file

    lateinit var options: TiffBitmapFactory.Options
    private lateinit var binding: ImageFragmentBinding


    companion object {
        fun newInstance(): ImageFragment {
            return ImageFragment()
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

                // if file is tiff we need to convert file to png
                if (isFileTif){
                    convertTiffToPng(shareFile)
                }else{
                    Tools().shareFile(shareFile, activity)
                }
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
        binding = ImageFragmentBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       try {
           var img: File = File(arguments?.getString(Tools().FILE_KEY)) // get argument that passed from another view
           var tifPages: Int = 0 // Store tif page numbers

           shareFile = img // we set img to shareFile so we can share it later

           options = TiffBitmapFactory.Options()

           if (img.extension.toLowerCase().contains("tif")) {
                isFileTif = true
               TiffBitmapFactory.decodeFile(img, options)
               var dirCount: Int = options.outDirectoryCount;

               // we need some btn for changing page
               binding.fabNext.visibility = View.VISIBLE
               binding.fabPrev.visibility = View.VISIBLE
               binding.tifViewNumber.visibility = View.VISIBLE

               binding.tifViewNumber.setText(tifPages.toString() + " از " + dirCount)

               var bmp: Bitmap = TiffBitmapFactory.decodeFile(img, options)
               binding.imgActivity.setImageBitmap(bmp)

               binding.fabNext.setOnClickListener {
                   if (tifPages < dirCount) {
                       tifPages++;
                       options.inDirectoryNumber = tifPages
                       bmp = TiffBitmapFactory.decodeFile(img, options)
                       binding.imgActivity.setImageBitmap(bmp)
                       binding.tifViewNumber.setText(tifPages.toString() + " از " + dirCount)
                   }
               }

               binding.fabPrev.setOnClickListener {
                   if (tifPages > 0) {
                       tifPages--;
                       options.inDirectoryNumber = tifPages;
                       bmp = TiffBitmapFactory.decodeFile(img, options);
                       binding.imgActivity.setImageBitmap(bmp)
                       binding.tifViewNumber.setText(tifPages.toString() + " از " + dirCount);
                   }
               }
           } else {
               // file is not tif so we just show it
               isFileTif = false
               var bmp: Bitmap = BitmapFactory.decodeFile(img.absolutePath)
               binding.imgActivity.setImageBitmap(bmp)
           }
       }
       catch (e:Exception) {
        Tools().snack(view, "این نوع فایل پشتیبانی نمی شود")
    }
    }

    // Tif is not displayed on all phones and requires external application So we Convert Tif to png
    // after Convert we can share it
    private fun convertTiffToPng(file: File) {

        // we must remove temp file that generated before
        Tools().clearShareTemp(context)

        val options = TiffConverter.ConverterOptions()
        options.throwExceptions = false //Set to true if you want use java exception mechanism;
        options.availableMemory = 128 * 1024 * 1024.toLong() //Available 128Mb for work;
        options.readTiffDirectory = 1 //Number of tiff directory to convert;
        val progressListener =
            IProgressListener { processedPixels, totalPixels ->
                //Log.v("Progress reporter", String.format("Processed %d pixels from %d", processedPixels, totalPixels);
                if (processedPixels == totalPixels) {
                    val share =
                        File(Tools()._Path(context) + Tools()._TempFileName)
                    Tools().shareFile(share,activity)
                }
            }
        TiffConverter.convertTiffPng(
            file.absolutePath,
            Tools()._Path(context) + Tools()._TempFileName,
            options,
            progressListener
        )
    }

}