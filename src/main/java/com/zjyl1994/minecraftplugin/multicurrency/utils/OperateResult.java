/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

/**
 * 操作结果
 * @author zjyl1
 */
public class OperateResult {
    /**
     * 操作是否成功
     */
    private boolean success;
    /**
     * 附加说明
     */
    private String reason;
    /**
     * 附加数据
     */
    private Object data;
    
    public OperateResult(boolean success) {
        this(success, null);
    }
    
    public OperateResult(boolean success, String reason) {
        this(success, reason, null);
    }
    
    public OperateResult(boolean success, String reason, Object data){
        this.success = success;
        this.reason = reason;
        this.data = data;
    }
    
    public boolean getSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
    
    public Object getData() {
        return data;
    }
}
