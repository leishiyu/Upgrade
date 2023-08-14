package com.yuu.upgrade.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.yuu.upgrade.api.UpdateApi
import com.yuu.upgrade.downloadManager.DownloadHandler
import com.yuu.upgrade.downloadManager.DownloadObserver
import com.yuu.upgrade.downloadManager.DownloadReceiver
import com.yuu.upgrade.listener.UpdateDialogListener
import com.yuu.upgrade.model.AppUpdate
import com.yuu.upgrade.view.UpdateDialogFragment
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.system.exitProcess

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Description:
 */
class UpdateManager : UpdateDialogListener, UpdateApi {
    /**
     * Context 弱引用
     */
    private lateinit var wrfContext: WeakReference<Context>

    /**
     * 系统的DownLoadManager
     */
    private lateinit var downloadManager: DownloadManager

    /**
     * 上次下载的id
     */
    private var lastDownloadId = -1L

    /**
     * 更新的实体参数
     */
    private lateinit var appUpdate: AppUpdate

    /**
     * 下载监听
     */
    private lateinit var downloadObserver: DownloadObserver

    /**
     * 更新提示对话卡
     */
    private lateinit var updateDialogFragment: UpdateDialogFragment

    private  var cancelClickListener: (() -> Unit)? = null

    /**
     * 开启下载更新
     */
    override fun startUpdate(context: Context, appUpdate: AppUpdate) {
        wrfContext = WeakReference(context)
        this.appUpdate = appUpdate
        isAutoInstall = appUpdate.isSilentMode
        val bundle = Bundle()
        bundle.putParcelable(UpdateDialogFragment.BUNDLE_KEY, appUpdate)
        updateDialogFragment = UpdateDialogFragment.newInstance(bundle).setDialogListener(this)
        updateDialogFragment.show(
            (context as FragmentActivity).supportFragmentManager,
            "UpdateManager"
        )
    }

     fun startUpdate(context: Context, appUpdate: AppUpdate, cancelClickListener:()->Unit) {
        this.cancelClickListener = cancelClickListener
        wrfContext = WeakReference(context)
        this.appUpdate = appUpdate
        isAutoInstall = appUpdate.isSilentMode
        val bundle = Bundle()
        bundle.putParcelable(UpdateDialogFragment.BUNDLE_KEY, appUpdate)
        updateDialogFragment = UpdateDialogFragment.newInstance(bundle).setDialogListener(this)
        updateDialogFragment.show(
            (context as FragmentActivity).supportFragmentManager,
            "UpdateManager"
        )
    }

    /**
     * 下载apk
     */
    private fun downloadApk() {
        try {
            val context = wrfContext.get()
            if (context != null) {
                //检测是否支持downloadManager 不支持走浏览器下载
                if (!downLoadMangerIsEnable(context)) {
                    downFromBrowser()
                    return
                }
                downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                clearCurrentTask()
                val downloadUrl = Objects.requireNonNull(appUpdate.newVersionLink)
                val uri = Uri.parse(downloadUrl)
                val request = DownloadManager.Request(uri)
                //下载中和下载完成通知栏展示
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                if (appUpdate.savePath.isEmpty()) {
                    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,context.packageName+".apk")
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.packageName + ".apk")?.let {
                        deleteApkFile(it)
                    }
                } else {
                    request.setDestinationInExternalFilesDir(context,appUpdate.savePath,context.packageName+".apk")
                    context.getExternalFilesDir(appUpdate.savePath + File.separator + context.packageName + ".apk")?.let {
                        deleteApkFile(it)
                    }
                }
//                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                // 部分机型（暂时发现Nexus 6P）无法下载，猜测原因为默认下载通过计量网络连接造成的，通过动态判断一下
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkMetered = connectivityManager.isActiveNetworkMetered
                request.setAllowedOverMetered(activeNetworkMetered)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    request.allowScanningByMediaScanner()
                }
                // 设置通知栏的标题
                request.setTitle(getAppName())
                // 设置通知栏的描述
                request.setDescription("正在下载中...")
                // 设置媒体类型为apk文件
                request.setMimeType(DownloadReceiver.INTENT_ACTION_TYPE)
                // 开启下载，返回下载id
                lastDownloadId = downloadManager.enqueue(request)
                // 如需要进度及下载状态，增加下载监听
                if (!appUpdate.isSilentMode) {
                    val downloadHandler = DownloadHandler(this)
                    downloadObserver =
                        DownloadObserver(downloadHandler, downloadManager, lastDownloadId)
                    context.contentResolver.registerContentObserver(
                        Uri.parse("content://downloads/my_downloads"),
                        true,
                        downloadObserver
                    )
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 防止有些厂商更改了系统的downloadManager
            downloadFromBrowse()
        }
    }


    /**
     * 设置下载的进度
     *
     * @param progress 进度
     */
    fun setProgress(progress: Int) {
        updateDialogFragment.setProgress(progress)
    }

    /**
     * 取消下载的监听
     */
    fun unregisterContentObserver() {
        if (wrfContext.get() != null) {
            wrfContext.get()!!.contentResolver.unregisterContentObserver(downloadObserver)
        }
    }

    /**
     * 显示下载失败
     */
    fun showFail() {
        updateDialogFragment.showFailBtn()
    }

    /**
     * 关闭提醒弹框
     */
    private fun dismissDialog() {
        if (updateDialogFragment.isShowing && wrfContext.get() != null && !(wrfContext.get() as Activity?)!!.isFinishing) {
            updateDialogFragment.dismiss()
        }
    }

    /**
     * 检查本地是否有已经下载的最新apk文件
     *
     * @param filePath 文件相对路劲
     */
    private fun checkLocalUpdate(filePath: String): File? {
        try {
            val context = wrfContext.get()
            val apkFile: File? = if (TextUtils.isEmpty(filePath)) {
                context!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.packageName + ".apk")
            } else {
                context!!.getExternalFilesDir(filePath + File.separator + context.packageName + ".apk")
            }
            // 注意系统的getExternalFilesDir（）方法如果找不到文件会默认当成目录创建
            if (apkFile != null && apkFile.isFile) {
                val packageManager = context.packageManager
                val packageInfo = packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.GET_ACTIVITIES
                )
                if (packageInfo != null) {
                    val apkVersionCode =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
                    if (apkVersionCode > getAppCode()) {
                        return apkFile
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "checkLocalUpdate:本地目录没有已经下载的新版本")
        }
        return null
    }


    /**
     * 获取应用的版本号
     *
     * @return 应用版本号
     */
    private fun getAppCode(): Long {
        try {
            val context = wrfContext.get()
            //获取包管理器
            val pm = context!!.packageManager
            //获取包信息
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            //返回版本号
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }
    /**
     * downloadManager 是否可用
     *
     * @param context 上下文
     * @return true 可用
     */
    private fun downLoadMangerIsEnable(context: Context): Boolean {
        val state = context.applicationContext.packageManager
            .getApplicationEnabledSetting("com.android.providers.downloads")
        return !(checkApplicationState(state) || checkComponentEnableState(state) || checkComponentDisableState(state))
    }

    private fun checkComponentDisableState(state: Int) =
        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED

    private fun checkComponentEnableState(state: Int) = state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER

    private fun checkApplicationState(state: Int) = state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    /**
     * 清除上一个任务，防止apk重复下载
     */
    fun clearCurrentTask() {
        try {
            if (lastDownloadId != -1L) {
                downloadManager.remove(lastDownloadId.toLong())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 下载前清空本地缓存的文件
     */
    private fun deleteApkFile(destFileDir: File) {
        if (!destFileDir.exists()) {
            return
        }
        if (destFileDir.isDirectory) {
            val files = destFileDir.listFiles()
            if (files != null) {
                for (f in files) {
                    deleteApkFile(f)
                }
            }
        }
        destFileDir.delete()
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用名称
     */
    private fun getAppName(): String? {
        try {
            val context = wrfContext.get()
            val packageManager = context!!.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val labelRes = packageInfo.applicationInfo.labelRes
            return context.resources.getString(labelRes)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "下载"
    }

    /**
     * 从浏览器打开下载，暂时没有选择应用市场，因为应用市场太多，而且协议不同，无法兼顾所有
     */
    private fun downloadFromBrowse() {
        try {
            val downloadUrl = if (TextUtils.isEmpty(appUpdate.browserDownLoadLink)) appUpdate.newVersionLink else appUpdate.browserDownLoadLink
            val intent = Intent()
            val uri = Uri.parse(downloadUrl)
            intent.action = Intent.ACTION_VIEW
            intent.data = uri
            wrfContext.get()!!.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d(TAG, "无法通过浏览器下载！")
        }
    }

    /**
     * 安装app
     *
     * @param apkFile 下载的文件
     */
    fun installApp(apkFile: File?) {
        try {
            val context = wrfContext.get()
            // 验证md5
            if (!TextUtils.isEmpty(appUpdate.md5)) {
                val md5IsRight = Md5Util.checkFileMd5(appUpdate.md5, apkFile)
                if (!md5IsRight) {
                    Toast.makeText(context, "为了安全性和更好的体验，为你推荐浏览器下载更新！", Toast.LENGTH_SHORT).show()
                    downloadFromBrowse()
                    return
                }
            }
            // 安装
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    DownloadReceiver.INTENT_ACTION_TYPE
                )
            } else {
                if (isSupportInstall(context)) return
                //Android7.0之后获取uri要用contentProvider
                val apkUri = FileProvider.getUriForFile(
                    context!!.applicationContext, context.packageName + ".fileProvider",
                    apkFile!!
                )
                //Granting Temporary Permissions to a URI
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(apkUri, DownloadReceiver.INTENT_ACTION_TYPE)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
            dismissDialog()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun isSupportInstall(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val allowInstall = context!!.packageManager.canRequestPackageInstalls()
            if (!allowInstall) {
                //不允许安装未知来源应用，请求安装未知应用来源的权限
                updateDialogFragment.requestInstallPermission()
                return true
            }
        }
        return false
    }

    /**
     * 获取下载的文件
     *
     * @return file
     */
    fun getDownloadFile(): File? {
        val query = DownloadManager.Query()
        val cursor = downloadManager.query(query.setFilterById(lastDownloadId))
        if (cursor != null && cursor.moveToFirst()) {
            val fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val apkPath = Uri.parse(fileUri).path
            if (!TextUtils.isEmpty(apkPath)) {
                return File(apkPath)
            }
            cursor.close()
        }
        return null
    }

    override fun forceExit() {
        // 回到退出整个应用，比较好的方式，先退到桌面，再杀掉应用，不然会黑屏闪烁
        dismissDialog()
        if (wrfContext.get() != null) {
            wrfContext.get()!!
                .startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
            (wrfContext.get() as Activity?)!!.finish()
        }
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun updateDownLoad() {
        // 立即更新
        val apkFile = checkLocalUpdate(appUpdate.savePath)
        if (apkFile != null) {
            // 本地存在新版本，直接安装
            installApp(apkFile)
        } else {
            // 不存在新版本，需要下载
            if (!appUpdate.isSilentMode) {
                // 非静默模式，直接在下载更新框内部显示下载进度
                updateDialogFragment.showProgressBtn()
            } else {
                // 静默模式，不显示下载进度
                dismissDialog()
            }
            // 开启下载
            downloadApk()
        }
    }

    override fun updateRetry() {
        // 重试
        val apkFile = checkLocalUpdate(appUpdate.savePath)
        if (apkFile != null) {
            // 本地存在新版本，直接安装
            installApp(apkFile)
        } else {
            // 不存在新版本，需要下载
            if (!appUpdate.isSilentMode) {
                // 非静默模式，直接在下载更新框内部显示下载进度
                updateDialogFragment.showProgressBtn()
            }
            // 开启下载
            downloadApk()
        }
    }

    override fun downFromBrowser() {
        // 从浏览器下载
        downloadFromBrowse()
    }

    override fun cancelUpdate() {
        // 取消更新
        clearCurrentTask()
        dismissDialog()
        if (0 != appUpdate.forceUpdate) {
            forceExit()
        }else{
            cancelClickListener?.invoke()
        }
    }

    override fun installApkAgain() {
        val context = wrfContext.get()
        if (context != null) {
            try {
                val downloadFile = checkLocalUpdate(appUpdate.savePath)
                val intent = Intent(Intent.ACTION_VIEW)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.setDataAndType(
                        Uri.fromFile(downloadFile),
                        DownloadReceiver.INTENT_ACTION_TYPE
                    )
                } else {
                    //Android7.0之后获取uri要用contentProvider
                    val apkUri = FileProvider.getUriForFile(
                        context.applicationContext, context.packageName + ".fileProvider",
                        downloadFile!!
                    )
                    //Granting Temporary Permissions to a URI
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setDataAndType(apkUri,  DownloadReceiver.INTENT_ACTION_TYPE)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(context, "请点击通知栏完成应用的安装！", Toast.LENGTH_SHORT).show()
            } finally {
                dismissDialog()
            }
        }
    }




    companion object {
        val TAG = "UpdateManager"

        /**
         * 自动安装标志
         */
        var isAutoInstall: Boolean? = null
    }

}