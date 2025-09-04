package com.tcr.system.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应类
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功，带数据
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功，带数据和消息
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败，带消息
     */
    public static <T> Result<T> error(String message) {
        return error(message, 500);
    }

    /**
     * 失败，带消息和状态码
     */
    public static <T> Result<T> error(String message, Integer code) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败，与error方法相同，用作别名
     */
    public static <T> Result<T> fail() {
        return error();
    }
    
    /**
     * 失败，带消息，与error方法相同，用作别名
     */
    public static <T> Result<T> fail(String message) {
        return error(message);
    }
    
    /**
     * 失败，带消息和状态码，与error方法相同，用作别名
     */
    public static <T> Result<T> fail(String message, Integer code) {
        return error(message, code);
    }
}