package com.yuu.upgrade.listener

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/11/4
 * @Description:
 */
open interface OnProgressBarListener {
    /**
     * onProgressChange
     *
     * @param current 当前进度
     * @param max     最大进度
     */
    fun onProgressChange(current: Int, max: Int)
}
