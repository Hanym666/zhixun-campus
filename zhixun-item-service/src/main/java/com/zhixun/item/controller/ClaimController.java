package com.zhixun.item.controller;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.common.api.PageResult;
import com.zhixun.item.dto.ClaimResponse;
import com.zhixun.item.dto.CreateClaimRequest;
import com.zhixun.item.dto.ReviewClaimRequest;
import com.zhixun.item.security.AuthenticatedUser;
import com.zhixun.item.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ApiResponse<ClaimResponse> create(@Valid @RequestBody CreateClaimRequest request,
                                             Authentication authentication) {
        return ApiResponse.success("认领申请已提交",
                claimService.create(userId(authentication), request));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResult<ClaimResponse>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return ApiResponse.success(claimService.findMine(userId(authentication), page, size));
    }

    @GetMapping("/received")
    public ApiResponse<PageResult<ClaimResponse>> received(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return ApiResponse.success(claimService.findReceived(userId(authentication), page, size));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<ClaimResponse> review(@PathVariable Long id,
                                             @Valid @RequestBody ReviewClaimRequest request,
                                             Authentication authentication) {
        return ApiResponse.success(claimService.review(id, userId(authentication), request));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<ClaimResponse> complete(@PathVariable Long id,
                                               Authentication authentication) {
        return ApiResponse.success("交接已完成",
                claimService.complete(id, userId(authentication)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ClaimResponse> cancel(@PathVariable Long id,
                                             Authentication authentication) {
        return ApiResponse.success(claimService.cancel(id, userId(authentication)));
    }

    private Long userId(Authentication authentication) {
        return ((AuthenticatedUser) authentication.getPrincipal()).userId();
    }
}
