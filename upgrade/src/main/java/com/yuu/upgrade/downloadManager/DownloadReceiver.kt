package com.yuu.upgrade.downloadManager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.yuu.upgrade.utils.UpdateManager
import java.io.File
import java.util.*

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/4
 * @Description:
 */
class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1 != null && UpdateManager.isAutoInstall == true) {
            if (Objects.requireNonNull<String>(p1.action) == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                // 下载完成
                val downloadId: Long = p1.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                // 自动安装app
                if (p0 != null) {
                    installApp(p0, downloadId)
                }
            } else if (p1.action == DownloadManager.ACTION_NOTIFICATION_CLICKED) {
                // 未下载完成，点击跳转系统的下载管理界面
                val viewDownloadIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                viewDownloadIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                p0?.startActivity(viewDownloadIntent)
            }
        }
    }

    /**
     * 安装app
     *
     * @param context    上下文
     * @param downloadId 下载任务id
     */
    private fun installApp(context: Context, downloadId: Long) {
        try {
            if (downloadId == -1L) {
                return
            }
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            val cursor = downloadManager.query(query.setFilterById(downloadId))
            if (cursor != null && cursor.moveToFirst()) {
                val fileUri =
                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                val path = Uri.parse(fileUri).path
                cursor.close()
                if (!TextUtils.isEmpty(path)) {
                    val apkFile = File(path)
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        intent.setDataAndType(
                            Uri.fromFile(apkFile),
                            "application/vnd.android.package-archive"
                        )
                    } else {
                        //Android7.0之后获取uri要用contentProvider
                        val apkUri = FileProvider.getUriForFile(
                            context.applicationContext,
                            context.packageName + ".fileProvider",
                            apkFile
                        )
                        //Granting Temporary Permissions to a URI
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}