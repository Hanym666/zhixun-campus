package com.zhixun.item.controller;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.common.api.PageResult;
import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.item.dto.CreateItemPostRequest;
import com.zhixun.item.dto.ItemPostResponse;
import com.zhixun.item.dto.ItemMatchResponse;
import com.zhixun.item.dto.UpdateItemPostRequest;
import com.zhixun.item.security.AuthenticatedUser;
import com.zhixun.item.service.ItemPostService;
import com.zhixun.item.service.ItemMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemPostController {

    private final ItemPostService itemPostService;
    private final ItemMatchingService itemMatchingService;

    @GetMapping
    public ApiResponse<PageResult<ItemPostResponse>> list(
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(itemPostService.findPublic(
                itemType, categoryId, keyword, page, size));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResult<ItemPostResponse>> mine(
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return ApiResponse.success(itemPostService.findMine(
                currentUser(authentication).userId(), status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItemPostResponse> detail(@PathVariable Long id,
                                                Authentication authentication) {
        return ApiResponse.success(itemPostService.detail(id, optionalUserId(authentication)));
    }

    @PostMapping
    public ApiResponse<ItemPostResponse> create(
            @Valid @RequestBody CreateItemPostRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success("发布成功", itemPostService.create(
                currentUser(authentication).userId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ItemPostResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemPostRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(itemPostService.update(
                id, currentUser(authentication).userId(), request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<ItemPostResponse> publish(@PathVariable Long id,
                                                 Authentication authentication) {
        return ApiResponse.success("发布成功", itemPostService.publish(
                id, currentUser(authentication).userId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        itemPostService.delete(id, currentUser(authentication).userId());
        return ApiResponse.success();
    }

    @GetMapping("/{id}/matches")
    public ApiResponse<java.util.List<ItemMatchResponse>> matches(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        return ApiResponse.success(itemMatchingService.findMatches(
                id, currentUser(authentication).userId(), limit));
    }

    @PostMapping("/{id}/reindex")
    public ApiResponse<Void> reindex(@PathVariable Long id, Authentication authentication) {
        itemMatchingService.requestReindex(id, currentUser(authentication).userId());
        return ApiResponse.success();
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private Long optionalUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId();
        }
        return null;
    }
}
