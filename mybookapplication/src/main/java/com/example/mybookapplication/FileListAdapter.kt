package com.example.mybookapplication

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView

class FileListAdapter(val context: Context, val listener: OnBtnClickListener):
    RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    interface OnBtnClickListener {
        fun onFavButtonClick(file:FileData)
        fun onWishButtonClick(file:FileData)
        fun onFinishedButtonClick(file: FileData)
    }
    private val REQUEST_PAGE = 1
    private var filesList = emptyList<FileData>()
    private var i = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.list_item, parent, false)
        return FileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val current = filesList[position]
        var progress = 0
        holder.txtName.text = current.FileName
        if (current.Pages == 1) {
            if (i == 0) {
                progress = 0
                i++
            }
            else {
                progress = 100
            }
        } else {progress =((100.0/(current.Pages-1))*(current.CurPage)).toInt()}
        holder.progressBar.progress = progress
        println("pages = "+current.Pages)
        println("curpage = "+current.CurPage)
        println("progress = "+progress)
        holder.bindView(current,listener)
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val txtName: TextView = itemView.findViewById(R.id.txtFileName)
        private val fav: ImageButton = itemView.findViewById(R.id.favourite)
        private val wish:ImageButton = itemView.findViewById(R.id.wishes)
        private val fin:ImageButton = itemView.findViewById(R.id.finished)
        val progressBar:ProgressBar = itemView.findViewById(R.id.progressBar)
        init {
            itemView.setOnClickListener(this)
        }
        fun bindView(file: FileData, listener:OnBtnClickListener) {
            fav.setOnClickListener {listener.onFavButtonClick(file)}
            if(file.Fav) {
                fav.setImageResource(R.drawable.ic_favorite_border_purple_24dp)
            } else {
                fav.setImageResource(R.drawable.ic_favorite_border_grey_24dp)
            }
            wish.setOnClickListener {listener.onWishButtonClick(file)}
            if(file.Wishes) {
                wish.setImageResource(R.drawable.ic_wishes_purple_24dp)
            } else {
                wish.setImageResource(R.drawable.ic_wishes_grey_24dp)
            }
            fin.setOnClickListener {listener.onFinishedButtonClick(file)}
            if(file.HaveRead) {
                fin.setImageResource(R.drawable.ic_finished_purple_24dp)
            } else {
                fin.setImageResource(R.drawable.ic_finished_grey_24dp)
            }
        }
        override fun onClick(v: View?) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedFile = filesList[position]
                    val intent = Intent(context,PdfActivity::class.java).apply {
                        putExtra("fileId",selectedFile.id.toString())
                        putExtra("keyname",selectedFile.FileName)
                        putExtra("filename",selectedFile.FilePath)
                        putExtra("currentPage",selectedFile.CurPage.toString())
                    }
                    (context as MainActivity).startActivityForResult(intent,REQUEST_PAGE)
                }
        }
    }

    internal fun setFiles(files: List<FileData>) {
            this.filesList = files
            notifyDataSetChanged()
        }
    fun setFilterFiles (query:String) {
        val filteredList = filesList.filter {
            if(query == "Fav") {
                it.Fav
            } else if(query=="Wish") {
                it.Wishes
            } else it.HaveRead
        }
        setFiles(filteredList)
    }
    fun getItem(position:Int) :FileData {
        return filesList[position]
    }

    override fun getItemCount() = filesList.size

    /*override fun getFilter(): Filter {
        return object:Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    setFiles(filesList)
                } else {
                    lateinit var filteredFiles: MutableList<FileData>
                    for (row in filteredFiles) {
                        if (charSequence == "Fav" && row.Fav) filteredFiles.add(row)
                    }
                    setFiles(filteredFiles)
                }
                val filterResults = Filter.FilterResults()
                filterResults.values = filesList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                filesList = filterResults.values as List<FileData>
                notifyDataSetChanged()
            }

        }
    }*/
}