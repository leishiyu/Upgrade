package com.yuu.upgrade.view

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.yuu.update.R

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/3
 * @Description:
 */
abstract class BaseDialog() : AppCompatDialogFragment() {
    /**
     * 是否正在显示,防止在特殊情况下弹出多层
     */
    var isShowing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BaseDialogFragment)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.apply {
                setGravity(Gravity.CENTER)
                setLayout(dp2px(605),dp2px(439))
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(getLayoutId() == 0){
            throw java.lang.NullPointerException("请在getLayoutId()方法中传入布局Id")
        }
        return inflater.inflate(getLayoutId(),container,false)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            if(isShowing) return
            super.show(manager, tag)
            isShowing = true
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

    }

    override fun dismiss() {
        try {
            super.dismissAllowingStateLoss()
            isShowing = false
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getWidth(window: Window): Int {
        val wm = window.windowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }
    fun dp2px(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
    @LayoutRes
    abstract fun getLayoutId():Int
}