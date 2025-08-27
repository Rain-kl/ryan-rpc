package io.ryan.common.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
public class RpcResponse implements Serializable {
    //状态信息
    private int code;
    private String message;
    //具体数据
    private Object data;

    //构造成功信息
    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).data(data).build();
    }

    //构造失败信息
    public static RpcResponse fail(String msg) {
        return RpcResponse.builder().code(500).message(msg).build();
    }

    public static RpcResponse fail(Integer code, String msg) {
        return RpcResponse.builder().code(code).message(msg).build();
    }
}

