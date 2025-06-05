package com.quitjourney.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * 统一API响应格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val meta: ResponseMeta = ResponseMeta()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> success(): ApiResponse<T> {
            return ApiResponse(success = true)
        }
        
        fun <T> error(error: ErrorDetail): ApiResponse<T> {
            return ApiResponse(success = false, error = error)
        }
        
        fun <T> error(code: String, message: String, details: List<FieldError>? = null): ApiResponse<T> {
            return ApiResponse(success = false, error = ErrorDetail(code, message, details))
        }
    }
}

/**
 * 分页响应格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PagedResponse<T>(
    val success: Boolean,
    val data: List<T>,
    val pagination: PaginationInfo,
    val meta: ResponseMeta = ResponseMeta()
) {
    companion object {
        fun <T> success(data: List<T>, pagination: PaginationInfo): PagedResponse<T> {
            return PagedResponse(success = true, data = data, pagination = pagination)
        }
    }
}

/**
 * 错误详情
 */
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<FieldError>? = null
)

/**
 * 字段错误
 */
data class FieldError(
    val field: String,
    val code: String,
    val message: String
)

/**
 * 响应元数据
 */
data class ResponseMeta(
    val timestamp: Instant = Instant.now(),
    val requestId: String? = null,
    val version: String = "1.0"
)

/**
 * 分页信息
 */
data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
) {
    companion object {
        fun of(page: Int, limit: Int, total: Long): PaginationInfo {
            val totalPages = if (total == 0L) 0 else ((total - 1) / limit + 1).toInt()
            return PaginationInfo(
                page = page,
                limit = limit,
                total = total,
                totalPages = totalPages,
                hasNext = page < totalPages,
                hasPrev = page > 1
            )
        }
    }
}
