-- 焕新之旅 - 戒烟辅助应用数据库初始化脚本
-- 版本: V1
-- 创建时间: 2025-01-27
-- 描述: 创建用户、用户资料、每日打卡、吸烟记录、成就系统等核心表

-- 启用UUID扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

-- 用户资料表
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 基本信息
    name VARCHAR(100),
    avatar_url VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'zh-CN',
    
    -- 戒烟相关信息
    quit_date TIMESTAMP WITH TIME ZONE,
    quit_reason TEXT,
    cigarettes_per_day INTEGER,
    cigarette_price DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'CNY',
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- 约束
    UNIQUE(user_id)
);

-- 每日打卡表
CREATE TABLE daily_checkins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 打卡信息
    checkin_date DATE NOT NULL,
    is_checked_in BOOLEAN NOT NULL DEFAULT TRUE,
    checkin_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- 约束
    UNIQUE(user_id, checkin_date)
);

-- 吸烟记录表
CREATE TABLE smoking_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- 吸烟信息
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    cigarettes_smoked INTEGER NOT NULL CHECK (cigarettes_smoked > 0),
    trigger_tags TEXT[],
    notes TEXT,
    location JSONB,
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 成就定义表
CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- 成就信息
    key VARCHAR(100) UNIQUE NOT NULL,
    name_i18n JSONB NOT NULL,
    description_i18n JSONB NOT NULL,
    icon_name VARCHAR(100),
    category VARCHAR(50),
    
    -- 解锁条件
    unlock_condition JSONB NOT NULL,
    points INTEGER DEFAULT 0,
    
    -- 元数据
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 用户成就表
CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievements(id) ON DELETE CASCADE,
    
    -- 解锁信息
    unlocked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    progress JSONB,
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- 约束
    UNIQUE(user_id, achievement_id)
);

-- 同步状态表
CREATE TABLE sync_status (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    
    -- 同步信息
    last_sync_at TIMESTAMP WITH TIME ZONE,
    sync_version BIGINT DEFAULT 0,
    client_version VARCHAR(20),
    
    -- 冲突解决
    conflict_resolution_strategy VARCHAR(20) DEFAULT 'SERVER_WINS',
    
    -- 元数据
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- 约束
    UNIQUE(user_id, device_id)
);

-- 数据变更日志表
CREATE TABLE data_change_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 变更信息
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_data JSONB,
    new_data JSONB,

    -- 元数据
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    device_id VARCHAR(255),
    sync_version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_quit_date ON user_profiles(quit_date);

CREATE INDEX idx_daily_checkins_user_date ON daily_checkins(user_id, checkin_date);
CREATE INDEX idx_daily_checkins_date ON daily_checkins(checkin_date);

CREATE INDEX idx_smoking_records_user_id ON smoking_records(user_id);
CREATE INDEX idx_smoking_records_timestamp ON smoking_records(timestamp);
CREATE INDEX idx_smoking_records_user_timestamp ON smoking_records(user_id, timestamp);
CREATE INDEX idx_smoking_records_trigger_tags ON smoking_records USING GIN (trigger_tags);

CREATE INDEX idx_achievements_key ON achievements("key");
CREATE INDEX idx_achievements_category ON achievements(category);

CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked_at ON user_achievements(unlocked_at);

CREATE INDEX idx_sync_status_user_id ON sync_status(user_id);
CREATE INDEX idx_sync_status_last_sync ON sync_status(last_sync_at);

CREATE INDEX idx_data_change_logs_user_id ON data_change_logs(user_id);
CREATE INDEX idx_data_change_logs_changed_at ON data_change_logs(changed_at);
CREATE INDEX idx_data_change_logs_sync_version ON data_change_logs(sync_version);
