package com.example.mybookapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class adapter(private var activity: Activity, private var items: ArrayList<PdfFile>): BaseAdapter() {

    private class ViewHolder(view: View?) {
        var txtName:TextView? = null
        init {
            this.txtName = view?.findViewById<TextView>(R.id.txtFileName)
        }
    }

    override fun getCount():Int {
        return items.size
    }

    override fun getItem(position: Int): PdfFile {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View?
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.list_item, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var pdffile = items[position]
        viewHolder.txtName?.text = pdffile.pdfFileName

        return view as View
    }
}
