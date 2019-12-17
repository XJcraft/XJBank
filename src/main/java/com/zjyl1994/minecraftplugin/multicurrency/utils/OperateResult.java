/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

/**
 *
 * @author zjyl1
 */
public class OperateResult {
    Boolean success;
    String reason;
    Object data;
    
    public OperateResult(Boolean success){
        this.success = success;
    }
    
    public OperateResult(Boolean success,String reason){
        this.success = success;
        this.reason = reason;
    }
    
    public OperateResult(Boolean success,String reason,Object data){
        this.success = success;
        this.reason = reason;
        this.data = data;
    }
    
    public Boolean getSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
    
    public Object getData() {
        return data;
    }
}
