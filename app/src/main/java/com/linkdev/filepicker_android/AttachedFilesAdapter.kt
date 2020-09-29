/*
 * Copyright (C) 2020 Link Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private var attachedFilesList = arrayListOf<FileData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachedFilesViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_attached_file, parent, false)
        return AttachedFilesViewHolder(view)
    }

    override fun getItemCount(): Int = attachedFilesList.size

    override fun onBindViewHolder(holder: AttachedFilesViewHolder, position: Int) {
        holder.onBind(attachedFilesList[position])
    }

    fun replaceFiles(attachedFiles: ArrayList<FileData>) {
        attachedFilesList = attachedFiles
        notifyDataSetChanged()
    }

    inner class AttachedFilesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun onBind(fileData: FileData) {
            itemView.tvFileName.text = fileData.fileName
        }
    }
}