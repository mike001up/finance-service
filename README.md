## 技术栈

| 层次       | 技术                                      |
| ---------- | ----------------------------------------- |
| 后端框架   | Spring Boot 2.7+                          |
| 数据库     | MySQL 8.0 (每个租户独立 database)         |
| 持久层     | MyBatis-Plus / JPA (根据团队习惯)         |
| 连接池     | HikariCP                                  |
| 缓存       | Caffeine (本地缓存数据源、科目树)         |
| 消息队列   | RocketMQ (可选，用于延迟报表生成)         |
| 服务发现   | Nacos / Consul (可选)                     |
| 容器化     | Docker + Kubernetes                       |

---

## 快速开始

### 1. 环境要求

- JDK 21+
- MySQL 8.0+
- RocketMQ (如需延迟报表功能)
- 系统管理微服务 (必须，提供租户数据源、认证、权限)