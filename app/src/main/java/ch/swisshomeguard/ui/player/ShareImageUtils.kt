package ch.swisshomeguard.ui.player

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.content.FileProvider
import ch.swisshomeguard.R
import ch.swisshomeguard.utils.HomeguardTokenUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Screenshot
// https://stackoverflow.com/a/61136470/
// https://medium.com/@shiveshmehta09/taking-screenshot-programmatically-using-pixelcopy-api-83c84643b02a

// Sharing
// https://stackoverflow.com/questions/9049143/android-share-intent-for-a-bitmap-is-it-possible-not-to-save-it-prior-sharing
// https://stackoverflow.com/questions/55561841/how-to-save-a-bitmap-in-and-share-from-cache
// https://developer.android.com/training/sharing/send#kotlin

// FileProvider
// https://developer.android.com/reference/androidx/core/content/FileProvider
// https://techenum.com/learn-how-to-use-fileprovider-in-android-with-example/
// https://stackoverflow.com/a/41494032/5987516

// DownloadManager
// https://www.youtube.com/watch?v=Vx-NY81Gpds
// https://github.com/commonsguy/cw-android/blob/master/Internet/Download/src/com/commonsware/android/download/DownloadDemo.java

const val FILE_PROVIDER_AUTHORITY =
    "ch.swisshomeguard.fileprovider" // this is specified in AndroidManifest.xml
const val IMAGE_CACHE_DIR = "images" // this is specified in res/xml/filepaths.xml
const val TEMP_IMAGE_FILE_NAME = "image.jpg"

fun makeVideoScreenshot(surfaceView: SurfaceView, callback: (Bitmap?) -> Unit) {
    val bitmap: Bitmap = Bitmap.createBitmap(
        surfaceView.width,
        surfaceView.height,
        Bitmap.Config.ARGB_8888
    )
    try {
        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        PixelCopy.request(
            surfaceView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    callback(bitmap)
                }
                handlerThread.quitSafely()
            },
            Handler(handlerThread.looper)
        )
    } catch (e: IllegalArgumentException) {
        callback(null)
        e.printStackTrace()
    }
}

fun shareImage(context: Context, bitmap: Bitmap) {
    saveImageToCache(context, bitmap)
    shareImageFromCache(context)
}

private fun saveImageToCache(context: Context, bitmap: Bitmap) {
    try {
        val imageCachePath = File(context.cacheDir, IMAGE_CACHE_DIR)
        imageCachePath.mkdirs() // Create directory if nonexistent

        val stream = FileOutputStream("$imageCachePath/$TEMP_IMAGE_FILE_NAME")
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            80,
            stream
        ) // This file type and quality produced good quality images with ~200 KB
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

private fun shareImageFromCache(context: Context) {
    val imageCachePath = File(context.cacheDir, IMAGE_CACHE_DIR)
    val imageFile = File(imageCachePath, TEMP_IMAGE_FILE_NAME)
    val contentUri = FileProvider.getUriForFile(
        context,
        FILE_PROVIDER_AUTHORITY,
        imageFile
    )

    if (contentUri != null) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
        shareIntent.setDataAndType(
            contentUri,
            context.contentResolver.getType(contentUri)
        )
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        context.startActivity(Intent.createChooser(shareIntent, null))
    }
}

fun saveVideoToDownloads(context: Context, url: String) {
    // TODO Add a more specific file name SystemCameraEventXXX.mp4
    val videoFileName = "${context.getString(R.string.player_video_file)}.mp4"

    val downloadManagerRequest =
        DownloadManager.Request(Uri.parse(url))
            .addRequestHeader("Authorization", "Bearer ${HomeguardTokenUtils.readHomeguardToken()}")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                videoFileName
            ) // Suffixes are added to the file name when required (e.g. video-1.mp4)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

    val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    val enqueue = downloadManager.enqueue(downloadManagerRequest)

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val longExtra = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (enqueue == longExtra) {
                if (context != null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.player_video_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // TODO Do I need to unregister the broadcast receiver?
    context.registerReceiver(
        broadcastReceiver,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    )
}
