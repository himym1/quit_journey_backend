-- 焕新之旅数据库初始化脚本
-- 用于Docker容器启动时的数据库初始化

-- 创建数据库（如果不存在）
-- 注意：在Docker环境中，数据库已通过环境变量创建

-- 设置时区
SET timezone = 'UTC';

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 输出初始化信息
DO $$
BEGIN
    RAISE NOTICE '焕新之旅数据库初始化完成';
    RAISE NOTICE '数据库版本: %', version();
    RAISE NOTICE '当前时间: %', now();
END $$;
