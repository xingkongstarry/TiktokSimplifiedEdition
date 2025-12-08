// 新建文件: main/java/com/starry/tiktoksimplifiededition/utils/Resource.java
package com.starry.tiktoksimplifiededition.utils;

public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR, EMPTY }
    public final Status status;
    public final T data;
    public final String message;

    public Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> loading(T data) { return new Resource<>(Status.LOADING, data, null); }
    public static <T> Resource<T> success(T data) { return new Resource<>(Status.SUCCESS, data, null); }
    public static <T> Resource<T> error(String msg, T data) { return new Resource<>(Status.ERROR, data, msg); }
    public static <T> Resource<T> empty(String msg) { return new Resource<>(Status.EMPTY, null, msg); }
}