package com.kdt.shop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OutsidePostClient {
    private final RestClient restClient;

    public OutsidePostClient(RestClient.Builder builder, @Value("${shop.outside.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public OutsideTitle title(int id) {
        OutsidePost found = this.read(id);
        return new OutsideTitle(found.title());
    }

    public OutsidePost read(int id) {
        OutsidePost found = this.restClient.get().uri("/posts/{id}", id) // https://jsonplaceholder.typicode.com/posts/
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OutsidePost.class);
        if (found == null || found.title() == null) {
            throw new IllegalStateException("outside response missing");
        }
        return found;
    }
}