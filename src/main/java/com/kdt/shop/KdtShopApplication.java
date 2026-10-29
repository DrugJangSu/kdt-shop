package com.kdt.shop;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "게시판 API",
                version ="v1.0",
                description = "게시판 API 문서입니다."
        )
)
public class KdtShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(KdtShopApplication.class, args);
    }

}
