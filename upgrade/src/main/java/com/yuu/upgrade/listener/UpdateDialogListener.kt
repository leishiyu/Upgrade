package com.yuu.upgrade.listener

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Description:
 */
interface UpdateDialogListener {
    /**
     * 强制退出，回调给app处理退出应用
     */
    fun forceExit()

    /**
     * 点击立即更新，用来开启权限检查和下载服务
     */
    fun updateDownLoad()

    /**
     * 重试按钮，进行重新下载
     */
    fun updateRetry()

    /**
     * 若应用下载失败，可以选择去应用市场下载或者去浏览器下载
     */
    fun downFromBrowser()

    /**
     * 取消更新
     */
    fun cancelUpdate()

    /**
     * 重新安装
     */
    fun installApkAgain()
}