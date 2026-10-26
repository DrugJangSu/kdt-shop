package com.kdt.shop;

import java.io.Serializable;
import java.util.List;

public record PostPageResponse(
        List<PostItem> content,
        int page,
        int size,
        long totalElements) implements Serializable {
}
