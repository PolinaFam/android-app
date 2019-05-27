package com.example.mybookapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.graphics.pdf.PdfRenderer
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Matrix
import android.widget.Toast
import java.lang.Exception
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import java.io.File
import java.io.IOException

class PdfActivity : AppCompatActivity() {
    private lateinit var pdfRenderer:PdfRenderer
    private lateinit var curPage:PdfRenderer.Page
    private lateinit var descriptor: ParcelFileDescriptor
    private lateinit var imgView: ImageView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnZoomin: ImageButton
    private lateinit var btnZoomout: ImageButton
    private var currentZoomLevel:Float = 5.0f
    private var currentPage: Int = 0
    private var path: String? = null
    private val CURRENT_PAGE = "current_page_index"
    val displayMetrics = DisplayMetrics()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        path = intent.extras?.getString("filename")
        setTitle(intent.extras?.getString("keyname"));
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(CURRENT_PAGE,0)
        }

        windowManager.defaultDisplay.getMetrics(displayMetrics)

        imgView = findViewById(R.id.imgView);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnZoomin = findViewById(R.id.zoomin);
        btnZoomout = findViewById(R.id.zoomout);

        btnPrevious.setOnClickListener(clickListener);
        btnNext.setOnClickListener(clickListener);
        btnZoomin.setOnClickListener(clickListener);
        btnZoomout.setOnClickListener(clickListener);
    }

    override fun onStart() {
        super.onStart()
        try {
            openPdfRenderer()
            displayPage(currentPage)
        } catch (e:Exception) {
            Toast.makeText(this,"PDF-file cannot be read",Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onStop(){
        try {
            closePdfRenderer()
        } catch (e: IOException) {
            Toast.makeText(this,"Close Error",Toast.LENGTH_SHORT)
                .show()
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE, curPage.index)
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun openPdfRenderer() {
        val file = File(path)
        descriptor = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(descriptor)
        curPage = pdfRenderer.openPage(currentPage)
    }

    private fun displayPage(index:Int) {
        if (pdfRenderer.pageCount <= index) return
        curPage.close()
        curPage = pdfRenderer.openPage(index)
        val newWidth = (displayMetrics.widthPixels * curPage.width / 72 * currentZoomLevel / 40).toInt()
        val newHeight = (displayMetrics.heightPixels * curPage.height / 72 * currentZoomLevel / 64).toInt()
        val bitmap = createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val matrix = Matrix()
        val dpiAdjustedZoomLevel: Float = currentZoomLevel * DisplayMetrics.DENSITY_MEDIUM / displayMetrics.densityDpi
        matrix.setScale(dpiAdjustedZoomLevel, dpiAdjustedZoomLevel);
        curPage.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imgView.setImageBitmap(bitmap)

        val pageCount = pdfRenderer.pageCount
        btnPrevious.isEnabled = (0 != index)
        btnNext.isEnabled = (index + 1 < pageCount)
        btnZoomout.isEnabled = (currentZoomLevel != 2.0f);
        btnZoomin.isEnabled = (currentZoomLevel != 12.0f);

    }

    @Throws(IOException::class)
    private fun closePdfRenderer() {
        curPage.close()
        pdfRenderer.close()
        descriptor.close()
    }

    private val clickListener = View.OnClickListener {
        v ->
        when(v.id) {
            R.id.btnPrevious -> {
                displayPage(curPage.index - 1)
            }
            R.id.btnNext -> {
                displayPage(curPage.index + 1)
            }
            R.id.zoomin -> {
                currentZoomLevel++
                displayPage(curPage.index)
            }
            R.id.zoomout -> {
                currentZoomLevel--
                displayPage(curPage.index)
            }
        }

    }
}
