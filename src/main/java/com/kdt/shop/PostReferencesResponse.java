package com.kdt.shop;

import java.util.List;

public record PostReferencesResponse(PostResponse post, List<OutsideReferenceItem> references) {
}
