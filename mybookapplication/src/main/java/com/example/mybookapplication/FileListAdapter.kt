package com.example.mybookapplication

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item.view.*

class FileListAdapter(val context: Context, val listener: OnBtnClickListener):
    RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    interface OnBtnClickListener {
        fun onFavButtonClick(file:FileData)
        fun onWishButtonClick(file:FileData)
        fun onFinishedButtonClick(file: FileData)
    }
    private val REQUEST_PAGE = 1
    private var filesList = emptyList<FileData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.list_item, parent, false)
        return FileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val current = filesList[position]
        var progress = 0
        holder.itemView.txtFileName.text = current.FileName
        val size = current.Size
        val sizeStr:String
        if (size < 1024) {
            sizeStr = "%.2f".format(current.Size) + " б"
        } else if (size >=1024 && size <1024*1024) {
            sizeStr ="%.2f".format(current.Size/1024.0) + " Кб"
        } else {
            sizeStr = "%.2f".format(current.Size/(1024.0*1024.0)) + " Мб"
        }
        holder.itemView.txtFileSize.text = sizeStr
        if (current.Pages == 1) {
            progress = 0
        } else {progress =((100.0/(current.Pages-1))*(current.CurPage)).toInt()}
        holder.itemView.progressBar.progress = progress
        holder.bindView(current,listener)
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        init {
            itemView.setOnClickListener(this)
        }
        fun bindView(file: FileData, listener:OnBtnClickListener) {
            itemView.favourite.setOnClickListener {listener.onFavButtonClick(file)}
            if(file.Fav) {
                itemView.favourite.setImageResource(R.drawable.ic_favorite_border_purple_24dp)
            } else {
                itemView.favourite.setImageResource(R.drawable.ic_favorite_border_grey_24dp)
            }
            itemView.wishes.setOnClickListener {listener.onWishButtonClick(file)}
            if(file.Wishes) {
                itemView.wishes.setImageResource(R.drawable.ic_wishes_purple_24dp)
            } else {
                itemView.wishes.setImageResource(R.drawable.ic_wishes_grey_24dp)
            }
            itemView.finished.setOnClickListener {listener.onFinishedButtonClick(file)}
            if(file.HaveRead) {
                itemView.finished.setImageResource(R.drawable.ic_finished_purple_24dp)
            } else {
                itemView.finished.setImageResource(R.drawable.ic_finished_grey_24dp)
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

    fun getItem(position:Int) :FileData {
        return filesList[position]
    }

    override fun getItemCount() = filesList.size
}