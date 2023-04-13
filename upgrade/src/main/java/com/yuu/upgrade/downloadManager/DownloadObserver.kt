package com.yuu.upgrade.downloadManager

import android.app.DownloadManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Message
import android.util.Log

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Describetion:
 */
class DownloadObserver : ContentObserver {

    /**
     * 记录成功或者失败的状态，主要用来只发送一次成功或者失败
     */
    private var isEnd = false

    private var downloadManager: DownloadManager? = null

    private var handler: Handler? = null

    private var query: DownloadManager.Query? = null

    constructor(handler: Handler, downloadManager: DownloadManager, downloadId: Long) : super(
        handler
    ) {
        this.handler = handler
        this.downloadManager = downloadManager
        query = DownloadManager.Query().setFilterById(downloadId)
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        queryDownloadStatus()
    }
    /**
     * 检查下载的状态
     */
    private fun queryDownloadStatus() {
        // Java 7 新的 try-with-resources ，凡是实现了AutoCloseable接口的可自动close()，所以此处不需要手动cursor.close()
        try {
            downloadManager?.query(query).use { cursor ->
                if (cursor != null && cursor.moveToNext()) {
                    val status: Int =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val totalSize: Long =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            .toLong()
                    val currentSize: Long =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            .toLong()
                    // 当前进度
                    val mProgress: Int = if (totalSize != 0L) {
                        (currentSize * 100 / totalSize).toInt()
                    } else {
                        0
                    }
                    Log.d(TAG, mProgress.toString())
                    when (status) {
                        DownloadManager.STATUS_PAUSED -> {
                            // 下载暂停
                            handler?.sendEmptyMessage(DownloadManager.STATUS_PAUSED)
                            Log.d(TAG, "STATUS_PAUSED")
                        }
                        DownloadManager.STATUS_PENDING -> {
                            // 开始下载
                            handler?.sendEmptyMessage(DownloadManager.STATUS_PENDING)
                            Log.d(TAG, "STATUS_PENDING")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            // 正在下载，不做任何事情
                            val message = Message.obtain()
                            message.what = DownloadManager.STATUS_RUNNING
                            message.arg1 = mProgress
                            handler?.sendMessage(message)
                            Log.d(TAG, "STATUS_RUNNING")
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            if (!isEnd) {
                                // 完成
                                handler?.sendEmptyMessage(DownloadManager.STATUS_SUCCESSFUL)
                                Log.d(TAG, "STATUS_SUCCESSFUL")
                            }
                            isEnd = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            if (!isEnd) {
                                handler?.sendEmptyMessage(DownloadManager.STATUS_FAILED)
                                Log.d(TAG, "STATUS_FAILED")
                            }
                            isEnd = true
                        }
                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    companion object{
        val TAG = javaClass.canonicalName
    }
}