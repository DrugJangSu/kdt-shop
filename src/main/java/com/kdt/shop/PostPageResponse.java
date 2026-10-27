package com.kdt.shop;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PostPageResponse(
        List<PostItem> content,
        int page,
        int size,
        long totalElements) implements Serializable {
}
