package com.example.mybookapplication

import android.app.Activity
import android.arch.persistence.room.Database
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

class adapter(private var activity: Activity, private var items: ArrayList<FileData>, private var db: FileDataBase?): BaseAdapter() {
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

    override fun getItem(position: Int): FileData {
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
            view = inflater.inflate(R.layout.list_item,null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }


        viewHolder.but_fav?.setOnClickListener{
            if (!items[position].Fav) {
                viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_purple_24dp)
                items[position].Fav = true
                db?.fileDataDao()?.updateFile(items[position])
            } else {
                viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_grey_24dp)
                items[position].Fav = false
                db?.fileDataDao()?.updateFile(items[position])
            }
        }

        viewHolder.but_wish?.setOnClickListener{
            if (!items[position].Wishes) {
                viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_purple_24dp)
                items[position].Wishes = true
                db?.fileDataDao()?.updateFile(items[position])
            } else {
                viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_grey_24dp)
                items[position].Wishes = false
                db?.fileDataDao()?.updateFile(items[position])
            }
        }
        viewHolder.but_fin?.setOnClickListener{
            if (!items[position].HaveRead) {
                viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_purple_24dp)
                items[position].HaveRead = true
                db?.fileDataDao()?.updateFile(items[position])
            } else {
                viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_grey_24dp)
                items[position].HaveRead = false
                db?.fileDataDao()?.updateFile(items[position])
            }
        }

        val pdffile = items[position]
        //val pdffile = db?.fileDataDao()!!.findById(items[position].id)
        viewHolder.txtName?.text = pdffile.FileName
        if (pdffile.Fav) {
            viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_purple_24dp)
        } else {
            viewHolder.but_fav?.setImageResource(R.drawable.ic_favorite_border_grey_24dp)
        }
        if (pdffile.Wishes) {
            viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_purple_24dp)
        } else {
            viewHolder.but_wish?.setImageResource(R.drawable.ic_wishes_grey_24dp)
        }
        if (pdffile.HaveRead) {
            viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_purple_24dp)
        } else {
            viewHolder.but_fin?.setImageResource(R.drawable.ic_finished_grey_24dp)
        }

        return view as View
    }

}
