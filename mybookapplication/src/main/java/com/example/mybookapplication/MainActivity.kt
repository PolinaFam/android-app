package com.example.mybookapplication

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.androidbuffer.kotlinfilepicker.KotConstants
import com.androidbuffer.kotlinfilepicker.KotRequest
import com.androidbuffer.kotlinfilepicker.KotResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

import java.io.File



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    var list = ArrayList<FileData>()
   var listView: ListView? = null
    private lateinit var nameOfFile: String
    private lateinit var locationOfFile: String
    private val REQUEST_FILE = 103
    private val REQUEST_PERMISSION = 1
    private val DELETE_ID = 111
    private lateinit var adapter:adapter
    private var db: FileDataBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        db = FileDataBase.getDB(applicationContext)
        listView = findViewById(R.id.listView)
        adapter = adapter(this,list, db)
        listView?.adapter = adapter
        registerForContextMenu(listView)
        list.clear()
        list.addAll(db?.fileDataDao()!!.getAll())
        adapter.notifyDataSetChanged()

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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          checkPermission()
        }
        else {
            initViews()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(0,DELETE_ID,0,"Delete")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.itemId == DELETE_ID)
        {
            val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
            val position = info.position
            db?.fileDataDao()?.deleteFile(list[position])
            list.clear()
            list.addAll(db?.fileDataDao()!!.getAll())
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Deleted.", Toast.LENGTH_LONG)
                .show()
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK) {

            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            result!!.forEach { i ->
                nameOfFile = i.name.toString()
                locationOfFile = i.location.toString()
                val fileData = FileData(FileName=i.name.toString(),
                    FilePath = i.location.toString(),
                    CurPage = 0,
                    Fav = false,
                    HaveRead = false,
                    Wishes = false)
                db!!.fileDataDao().insertFile(fileData)
                list.clear()
                list.addAll(db?.fileDataDao()!!.getAll())
                adapter.notifyDataSetChanged()
            }
                Toast.makeText(this, "Added", Toast.LENGTH_LONG)
                    .show()
        }
    }

    //проверка доступа
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
                        val dialog = android.support.v7.app.AlertDialog.Builder(this)
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
    private fun initList(path: String) {
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
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.menuSortName -> {
                item.setChecked(true)
                list.clear()
                list.addAll(db?.fileDataDao()!!.sortName())
                adapter.notifyDataSetChanged() //это нужно будет поменять!!!
                return true
            }
            R.id.menuSortSize -> {
                item.setChecked(true)
                return true
            }
            R.id.menuSortDate -> {
                item.setChecked(true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_library -> {
                list.clear()
                list.addAll(db?.fileDataDao()!!.getAll())
                adapter.notifyDataSetChanged()
            }
            R.id.nav_favourite -> {
                if (db?.fileDataDao()?.findFav() != null)
                {
                    list.clear()
                    list.addAll(db?.fileDataDao()!!.findFav())
                }
                adapter.notifyDataSetChanged()
            }
            R.id.nav_wishes -> {

                if (db?.fileDataDao()?.findWishes() != null)
                {
                    list.clear()
                    list.addAll(db?.fileDataDao()!!.findWishes())
                }
                adapter.notifyDataSetChanged()
            }
            R.id.nav_finished -> {

                if (db?.fileDataDao()?.findHaveRead() != null)
                {
                    list.clear()
                    list.addAll(db?.fileDataDao()!!.findHaveRead())
                }
                adapter.notifyDataSetChanged()
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
