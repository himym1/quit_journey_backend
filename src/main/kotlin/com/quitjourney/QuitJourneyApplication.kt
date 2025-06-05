package com.quitjourney

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * 焕新之旅 - 戒烟辅助应用后端服务
 * 
 * 主要功能：
 * - 用户认证和授权
 * - 戒烟数据云端存储和同步
 * - 多设备数据一致性保证
 * - RESTful API 服务
 * 
 * 技术栈：
 * - Kotlin + Spring Boot 3.x
 * - PostgreSQL + Redis
 * - Spring Security + JWT
 * - Docker 容器化部署
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
class QuitJourneyApplication

fun main(args: Array<String>) {
    runApplication<QuitJourneyApplication>(*args)
}
