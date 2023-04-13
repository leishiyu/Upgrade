package com.yuu.upgrade.downloadManager

import android.app.DownloadManager
import android.os.Handler
import android.os.Message
import com.yuu.upgrade.utils.UpdateManager
import java.lang.ref.WeakReference

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/4
 * @Description:
 */
class DownloadHandler(updateManager: UpdateManager?) : Handler() {

    private var wrfUpdateManager: WeakReference<UpdateManager?>? = null

    init {
        wrfUpdateManager = WeakReference<UpdateManager?>(updateManager)
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            DownloadManager.STATUS_PAUSED -> {}
            DownloadManager.STATUS_PENDING -> {}
            DownloadManager.STATUS_RUNNING ->                 // 下载中
                if (wrfUpdateManager!!.get() != null) {
                    wrfUpdateManager!!.get()?.setProgress(msg.arg1)
                }
            DownloadManager.STATUS_SUCCESSFUL -> {
                if (wrfUpdateManager!!.get() != null) {
                    wrfUpdateManager!!.get()?.setProgress(100)
                    wrfUpdateManager!!.get()?.unregisterContentObserver()
                }
                if (wrfUpdateManager!!.get() != null) {
                    wrfUpdateManager!!.get()
                        ?.installApp(wrfUpdateManager!!.get()?.getDownloadFile())
                }
            }
            DownloadManager.STATUS_FAILED ->                 // 下载失败，清除本次的下载任务
                if (wrfUpdateManager!!.get() != null) {
                    wrfUpdateManager!!.get()?.clearCurrentTask()
                    wrfUpdateManager!!.get()?.unregisterContentObserver()
                    wrfUpdateManager!!.get()?.showFail()
                }
            else -> {}
        }
    }
}