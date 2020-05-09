package com.example.mybookapplication

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.mertakdut.BookSection
import com.github.mertakdut.CssStatus
import com.github.mertakdut.Reader
import com.github.mertakdut.exception.OutOfPagesException
import com.github.mertakdut.exception.ReadingException
import kotlinx.android.synthetic.main.app_bar_main.*


class EpubActivity : AppCompatActivity(), PageFragment.OnFragmentReadyListener {

    private var reader: Reader = Reader()

    private var mViewPager: ViewPager? = null
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var pageCount = Int.MAX_VALUE

    private val searchMenuItem: MenuItem? = null
    private val searchView: SearchView? = null

    private var isSkippedToPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epub)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager = findViewById(R.id.container)
        mViewPager!!.offscreenPageLimit = 0
        mViewPager!!.adapter = mSectionsPagerAdapter

        val filePath = intent.extras?.getString("filepath")
        title = intent.extras?.getString("filename")
        try{
            reader.setMaxContentPerSection(1250)
            reader.setCssStatus(CssStatus.INCLUDE)
            reader.setIsIncludingTextContent(true)
            reader.setIsOmittingTitleTag(true)

            reader.setFullContent(filePath);
            if (reader.isSavedProgressFound) {
                val lastSavedPage = reader.loadProgress()
                mViewPager!!.currentItem = lastSavedPage
            }
        } catch(e: ReadingException){
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onFragmentReady(position: Int): View? {
        var bookSection: BookSection? = null
        try {
            bookSection = reader.readSection(position)
        } catch(e: ReadingException){
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        } catch (e: OutOfPagesException){
            e.printStackTrace()
            this.pageCount = e.pageCount

            if (isSkippedToPage){
                Toast.makeText(this, "Максимальное количество страниц: " + this.pageCount, Toast.LENGTH_LONG).show()
            }
            mSectionsPagerAdapter?.notifyDataSetChanged()
        }
        isSkippedToPage = false

        if (bookSection != null) {
            return setFragmentView(bookSection.sectionContent, "text/html", "UTF-8")
        }
        return null
    }

    private fun setFragmentView(data: String, mimeType: String, encoding: String): View {
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val webview = WebView(this)
        webview.loadDataWithBaseURL(null, data, mimeType, encoding, null)

        webview.layoutParams = layoutParams
        return webview
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu!!.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && !query.equals("")) {
                    if (TextUtils.isDigitsOnly(query)) {
                        loseFocusOnSearchView()
                        val skippingPage = Integer.valueOf(query)
                        if (skippingPage >= 0) {
                            isSkippedToPage = true
                            mViewPager?.currentItem = skippingPage
                        }
                    } else {
                        loseFocusOnSearchView()
                    }
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }

    override fun onBackPressed() {
        if (!searchView!!.isIconified) {
            loseFocusOnSearchView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            reader.saveProgress(mViewPager!!.currentItem)
        } catch (e: ReadingException) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка. Прогресс не сохранен.", Toast.LENGTH_LONG).show()
        } catch (e: OutOfPagesException) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка. Прогресс не сохранен.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_search) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

private fun loseFocusOnSearchView() {
    searchView?.setQuery("", false)
    searchView?.clearFocus()
    searchView?.isIconified = true
    searchMenuItem?.collapseActionView()
}


inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return PageFragment.newInstance(position);
        }

        override fun getCount(): Int {
            return pageCount
        }

    }
}