CREATE TABLE maids (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES owners(id) ON DELETE RESTRICT,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    joining_date DATE NOT NULL,
    leaving_date DATE,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_maid_dates CHECK (leaving_date IS NULL OR leaving_date >= joining_date)
);

CREATE TABLE employment_configs (
    id UUID PRIMARY KEY,
    maid_id UUID NOT NULL REFERENCES maids(id) ON DELETE RESTRICT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    salary_mode VARCHAR(20) NOT NULL CHECK (salary_mode IN ('MONTHLY', 'DAILY', 'HOURLY')),
    salary_amount NUMERIC(12,2) NOT NULL CHECK (salary_amount > 0),
    expected_minutes_per_day INTEGER NOT NULL CHECK (expected_minutes_per_day > 0),
    shortfall_threshold_minutes INTEGER NOT NULL CHECK (shortfall_threshold_minutes >= 0),
    overtime_enabled BOOLEAN NOT NULL DEFAULT false,
    overtime_multiplier NUMERIC(6,3) NOT NULL DEFAULT 1.0 CHECK (overtime_multiplier >= 1.0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_config_effective_dates CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT chk_shortfall_threshold CHECK (shortfall_threshold_minutes <= expected_minutes_per_day)
);

CREATE TABLE maid_working_days (
    id UUID PRIMARY KEY,
    employment_config_id UUID NOT NULL REFERENCES employment_configs(id) ON DELETE CASCADE,
    weekday SMALLINT NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    CONSTRAINT uq_config_weekday UNIQUE (employment_config_id, weekday)
);
