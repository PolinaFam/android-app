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
        viewModel.Files.observe(this,Observer {
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
                    Size = file.length(),
                    DateOfAdding = Calendar.getInstance().time,
                    Fav = false,
                    HaveRead = false,
                    Wishes = false)
                viewModel.insert(fileData)
                pdfRenderer.close()
                descriptor.close()
                Toast.makeText(this, "Добавлено.", Toast.LENGTH_LONG)
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
