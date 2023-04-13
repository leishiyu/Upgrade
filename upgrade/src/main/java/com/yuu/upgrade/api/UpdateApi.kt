package com.yuu.upgrade.api

import android.content.Context
import com.yuu.upgrade.model.AppUpdate

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/12/12
 * @Description:
 */
interface UpdateApi {
    /**
     * 开始更新
     */
    fun startUpdate(context: Context, appUpdate: AppUpdate)

}