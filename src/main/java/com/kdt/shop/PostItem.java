package com.kdt.shop;

import java.io.Serializable;

public record PostItem(Long id, String title, String loginId) implements Serializable {

}
