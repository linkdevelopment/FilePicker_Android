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

package com.linkdev.filepicker.mapper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Caller is a mapper class to get context and startActivityForResult based on base caller is fragment or activity
 * @param fragment for host fragment
 * @param activity for host activity
 */
class Caller private constructor(
    private val fragment: Fragment? = null,
    private val activity: Activity? = null
) {
    companion object {
        fun getInstance(caller: Any): Caller {
            return when (caller) {
                is Activity -> Caller(activity = caller)
                is Fragment -> Caller(fragment = caller)
                else -> throw(Throwable("please pass reference of Activity or androidx.fragment.app"))
            }
        }
    }

    val context: Context
        get() = (activity ?: fragment?.requireContext())!!

    fun startActivityForResult(intent: Intent, requestCode: Int) {
        activity?.startActivityForResult(intent, requestCode)
            ?: fragment?.startActivityForResult(intent, requestCode)
    }
}