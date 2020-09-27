package com.linkdev.filepicker_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkdev.filepicker.models.FileData
import kotlinx.android.synthetic.main.item_attached_file.view.*

class AttachedFilesAdapter(private val context: Context) :
    RecyclerView.Adapter<AttachedFilesAdapter.AttachedFilesViewHolder>() {
    private val attachedFilesList = arrayListOf<FileData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachedFilesViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_attached_file, parent, false)
        return AttachedFilesViewHolder(view)
    }

    override fun getItemCount(): Int = attachedFilesList.size

    override fun onBindViewHolder(holder: AttachedFilesViewHolder, position: Int) {
        holder.onBind(attachedFilesList[position])
    }

    fun addFiles(attachedFiles: ArrayList<FileData>) {
        attachedFilesList.addAll(attachedFiles)
        notifyDataSetChanged()
    }

    inner class AttachedFilesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun onBind(fileData: FileData) {
            itemView.tvFileName.text = fileData.fileName
        }
    }
}