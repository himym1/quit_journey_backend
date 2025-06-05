package com.quitjourney.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI (Swagger) 配置
 * 
 * 配置API文档的基本信息、安全认证等
 */
@Configuration
class OpenApiConfig {
    
    @Value("\${server.servlet.context-path:/api/v1}")
    private lateinit var contextPath: String
    
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .servers(listOf(
                Server().url("http://localhost:8080$contextPath").description("开发环境"),
                Server().url("https://api.quitjourney.com$contextPath").description("生产环境")
            ))
            .components(
                Components()
                    .addSecuritySchemes("bearerAuth", securityScheme())
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
    
    private fun apiInfo(): Info {
        return Info()
            .title("焕新之旅 - 戒烟辅助应用 API")
            .description("""
                ## 焕新之旅后端API文档
                
                提供用户认证、数据同步、戒烟记录管理等功能的RESTful API服务。
                
                ### 主要功能模块：
                - **用户认证**: 注册、登录、令牌管理
                - **用户管理**: 资料管理、设置配置
                - **每日打卡**: 打卡记录、统计分析
                - **吸烟记录**: 记录管理、趋势分析
                - **成就系统**: 成就解锁、进度追踪
                - **数据同步**: 多设备数据同步
                
                ### 认证方式：
                使用JWT Bearer Token进行API认证。在请求头中添加：
                ```
                Authorization: Bearer <your-access-token>
                ```
                
                ### 响应格式：
                所有API响应都遵循统一的格式：
                ```json
                {
                  "success": true,
                  "data": { ... },
                  "meta": {
                    "timestamp": "2025-01-27T10:00:00Z",
                    "requestId": "req_123456789",
                    "version": "1.0"
                  }
                }
                ```
                
                ### 错误处理：
                错误响应包含详细的错误信息：
                ```json
                {
                  "success": false,
                  "error": {
                    "code": "ERROR_CODE",
                    "message": "错误描述",
                    "details": [...]
                  }
                }
                ```
            """.trimIndent())
            .version("1.0.0")
            .contact(
                Contact()
                    .name("Quit Journey Team")
                    .email("support@quitjourney.com")
                    .url("https://quitjourney.com")
            )
            .license(
                License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
            )
    }
    
    private fun securityScheme(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT认证令牌")
    }
}
