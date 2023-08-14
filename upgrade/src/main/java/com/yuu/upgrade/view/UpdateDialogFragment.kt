package com.yuu.upgrade.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.yuu.update.R
import com.yuu.upgrade.listener.UpdateDialogListener
import com.yuu.upgrade.model.AppUpdate
import java.lang.String

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Description:
 */
class UpdateDialogFragment : BaseDialog() {
    /**
     * 进度条
     */
    private lateinit var progressBar: XDownloadProgressBar

    /**
     * 更新背景图
     */
    private lateinit var imageView:ImageView
    /**
     * 底部按钮事件的根布局
     */
    private lateinit var llEvent: FlexboxLayout

    /**
     * 取消更新按钮
     */
    private lateinit var btnCancelUpdate: Button

    /**
     * 稍后更新按钮
     */
    private lateinit var btnUpdateLater: Button

    /**
     * 立即更新按钮
     */
    private lateinit var btnUpdateNow: Button

    /**
     * 浏览器下载按钮
     */
    private lateinit var btnBrowserDownLoad: Button

    /**
     * 重试下载
     */
    private lateinit var btnUpdateRetry: Button

    /**
     * 取消更新
     */
    private lateinit var btnUpdateExit: Button

    /**
     * 更新数据
     */
    private var appUpdate: AppUpdate? = null

    /**
     * Dialog的事件监听接口
     */
    private lateinit var dialogListener: UpdateDialogListener


    /**
     * 设置监听事件
     */
    fun setDialogListener(dialogListener: UpdateDialogListener): UpdateDialogFragment {
        this.dialogListener = dialogListener
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            appUpdate = it.getParcelable(BUNDLE_KEY)
            if (appUpdate != null && appUpdate!!.updateResourceId != 0) {
                return inflater.inflate(appUpdate!!.updateResourceId, container, false)
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (appUpdate == null) {
            dismiss()
            return
        }
        appUpdate?.let {
            if (it.updateResourceId == R.layout.laytout_update_dialog) {
                imageView = view.findViewById(R.id.iv_bg)
                it.updateTitleUrl?.let {
                    Glide.with(requireActivity())
                        .load(it)
                        .into(imageView)
                }?: kotlin.run {
                    imageView.setImageResource(R.mipmap.image_update_bg)
                }
                //更新标题
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                //强制更新提示语
//                val tvForceUpdate = view.findViewById<TextView>(R.id.tvForceUpdate)

                //文件大小
                val tvSize = view.findViewById<TextView>(R.id.tvFileSize)
                //更新内容title
                val tvContentTips = view.findViewById<TextView>(R.id.tvContentTips)

                // 更新的标题
                tvTitle.setText(it.updateDialogTitle)
                // 底部按钮事件的根布局
                llEvent = view.findViewById(R.id.llEvent)
                // 进度条
                progressBar = view.findViewById(R.id.progress)
                // 浏览器下载
                btnBrowserDownLoad = view.findViewById(R.id.btnUpdateBrowse)
                // 取消更新
//                btnCancelUpdate = view.findViewById(R.id.btnCancelUpdate)
                // 重新下载
                btnUpdateRetry = view.findViewById(R.id.btnUpdateRetry)
                // 取消更新（退出应用）
//                btnUpdateExit = view.findViewById(R.id.btnUpdateExit)

                if (TextUtils.isEmpty(it.appSize)) {
                    tvSize.visibility = View.GONE
                } else {
                    tvSize.visibility = View.VISIBLE
                    tvSize.text = String.format(
                        resources.getString(R.string.update_size), it.appSize
                    )
                }
                tvContentTips.setText(it.updateContentTips)

//                if (it.forceUpdate == 0) {
//                    tvForceUpdate.visibility = View.GONE
//                } else {
//                    tvForceUpdate.visibility = View.VISIBLE
//                }
//                //取消更新
//                btnCancelUpdate.setOnClickListener {
//                    dialogListener.cancelUpdate()
//                }
                //浏览器更新
                btnBrowserDownLoad.setOnClickListener {
                    dialogListener.downFromBrowser()
                }
                //重试更新
                btnUpdateRetry.setOnClickListener {
                    dialogListener.updateRetry()
                }
//                //退出更新
//                btnCancelUpdate.setOnClickListener {
//                    dialogListener.cancelUpdate()
//                }
//                btnUpdateExit.setOnClickListener {
//                    dialogListener.cancelUpdate()
//                }
            }
            //版本号
            val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
            if (TextUtils.isEmpty(it.appVersion)) {
                tvVersion.visibility = View.GONE
            } else {
                tvVersion.visibility = View.VISIBLE
                tvVersion.text = String.format(
                    resources.getString(R.string.update_version), it.appVersion
                )
            }
            //更新内容
            val tvContent = view.findViewById<TextView>(R.id.tvContent)
            tvContent.text =
                if (TextUtils.isEmpty(it.updateContent)) resources.getString(R.string.default_update_content)
                else it.updateContent
            tvContent.movementMethod = ScrollingMovementMethod()

            //稍后更新文案背景设置
            btnUpdateLater = view.findViewById(R.id.btnUpdateLater)
            btnUpdateLater.setText(it.cancelButtonText)
            //立即更新设置
            btnUpdateNow = view.findViewById(R.id.btnUpdateNow)
            btnUpdateNow.setText(it.updateButtonText)

            //强制更新 稍后更新按钮隐藏
            if (it.forceUpdate == 0) {
                btnUpdateLater.visibility = View.VISIBLE
            } else {
                btnUpdateLater.visibility = View.GONE
            }
            //稍后更新
            btnUpdateLater.setOnClickListener {
                dialogListener.cancelUpdate()
            }
            btnUpdateNow.setOnClickListener {
                requestPermission()
            }
        }
    }

    /**
     * 设置进度
     */
    fun setProgress(currentProgress: Int) {
        if(this::progressBar.isInitialized && currentProgress > 0 ) {
            progressBar.setCurrentProgress(currentProgress)
        }
    }

    /**
     * 开启进度条，若强制更新则隐藏底部所有按钮只显示进度条
     * 否则显示取消更新按钮，隐藏稍后更新与立即更新
     */
    fun showProgressBtn() {
        if(this::progressBar.isInitialized){
            progressBar.visibility = View.VISIBLE
            progressBar.setCurrentProgress(0)
        }
        appUpdate?.let {
            if (0 == it.forceUpdate) {
                llEvent.visibility = View.VISIBLE
                btnUpdateLater.visibility = View.GONE
                btnUpdateNow.visibility = View.GONE
//                btnCancelUpdate.visibility = View.VISIBLE
                btnBrowserDownLoad.visibility = View.GONE
//                btnUpdateExit.visibility = View.GONE
                btnUpdateRetry.visibility = View.GONE
            } else {
                //强制更新
                llEvent.visibility = View.GONE
            }
        }
    }

    /**
     * 下载失败
     * 强制更新策略：显示重试下载  浏览器下载 退出应用
     * 非强制更新策略：显示重试下载  浏览器下载 取消
     */
    fun showFailBtn() {
        Toast.makeText(context, "更新下载失败，请重试！", Toast.LENGTH_SHORT).show()
        if(this::progressBar.isInitialized){
            progressBar.visibility = View.GONE
        }
        // 非强制更新
        llEvent.visibility = View.VISIBLE
        btnUpdateLater.visibility = View.GONE
        btnUpdateNow.visibility = View.GONE
//        btnCancelUpdate.visibility = View.GONE
//        btnBrowserDownLoad.visibility = View.VISIBLE
//        btnUpdateExit.visibility = View.VISIBLE
        btnUpdateRetry.visibility = View.VISIBLE
//        appUpdate?.let {
//            if (0 == it.forceUpdate) {
//                // 非强制更新
//                btnUpdateExit.text = "取消"
//            } else {
//                // 强制更新
//                btnUpdateExit.text = "退出"
//            }
//        }
    }

    /**
     * 判断存储卡权限
     */
    private fun requestPermission() {
        if (activity == null) {
            return
        }
        //权限判断是否有访问外部存储空间权限
        val flag = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (flag != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                Toast.makeText(
                    activity,
                    resources.getString(R.string.update_permission),
                    Toast.LENGTH_LONG
                ).show()
            }
            // 申请授权
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            dialogListener.updateDownLoad()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.laytout_update_dialog
    }

    /**
     * 申请android O 安装权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestInstallPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
            INSTALL_PACKAGES_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //8.0应用设置界面未知安装开源返回时候
        if (requestCode == GET_UNKNOWN_APP_SOURCES && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val allowInstall =
                requireActivity().packageManager.canRequestPackageInstalls()
            if (allowInstall) {
                dismiss()
                dialogListener.installApkAgain()
            } else {
                Toast.makeText(context, "您拒绝了安装未知来源应用，应用暂时无法更新！", Toast.LENGTH_SHORT).show()
                exit()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<kotlin.String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //6.0 存储权限授权结果回调
                    dialogListener.updateDownLoad()
            } else {
                //提示，并且关闭
                Toast.makeText(
                    activity,
                    resources.getString(R.string.update_permission),
                    Toast.LENGTH_LONG
                ).show()
                exit()
            }
        } else if (requestCode == INSTALL_PACKAGES_REQUEST_CODE) {
            // 8.0的权限请求结果回调,授权成功
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dialogListener.installApkAgain()
            } else {
                // 授权失败，引导用户去未知应用安装的界面
                if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //注意这个是8.0新API
                    val packageUri = Uri.parse("package:" + requireContext().packageName)
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
                    startActivityForResult(
                        intent,
                        GET_UNKNOWN_APP_SOURCES
                    )
                }
            }
        }
    }
    /**
     * 强制退出
     */
    private fun exit() {
        appUpdate?.let {
            if (0 != it.forceUpdate) {
                dialogListener.forceExit()
            } else {
                dismiss()
            }
        }
    }

    companion object {
        /**
         * Android 8.0应用授权码
         */
        const val INSTALL_PACKAGES_REQUEST_CODE = 1112

        /**
         * 用户跳转未知应用安装胡界面请求码
         */
        const val GET_UNKNOWN_APP_SOURCES = 1113

        const val BUNDLE_KEY = "APP_UPDATE"

        fun newInstance(params: Bundle): UpdateDialogFragment {
            val dialog = UpdateDialogFragment()
            dialog.arguments = params
            return dialog
        }
    }


}
