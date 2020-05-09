package com.example.mybookapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.github.mertakdut.Reader
import java.io.*


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

            val path = Environment.getExternalStorageDirectory().toString() + "/BookCovers/" + filesList[position].FileName + ".png"
            if (!checkFileExistence(File(path))) {
                val reader = Reader()
                reader.setInfoContent(filesList[position].FilePath)
                val coverImageAsBytes = reader.coverImage
                if (coverImageAsBytes != null) {
                    val bitmap = decodeBitmapFromByteArray(coverImageAsBytes)
                    saveImage(bitmap, filesList[position].FileName)
                        viewHolder.coverImage!!.setImageBitmap(bitmap)
                } else {
                    viewHolder.coverImage!!.setImageResource(R.drawable.ic_book)
                }
            } else {
                val uri: Uri = Uri.parse(File(path).absolutePath)
                viewHolder.coverImage!!.setImageURI(uri)
            }

        } else {
            val fileName = filesList[position].FileName.replace(".pdf", "")
            val path = Environment.getExternalStorageDirectory().toString() + "/BookCovers/" + "$fileName.png"
            if (!checkFileExistence(File(path))) {
                try{
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

                    saveImage(bitmap, fileName)


                    viewHolder.coverImage!!.setImageBitmap(bitmap)
                    curPage.close()
                    pdfRenderer.close()
                    descriptor.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                val uri: Uri = Uri.parse(File(path).absolutePath)
                viewHolder.coverImage!!.setImageURI(uri)
            }
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

    fun checkFileExistence(file: File):Boolean {
        return file.exists()
    }

    fun saveImage(bitmap: Bitmap, fileName: String) {
        val path = Environment.getExternalStorageDirectory().toString() + "/BookCovers"

        if(!checkFileExistence(File(path))) {
            File(path).mkdir()
        }
        val file = File(path, "$fileName.png")

        if (!file.exists()) {
            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
                println("Image saved successful")
            } catch (e: IOException){
                e.printStackTrace()
                println("Error to save image")
            }
        }
    }

}