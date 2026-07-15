package com.zhixun.item.controller;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.item.dto.ClaimMessageResponse;
import com.zhixun.item.dto.SendClaimMessageRequest;
import com.zhixun.item.security.AuthenticatedUser;
import com.zhixun.item.service.ClaimMessageService;
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

import java.util.List;

@RestController
@RequestMapping("/api/claims/{claimId}/messages")
@RequiredArgsConstructor
public class ClaimMessageController {

    private final ClaimMessageService claimMessageService;

    @PostMapping
    public ApiResponse<ClaimMessageResponse> send(
            @PathVariable Long claimId,
            @Valid @RequestBody SendClaimMessageRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(claimMessageService.send(
                claimId, userId(authentication), request));
    }

    @GetMapping
    public ApiResponse<List<ClaimMessageResponse>> list(
            @PathVariable Long claimId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication
    ) {
        return ApiResponse.success(claimMessageService.list(
                claimId, userId(authentication), beforeId, limit));
    }

    private Long userId(Authentication authentication) {
        return ((AuthenticatedUser) authentication.getPrincipal()).userId();
    }
}
