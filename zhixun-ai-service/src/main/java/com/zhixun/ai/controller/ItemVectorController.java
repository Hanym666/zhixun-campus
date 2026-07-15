package com.zhixun.ai.controller;

import com.zhixun.ai.dto.ItemFeatures;
import com.zhixun.ai.dto.ItemVectorIndexRequest;
import com.zhixun.ai.dto.ItemVectorSearchRequest;
import com.zhixun.ai.dto.VectorMatchResponse;
import com.zhixun.ai.service.ItemVectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/items")
@RequiredArgsConstructor
public class ItemVectorController {

    private final ItemVectorService itemVectorService;

    @PostMapping("/index")
    public ItemFeatures index(@RequestBody ItemVectorIndexRequest request) {
        return itemVectorService.index(request);
    }

    @PostMapping("/search")
    public List<VectorMatchResponse> search(@RequestBody ItemVectorSearchRequest request) {
        return itemVectorService.search(request);
    }

    @DeleteMapping("/{postId}")
    public void delete(@PathVariable Long postId) {
        itemVectorService.delete(postId);
    }
}
