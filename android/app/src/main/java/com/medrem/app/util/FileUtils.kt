package com.medrem.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val fileName = getFileName(context, uri) ?: "upload_file_${System.currentTimeMillis()}"
        val file = File(context.cacheDir, fileName)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
    fun createImageUri(context: Context): Uri {
        val fileName = "camera_capture_${System.currentTimeMillis()}.jpg"
        // Must use externalCacheDir — internal cacheDir is not accessible by the camera app
        val storageDir = context.externalCacheDir ?: context.cacheDir
        val file = File(storageDir, fileName)
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
