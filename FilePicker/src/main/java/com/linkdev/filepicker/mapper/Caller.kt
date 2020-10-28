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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import java.security.Permission

/**
 * Caller is a mapper class to get context and startActivityForResult based on caller is fragment or activity
 * and check runtime permission grated
 * @param fragment for host fragment
 * @param activity for host activity
 */
class Caller private constructor(
    private val fragment: Fragment? = null,
    private val activity: AppCompatActivity? = null
) {
    companion object {
        fun getInstance(caller: Any): Caller {
            return when (caller) {
                is AppCompatActivity -> Caller(activity = caller)
                is Fragment -> Caller(fragment = caller)
                else -> throw(Throwable("please pass reference of androidx.appcompat.app.AppCompatActivity or androidx.fragment.app"))
            }
        }
    }

    val context: Context
        get() = (activity ?: fragment?.requireContext())!!

    val lifecycleScope: LifecycleCoroutineScope
        get() = (activity?.lifecycleScope ?: fragment?.lifecycleScope)!!

    fun startActivityForResult(intent: Intent, requestCode: Int) {
        activity?.startActivityForResult(intent, requestCode)
            ?: fragment?.startActivityForResult(intent, requestCode)
    }

    fun isCameraPermissionsGranted(): Boolean {
        val cameraPermission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        for (permission: String in cameraPermission) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    fun isDocumentPermissionsGranted(): Boolean {
        val cameraPermission = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        for (permission: String in cameraPermission) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }
}