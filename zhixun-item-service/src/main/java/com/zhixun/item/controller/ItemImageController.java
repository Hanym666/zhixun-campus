package com.zhixun.item.controller;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.item.dto.ItemImageResponse;
import com.zhixun.item.security.AuthenticatedUser;
import com.zhixun.item.service.ItemImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items/{postId}/images")
@RequiredArgsConstructor
public class ItemImageController {

    private final ItemImageService itemImageService;

    @PostMapping
    public ApiResponse<ItemImageResponse> upload(@PathVariable Long postId,
                                                 @RequestParam("file") MultipartFile file,
                                                 Authentication authentication) {
        return ApiResponse.success("图片上传成功", itemImageService.upload(
                postId, userId(authentication), file));
    }

    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> delete(@PathVariable Long postId, @PathVariable Long imageId,
                                    Authentication authentication) {
        itemImageService.delete(postId, imageId, userId(authentication));
        return ApiResponse.success();
    }

    private Long userId(Authentication authentication) {
        return ((AuthenticatedUser) authentication.getPrincipal()).userId();
    }
}
