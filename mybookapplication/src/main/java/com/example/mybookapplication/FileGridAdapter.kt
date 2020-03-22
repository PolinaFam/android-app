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
                val bitmap = decodeBitmapFromByteArray(coverImageAsBytes)
                viewHolder.coverImage!!.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 330, 465, false))
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

    private fun decodeBitmapFromByteArray(coverImage: ByteArray): Bitmap {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(coverImage, 0, coverImage.size, options)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(coverImage, 0, coverImage.size, options)
    }

    //Auto Generated Method
    override fun getItem(p0: Int): FileData {
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