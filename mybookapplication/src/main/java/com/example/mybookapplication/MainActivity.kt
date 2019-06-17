package com.example.mybookapplication

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidbuffer.kotlinfilepicker.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FileListAdapter.OnBtnClickListener {

    //private val REQUEST_PERMISSION = 1
    private lateinit var viewModel: ListViewModel
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
        viewModel.allFiles.observe(this,Observer {
            files -> files?.let { adapter.setFiles(it)}
        })

        fab.setOnClickListener {
            KotRequest.File(this, REQUEST_FILE)
                .isMultiple(true)
                .setMimeType(KotConstants.FILE_TYPE_PDF)
                .pick()
        }
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
/*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          checkPermission()
        }
        else {
            initViews()
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK) {
           val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)

            result!!.forEach { i ->
                val file = File(i.location.toString())
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(descriptor)
                val fileData = FileData(FileName=i.name.toString(),
                    FilePath = i.location.toString(),
                    CurPage = 0,
                    Pages = pdfRenderer.pageCount,
                    Size = i.size!!.toString(),
                    DateOfAdding = Calendar.getInstance().time,
                    Fav = false,
                    HaveRead = false,
                    Wishes = false)
                viewModel.insert(fileData)
                pdfRenderer.close()
                descriptor.close()
                Toast.makeText(this, "Added.", Toast.LENGTH_LONG)
                .show()
            }
        }
        if (REQUEST_PAGE == requestCode && resultCode == Activity.RESULT_OK) {
            val resultIdStr = data?.extras?.getString("fileId")
            val resultId = resultIdStr!!.toLong()
            val resultPageStr = data.extras?.getString("currentPage")
            val resultPage = resultPageStr!!.toInt()
            viewModel.findForUpdate(resultId,resultPage)
        }
    }

  /*  //проверка доступа
    private fun checkPermission() {
        val permissionR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionR == PackageManager.PERMISSION_GRANTED && permissionW == PackageManager.PERMISSION_GRANTED) {
            initViews()
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        showDialogOK("Service Permissions are required for this app",
                            DialogInterface.OnClickListener { _, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkPermission()
                                    DialogInterface.BUTTON_NEGATIVE -> finish()
                                }
                            })
                    } else {
                        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                        dialog.setMessage("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            .setPositiveButton("Yes") { _, _ ->
                                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.mybookapplication")))
                            }
                            .setNegativeButton("Cancel") { _, _ -> finish() }
                        dialog.show()
                    }

                }
            }
        }
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    private fun initViews(){
        //val path:String = Environment.getExternalStorageDirectory().absolutePath
        //initList(path)

        listView?.setOnItemClickListener {_,_, position,_ ->
            val selectedFile = list[position]
            val readIntent = Intent(this, PdfActivity::class.java)
            readIntent.putExtra("keyname",selectedFile.FileName)
            readIntent.putExtra("filename",selectedFile.FilePath)
            startActivity(readIntent)
        }
    }

    //возможно оставить функцию для сканирования всех pdf файлов
    /*private fun initList(path: String) {
        val file = File(path)
        val fileList: Array<File> = file.listFiles()
        var fileName: String
        for (f in fileList) {
            if (f.isDirectory) {
                initList(f.absolutePath)
            } else {
                fileName = f.name
                if (fileName.endsWith(".pdf")) {
                    list.add(FileData(FileName=fileName,
                        FilePath = f.absolutePath,
                        CurPage = 0,
                        Fav = false,
                        HaveRead = false,
                        Wishes = false))
                }
            }
        }
    }*/
*/
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
            R.id.action_settings -> return true
            R.id.menuSortName -> {
                /*item.setChecked(true)
                list.clear()
                list.addAll(db?.fileDataDao()!!.sortName())
                adapter.notifyDataSetChanged() //это нужно будет поменять!!!*/
                return true
            }
            R.id.menuSortSize -> {
                //item.setChecked(true)
                return true
            }
            R.id.menuSortDate -> {
                /*item.setChecked(true)
                list.clear()
                list.addAll(db?.fileDataDao()!!.sortDate())
                adapter.notifyDataSetChanged()*/
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_library -> {
            }
            R.id.nav_favourite -> {
                adapter.setFilterFiles("Fav")
            }
            R.id.nav_wishes -> {
                adapter.setFilterFiles("Wish")
            }
            R.id.nav_finished -> {
                adapter.setFilterFiles("Fin")
            }
            R.id.nav_tool -> {

            }
            R.id.nav_exit -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
