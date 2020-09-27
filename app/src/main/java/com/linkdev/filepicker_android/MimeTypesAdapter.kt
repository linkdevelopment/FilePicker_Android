package com.linkdev.filepicker_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkdev.filepicker.models.MimeType
import kotlinx.android.synthetic.main.item_mime_types.view.*

class MimeTypesAdapter(private val context: Context) :
    RecyclerView.Adapter<MimeTypesAdapter.MimeTypesViewHolder>() {
    private var mimeTypesList = MimeType.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MimeTypesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mime_types, parent, false)
        return MimeTypesViewHolder(view)
    }

    override fun getItemCount(): Int = mimeTypesList.size

    override fun onBindViewHolder(holder: MimeTypesViewHolder, position: Int) {
        holder.onBind(mimeTypesList[position])
    }

    inner class MimeTypesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(mimeType: MimeType) {
            itemView.cbMimeType.text = mimeType.mimeTypeName
        }
    }
}