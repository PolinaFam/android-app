package com.example.mybookapplication

import android.Manifest
import android.app.Activity
import android.content.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.database.CursorIndexOutOfBoundsException
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbuffer.kotlinfilepicker.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*
import android.provider.OpenableColumns
import org.jetbrains.anko.toast
import java.io.FileNotFoundException
import android.content.*
import android.database.Cursor
import android.provider.MediaStore.Files.getContentUri
import android.provider.Settings
import androidx.core.content.FileProvider
import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.text.SimpleDateFormat
import java.util.regex.Pattern


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FileListAdapter.OnBtnClickListener {

    private lateinit var viewModel: ListViewModel
    private val REQUEST_PERMISSION = 2
    private val REQUEST_FILE = 103
    private val REQUEST_PAGE = 1
    private lateinit var adapter:FileListAdapter

    override fun onFavButtonClick(file:FileData) {
        file.Fav = !file.Fav
        viewModel.update(file)
    }
    override fun onWishButtonClick(file:FileData) {
        file.Wishes = !file.Wishes
        viewModel.update(file)
    }
    override fun onFinishedButtonClick(file:FileData) {
        file.HaveRead = !file.HaveRead
        viewModel.update(file)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        adapter = FileListAdapter(this, this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val delFile = adapter.getItem(viewHolder.adapterPosition)
                viewModel.delete(delFile)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        viewModel.Files.observe(this,Observer {
            files -> files?.let { adapter.setFiles(it)}
        })
        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (handlePermissionCheck()) {
                    handleIntent()
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION)
                }
            } else {
                handleIntent()
            }
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun handlePermissionCheck(): Boolean {
        val permissionR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionR == PackageManager.PERMISSION_GRANTED && permissionW == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun handleIntent() {
        intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*")
        val mimetypes = arrayOf("application/pdf", "application/epub+zip")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_FILE)
    }
    @SuppressWarnings("NewApi")
    private fun readPathFromUri(context: Context, uri: Uri): String? {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                val contentUri = getContentUri(type)

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) {
                uri.lastPathSegment
            } else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor!!.moveToFirst()) {
                val column_index = cursor!!.getColumnIndexOrThrow(column)
                return cursor!!.getString(column_index)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }
        return null
    }

    fun getFileDetails(context: Context, uri: Uri): File? {
        //get the details from uri
        var fileToReturn: File? = null
        try {
            fileToReturn = File(readPathFromUri(context, uri))
        } catch (exp: CursorIndexOutOfBoundsException) {
            exp.printStackTrace()
            fileToReturn = File(uri.path)
        } catch (exp: NullPointerException) {
            exp.printStackTrace()
            fileToReturn = File(uri.path)
        } catch (exp: NumberFormatException) {
            exp.printStackTrace()
            fileToReturn = File(uri.path)
        }
        return fileToReturn
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_PERMISSION == requestCode) {
            for (permission in grantResults) {
                if (permission == PackageManager.PERMISSION_DENIED) {
                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setMessage("Доступ к памяти устройства необходим для нормальной работы данного приложения. Перейдите в настройки и дайте разрешение.")
                        .setPositiveButton("НАСТРОЙКИ") { dialogInterface, i ->
                        startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.mybookapplication")))
                        }
                        .setNegativeButton("не сейчас") { dialogInterface, i ->
                            dialogInterface.dismiss()
                            finish()
                        }
                    alertBuilder.setCancelable(false)
                    alertBuilder.create().show()
                    return
                }
            }
            handleIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK) {
            val file = getFileDetails(this, data!!.data!!)
            val fileSize = file!!.length()
            val fileName = file.name
            val fileLocation = file.path
            val fileMimeType = contentResolver.getType(data.data!!)!!
            var filePages = 0
            Toast.makeText(this, fileMimeType,Toast.LENGTH_LONG).show()
            if (fileMimeType == "application/pdf") {
                try {
                    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(descriptor)
                    filePages = pdfRenderer.pageCount
                    pdfRenderer.close()
                    descriptor.close()
                }
                catch (e: FileNotFoundException) {
                    Toast.makeText(this, "Невозможно добавить файл",Toast.LENGTH_LONG).show()
                }
            }
            val fileData = FileData(
                FileName=fileName,
                FilePath = fileLocation,
                CurPage = 0,
                Pages = filePages,
                Size = fileSize,
                Format = fileMimeType,
                DateOfAdding = Calendar.getInstance().time,
                Fav = false,
                HaveRead = false,
                Wishes = false)
            println("FILE " + fileData)
            viewModel.insert(fileData)
        }
        if (REQUEST_PAGE == requestCode && resultCode == Activity.RESULT_OK) {
            val resultIdStr = data?.extras?.getString("fileId")
            val resultId = resultIdStr!!.toLong()
            val resultPageStr = data.extras?.getString("currentPage")
            val resultPage = resultPageStr!!.toInt()
            viewModel.findForUpdate(resultId,resultPage)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSortName -> {
                item.setChecked(true)
                viewModel.sortList("Name")
                return true
            }
            R.id.menuSortSize -> {
                item.setChecked(true)
                viewModel.sortList("Size")
                return true
            }
            R.id.menuSortDate -> {
                item.setChecked(true)
                viewModel.sortList("Date")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_library -> {
                viewModel.changeList("All")
            }
            R.id.nav_favourite -> {
                viewModel.changeList("Fav")
            }
            R.id.nav_wishes -> {
                viewModel.changeList("Wish")
            }
            R.id.nav_finished -> {
                viewModel.changeList("Fin")
            }
            R.id.nav_exit -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
