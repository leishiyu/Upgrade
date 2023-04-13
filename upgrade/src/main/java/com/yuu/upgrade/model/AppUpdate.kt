package com.yuu.upgrade.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.yuu.update.R
import kotlinx.android.parcel.Parcelize

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Describetion: 下载实体类
 */
@Parcelize
class AppUpdate(
    /**
     * App版本号
     */
    var appVersion: String? = null,

    /**
     * AppVersionCode
     */
    var appVersionCode: Int? = null,

    /**
     * 新版本下载链接
     */
    var newVersionLink: String? = null,

    /**
     * 是否采取强制更新 0：不强制  1：强制
     */
    var forceUpdate: Int = 0,

    /**
     * 更新背景URL
     */
    var updateTitleUrl :String ?=null,
    /**
     * 新版本更新内容
     */
    var updateInfo: String? = null,

    /**
     * 新版本文件大小，一般需要自己换算成需要单位
     */
    var appSize: String? = null,

    /**
     * 文件下载保存路径
     */
    var savePath: String = "/download/",

    /**
     * 安装文件的MD5,用于校验签名是否一致
     */
    var md5: String? = null,

    /**
     * 若下载失败，走浏览器下载的地址
     */
    var browserDownLoadLink: String? = null,


    /**
     * 更新弹窗标题
     */
    @StringRes
    var updateDialogTitle: Int = R.string.update_title,

    /**
     * 更新内容提示文案
     */
    @StringRes
    var updateContentTips: Int = R.string.update_content_lb,

    /**
     * 更新内容
     */
    var updateContent: String = "",

    /**
     * 更新按钮的文字
     */
    @StringRes
    var updateButtonText: Int = R.string.update_text,

    /**
     * 取消更新按钮文案
     */
    @StringRes
    var cancelButtonText: Int = R.string.update_later,

    /**
     * 更新按钮颜色
     */
    @ColorRes
    var updateButtonColor: Int = R.color.updateColor,

    /**
     * 取消更新按钮颜色
     */
    @ColorRes
    var cancelButtonColor: Int = R.color.color_blue,

    /**
     * 下载进度条颜色
     */
    @ColorRes
    var updateProgressColor: Int = R.color.updateColor,

    /**
     * 风格：true代表默认静默下载模式，只弹出下载更新框,下载完毕自动安装， false 代表配合使用进度框与下载失败弹框
     */
    var isSilentMode: Boolean = true,


    /**
     * 更新对话框id
     */
    @LayoutRes
    var updateResourceId: Int = R.layout.laytout_update_dialog,


    ) : Parcelable {

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val appUpdate: AppUpdate = AppUpdate()

        /**
         * App版本号
         */
        fun setAppVersion(appVersion: String): Builder {
            appUpdate.appVersion = appVersion
            return this
        }

        /**
         * AppVersionCode
         */
        fun setAppVersionCode(appVersionCode: Int): Builder {
            appUpdate.appVersionCode = appVersionCode
            return this
        }

        /**
         * 新版本下载链接
         */
        fun setNewVersionLink(newVersionLink: String): Builder {
            appUpdate.newVersionLink = newVersionLink
            return this
        }

        /**
         * 是否采取强制更新 0：不强制  1：强制
         */
        fun setForceUpdate(forceUpdate: Int): Builder {
            appUpdate.forceUpdate = forceUpdate
            return this
        }

        /**
         * 更新弹窗标题图片
         */
        fun setUpdateTitleUrl(updateTitleUrl:String): Builder {
            appUpdate.updateTitleUrl = updateTitleUrl
            return this
        }

        /**
         * 更新信息
         */
        fun setUpdateInfo(updateInfo: String): Builder {
            appUpdate.updateInfo = updateInfo
            return this
        }

        /**
         * 更新App大小
         */
        fun setAppSize(appSize: String): Builder {
            appUpdate.appSize = appSize
            return this
        }

        /**
         * 下载保存路径
         */
        fun setSavePath(savePath: String): Builder {
            appUpdate.savePath = savePath
            return this
        }

        /**
         * 设置APP MD5  默认为null 不校验
         */
        fun setMD5(md5: String): Builder {
            appUpdate.md5 = md5
            return this
        }

        /**
         * 浏览器下载地址  下载失败后走浏览器下载
         */
        fun setBrowserDownLoadLink(browserDownLoadLink: String): Builder {
            appUpdate.browserDownLoadLink = browserDownLoadLink
            return this
        }

        /**
         * 更新弹窗标题
         */
        fun setUpdateDialogTitle(@StringRes updateDialogTitle: Int): Builder {
            appUpdate.updateDialogTitle = updateDialogTitle
            return this
        }

        /**
         * 更新弹窗内容提示
         */
        fun setUpdateContentTips(@StringRes updateContentTips: Int): Builder {
            appUpdate.updateContentTips = updateContentTips
            return this
        }

        /**
         * 更新提示
         */
        fun setUpdateContent(updateContent: String): Builder {
            appUpdate.updateContent = updateContent
            return this
        }

        /**
         * 更新按钮文本
         */
        fun setUpdateButtonText(@StringRes updateButtonText: Int): Builder {
            appUpdate.updateButtonText = updateButtonText
            return this
        }

        /**
         * 取消更新按钮文本
         */
        fun setCancelButtonText(@StringRes cancelButtonText: Int): Builder {
            appUpdate.cancelButtonText = cancelButtonText
            return this
        }

        /**
         * 更新按钮颜色
         */
        fun setUpdateButtonColor(@ColorRes updateButtonColor: Int): Builder {
            appUpdate.updateButtonColor = updateButtonColor
            return this
        }

        /**
         * 取消更新按钮颜色
         */
        fun setCancelButtonColor(@ColorRes cancelButtonColor: Int): Builder {
            appUpdate.cancelButtonColor = cancelButtonColor
            return this
        }

        fun setUpdateProgressColor(@ColorRes updateProgressColor: Int): Builder {
            appUpdate.updateProgressColor = updateProgressColor
            return this
        }

        /**
         * 风格：true代表默认静默下载模式，只弹出下载更新框,下载完毕自动安装， false 代表配合使用进度框与下载失败弹框
         */
        fun setIsSilentMode(isSilentMode: Boolean): Builder {
            appUpdate.isSilentMode = isSilentMode
            return this
        }

        /**
         * 自定义更新弹窗布局资源
         */
        fun setUpdateResourceId(@LayoutRes updateResourceId: Int): Builder {
            appUpdate.updateResourceId = updateResourceId
            return this
        }

        fun build() = appUpdate
    }
}

