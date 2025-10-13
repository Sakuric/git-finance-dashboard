package com.example.financedashboard.utils;

import lombok.Data;

/**
 * 统一返回结果类
 * 用于封装API接口的返回结果，使前端能够统一处理响应数据
 * 使用泛型T可以支持不同类型的返回数据
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class Result<T> {
    private Integer code;    // 状态码，200表示成功，其他表示失败
    private String message;  // 返回消息，描述请求结果
    private T data;          // 返回数据，泛型类型，可以是任意类型的数据

    /**
     * 成功响应的静态工厂方法
     * @param data 返回的数据
     * @return 封装后的Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);           // 设置成功状态码
        result.setMessage("success");  // 设置成功消息
        result.setData(data);          // 设置返回数据
        return result;
    }

    /**
     * 失败响应的静态工厂方法
     * @param code 错误状态码
     * @param message 错误消息
     * @return 封装后的Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);          // 设置错误状态码
        result.setMessage(message);    // 设置错误消息
        return result;
    }
}