package com.example.mybookapplication

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

class adapter(private var activity: Activity, private var items: ArrayList<PdfFile>): BaseAdapter() {
    var but_fav_add:Int = 0
    var but_wish_add:Int = 0
    var but_fin_add:Int = 0
    private class ViewHolder(view: View?) {
        var txtName:TextView? = null
        var but_fav:ImageButton? = null
        var but_wish:ImageButton? = null
        var but_fin:ImageButton? = null

        init {
            this.txtName = view?.findViewById(R.id.txtFileName)
            this.but_fav = view?.findViewById(R.id.favourite)
            this.but_wish = view?.findViewById(R.id.wishes)
            this.but_fin = view?.findViewById(R.id.finished)
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
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.list_item, null)
            viewHolder = ViewHolder(view)
            viewHolder.but_fav?.setOnClickListener{
                if (but_fav_add == 0) {
                    viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_purple_24dp)
                    but_fav_add = 1
                } else {
                    viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_grey_24dp)
                    but_fav_add = 0
                }
            }

            viewHolder.but_wish?.setOnClickListener{
                if (but_wish_add == 0) {
                    viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_purple_24dp)
                    but_wish_add = 1
                } else {
                    viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_grey_24dp)
                    but_wish_add = 0
                }
            }
            viewHolder.but_fin?.setOnClickListener{
                if (but_fin_add == 0) {
                    viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_purple_24dp)
                    but_fin_add = 1
                } else {
                    viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_grey_24dp)
                    but_fin_add = 0
                }
            }
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val pdffile = items[position]
        viewHolder.txtName?.text = pdffile.pdfFileName

        return view as View
    }

}
