package com.zhixun.common;

import com.zhixun.common.api.ApiResponse;
import com.zhixun.common.api.CommonResultCode;
import com.zhixun.common.api.PageResult;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommonFoundationTest {

    @Test
    void shouldCreateSuccessfulResponse() {
        ApiResponse<String> response = ApiResponse.success("created");

        assertEquals(0, response.code());
        assertEquals("成功", response.message());
        assertEquals("created", response.data());
    }

    @Test
    void shouldCalculatePageCount() {
        PageResult<String> page = PageResult.of(List.of("a", "b"), 21, 2, 10);

        assertEquals(3, page.pages());
        assertEquals(2, page.records().size());
    }

    @Test
    void shouldRejectInvalidPageNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PageResult.of(List.of(), 0, 0, 10)
        );
    }

    @Test
    void shouldConvertBusinessExceptionToHttpResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BusinessException exception = new BusinessException(
                CommonResultCode.NOT_FOUND,
                "帖子不存在"
        );

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(40400, response.getBody().code());
        assertEquals("帖子不存在", response.getBody().message());
        assertNull(response.getBody().data());
    }
}
