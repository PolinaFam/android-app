package com.example.mybookapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.github.mertakdut.Reader
import java.io.File
import kotlin.Float as Float1


class FileGridAdapter(val context: Context):BaseAdapter() {

    private var filesList = emptyList<FileData>()

    class ViewHolder {
        var coverImage: ImageView? = null
        var title: TextView? = null
    }

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        val viewHolder: ViewHolder

        if (myView === null) {
            val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myView = inflater.inflate(R.layout.grid_item, parent, false)

            viewHolder = ViewHolder()
            viewHolder.title = myView!!.findViewById<TextView>(R.id.txt_book_title)
            viewHolder.coverImage = myView!!.findViewById<ImageView>(R.id.img_cover)

            myView.setTag(viewHolder)
        } else {
            viewHolder = myView.getTag() as ViewHolder
        }
        //set Image Cover
        if (filesList[position].Format == "application/epub+zip") {
            val reader = Reader()
            reader.setInfoContent(filesList[position].FilePath)

            val coverImageAsBytes = reader.coverImage
            if (coverImageAsBytes != null) {
                val bitmap = decodeBitmapFromByteArray(coverImageAsBytes, 100, 200)
                viewHolder.coverImage!!.setImageBitmap(bitmap)
            } else {
                viewHolder.coverImage!!.setImageResource(R.drawable.ic_book)
            }
        } else {
            val file = File(filesList[position].FilePath)
            val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(descriptor)
            val curPage = pdfRenderer.openPage(0)

            val pageWidth = curPage.width
            val pageHeight = curPage.height

            val bitmap = Bitmap.createBitmap(
                pageWidth,
                pageHeight,
                Bitmap.Config.ARGB_8888
            )
            curPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            viewHolder.coverImage!!.setImageBitmap(bitmap)
            curPage.close()
            pdfRenderer.close()
            descriptor.close()
        }

        viewHolder.title!!.setText(filesList[position].FileName)
        return myView
    }

    private fun decodeBitmapFromByteArray(coverImage: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(coverImage, 0, coverImage.size, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(coverImage, 0, coverImage.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int) :Int {

        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize > reqHeight
                && halfWidth / inSampleSize > reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    //Auto Generated Method
    override fun getItem(p0: Int): Any {
        return filesList.get(p0)
    }

    //Auto Generated Method
    override fun getItemId(p0: Int): Long {
        return 0
    }

    //Auto Generated Method
    override fun getCount(): Int {
        return filesList.size
    }

    internal fun setFiles(files: List<FileData>) {
        this.filesList = files
        notifyDataSetChanged()
    }

}