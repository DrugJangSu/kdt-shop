package com.kdt.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/outside/posts")
public class OutsidePostController {

    private final OutsidePostClient outsidePostClient;

    @Autowired
    public OutsidePostController(OutsidePostClient outsidePostClient) {
        this.outsidePostClient = outsidePostClient;
    }


    @GetMapping("/{id}")
    public OutsideTitle one(@PathVariable("id") int id) {
        return this.outsidePostClient.title(id);
    }

}
