package com.demo.market.callbacks;

/**
 * 图片详情Activity回调
 */
public interface ImageActivityCallback extends BaseCallback{
    /**
     * 成功时
     */
    void onSuccess();

    /**
     * 失败时
     */
    void onFail();

}
