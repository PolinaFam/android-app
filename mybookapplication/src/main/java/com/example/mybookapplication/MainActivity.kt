package com.example.mybookapplication

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import com.androidbuffer.kotlinfilepicker.KotConstants
import com.androidbuffer.kotlinfilepicker.KotRequest
import com.androidbuffer.kotlinfilepicker.KotResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var list = ArrayList<PdfFile>()
    var listView: ListView? = null
    private lateinit var nameOfFile: String
    private lateinit var locationOfFile: String
    private val REQUEST_FILE = 103
    private val REQUEST_PERMISSION = 1
    private lateinit var adapter:adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        listView = findViewById(R.id.listView)
        adapter = adapter(this,list)
        listView?.adapter = adapter


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_FILE == requestCode && resultCode == Activity.RESULT_OK) {

            val result = data?.getParcelableArrayListExtra<KotResult>(KotConstants.EXTRA_FILE_RESULTS)
            result!!.forEach { i ->
                nameOfFile = i.name.toString()
                locationOfFile = i.location.toString()
                list.add(PdfFile(nameOfFile, locationOfFile))
                adapter.notifyDataSetChanged()
            }
            Toast.makeText(this, "Books are added", Toast.LENGTH_LONG)
                .show()
        }
    }

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
        adapter.notifyDataSetChanged()

        listView?.setOnItemClickListener {_,_, position,_ ->
            val selectedFile = list[position]
            val readIntent = Intent(this, PdfActivity::class.java)
            readIntent.putExtra("keyname",selectedFile.pdfFileName)
            readIntent.putExtra("filename",selectedFile.pdfFilePath)
            startActivity(readIntent)
        }
    }

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
                    list.add(PdfFile(fileName, f.absolutePath))
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_library -> {
                // Handle the camera action
            }
            R.id.nav_favourite -> {

            }
            R.id.nav_wishes -> {

            }
            R.id.nav_finished -> {

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
