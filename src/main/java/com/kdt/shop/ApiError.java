package com.kdt.shop;

public record ApiError(String code, String message) {
    public static record OutsidePost(int userId, int id, String title, String body) {
    }
}
