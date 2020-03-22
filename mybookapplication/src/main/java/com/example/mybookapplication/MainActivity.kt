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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File
import java.util.*
import java.io.FileNotFoundException
import android.database.Cursor
import android.provider.MediaStore.Files.getContentUri
import android.view.View
import android.webkit.MimeTypeMap
import android.view.ViewStub;
import android.widget.AdapterView
import android.widget.GridView
import com.github.mertakdut.Reader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FileListAdapter.OnBtnClickListener {

    private lateinit var viewModel: ListViewModel
    private val REQUEST_PERMISSION = 2
    private val REQUEST_FILE = 103
    private val REQUEST_PAGE = 1
    private val VIEW_MODE_LISTVIEW = 0
    private val VIEW_MODE_GRIDVIEW = 1

    private lateinit var stubList: ViewStub
    private lateinit var stubGrid: ViewStub
    private lateinit var recyclerView: RecyclerView
    private lateinit var gridView: GridView

    private lateinit var listAdapter:FileListAdapter
    private lateinit var gridAdapter:FileGridAdapter
    private lateinit var sharedPrefernces: SharedPreferences
    private var currentViewMode = 0;

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

        stubList = findViewById(R.id.stub_list)
        stubGrid = findViewById(R.id.stub_grid)
        stubList.inflate()
        stubGrid.inflate()

        gridView = findViewById(R.id.grid_view)
        recyclerView = findViewById(R.id.recycler_view)


        sharedPrefernces = getSharedPreferences("ViewMode", Context.MODE_PRIVATE)
        currentViewMode = sharedPrefernces.getInt("currentViewMode", VIEW_MODE_LISTVIEW)

        switchView();

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

    private fun switchView() {
        if(VIEW_MODE_LISTVIEW == currentViewMode) {
            //Display listview
            stubList.setVisibility(View.VISIBLE);
            //Hide gridview
            stubGrid.setVisibility(View.GONE);
        } else {
            //Hide listview
            stubList.setVisibility(View.GONE);
            //Display gridview
            stubGrid.setVisibility(View.VISIBLE);
        }
        setAdapters();
    }

    private fun setAdapters() {
        if (VIEW_MODE_LISTVIEW == currentViewMode) {
            listAdapter = FileListAdapter(this, this)
            recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recyclerView.adapter = listAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            val swipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val delFile = listAdapter.getItem(viewHolder.adapterPosition)
                    viewModel.delete(delFile)
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
            viewModel.Files.observe(this,Observer {
                    files -> files?.let { listAdapter.setFiles(it)}
            })
        } else {
            gridAdapter = FileGridAdapter(this)
            gridView.adapter = gridAdapter

            gridView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val clickedItem = gridAdapter.getItem(position)
                    if (clickedItem.Format == "application/epub+zip") {
                        val intent = Intent(this@MainActivity, EpubActivity::class.java).apply {
                            putExtra("filename", clickedItem.FileName)
                            putExtra("filepath", clickedItem.FilePath)
                        }
                        this@MainActivity.startActivity(intent)
                    } else if (clickedItem.Format == "application/pdf"){
                        val intent = Intent(this@MainActivity,PdfActivity::class.java).apply {
                            putExtra("fileId",clickedItem.id.toString())
                            putExtra("keyname",clickedItem.FileName)
                            putExtra("filename",clickedItem.FilePath)
                            putExtra("currentPage",clickedItem.CurPage.toString())
                        }
                        this@MainActivity.startActivityForResult(intent,REQUEST_PAGE)
                    }
                }


            viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
            viewModel.Files.observe(this,Observer {
                    files -> files?.let { gridAdapter.setFiles(it)}
            })
        }
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
        } catch (e: Throwable) {
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

    private fun addFile(file: File, fileType: String){
        try {
            val fileSize = file.length()
            val fileMimeType = fileType
            var fileName = file.name
            println("FILENAME")
            println(fileName)
            println("FILEPATH")
            println(file.path)
            if (fileMimeType == "application/epub+zip") {
                val reader = Reader()
                reader.setInfoContent(file.absolutePath)
                val title = reader.infoPackage.metadata.title
                if (title != null && title != "") {
                    fileName = title
                }
            }
            val fileLocation = file.absolutePath
            var filePages = 0
            if (fileMimeType == "application/pdf") {
                val descriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(descriptor)
                filePages = pdfRenderer.pageCount
                pdfRenderer.close()
                descriptor.close()
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
            viewModel.insert(fileData)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK) {
            val file = getFileDetails(this, data!!.data!!)
            val fileMimeType = contentResolver.getType(data.data!!)!!
            addFile(file!!, fileMimeType)
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
            R.id.switch_view -> {
                if(VIEW_MODE_LISTVIEW == currentViewMode) {
                    currentViewMode = VIEW_MODE_GRIDVIEW;
                } else {
                    currentViewMode = VIEW_MODE_LISTVIEW;
                }
                //Switch view
                switchView();
                //Save view mode in share reference
                sharedPrefernces = getSharedPreferences("ViewMode", MODE_PRIVATE);
                val editor = sharedPrefernces.edit();
                editor.putInt("currentViewMode", currentViewMode);
                editor.apply();
                return true
            }
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

    private suspend fun initList(path: String) {
        val file = File(path)
        val fileList: Array<File> = file.listFiles()

        for (f in fileList) {
            if (f.isDirectory) {
                initList(f.absolutePath)
            } else {
                val uri = Uri.fromFile(f);
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                val fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                if (fileMimeType == "application/pdf" || fileMimeType == "application/epub+zip"){
                    addFile(f, fileMimeType)
                }
            }
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
            R.id.nav_scan -> {
                val path:String = Environment.getExternalStorageDirectory().absolutePath
                GlobalScope.launch {
                    initList(path)
                }
            }
            R.id.nav_exit -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
