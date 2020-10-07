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

package com.linkdev.filepicker_android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linkdev.filepicker.models.MimeType
import com.linkdev.filepicker_android.R
import kotlinx.android.synthetic.main.item_mime_types.view.*

class MimeTypesAdapter(private val context: Context) :
    RecyclerView.Adapter<MimeTypesAdapter.MimeTypesViewHolder>() {
    private var mimeTypesList = MimeType.toList()
    private var checkedMimeTypesList = arrayListOf<MimeType>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MimeTypesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mime_types, parent, false)
        return MimeTypesViewHolder(view)
    }

    override fun getItemCount(): Int = mimeTypesList.size

    override fun onBindViewHolder(holder: MimeTypesViewHolder, position: Int) {
        holder.onBind(mimeTypesList[position])
    }

    fun getCheckedMimeTypeList(): ArrayList<MimeType> {
        return checkedMimeTypesList
    }

    inner class MimeTypesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(mimeType: MimeType) {
            itemView.cbMimeType.text = mimeType.mimeTypeName

            itemView.cbMimeType.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkedMimeTypesList.add(mimeType)
                } else {
                    checkedMimeTypesList.remove(mimeType)
                }
            }
        }
    }
}