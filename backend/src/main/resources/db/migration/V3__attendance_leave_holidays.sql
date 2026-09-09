CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY,
    maid_id UUID NOT NULL REFERENCES maids(id) ON DELETE RESTRICT,
    business_date DATE NOT NULL,
    entry_at TIMESTAMP WITH TIME ZONE,
    exit_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL,
    note TEXT,
    created_by UUID REFERENCES owners(id),
    updated_by UUID REFERENCES owners(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_exit_after_entry CHECK (entry_at IS NULL OR exit_at IS NULL OR exit_at >= entry_at)
);

CREATE TABLE leave_records (
    id UUID PRIMARY KEY,
    maid_id UUID NOT NULL REFERENCES maids(id) ON DELETE RESTRICT,
    leave_date DATE NOT NULL,
    leave_type VARCHAR(10) NOT NULL CHECK (leave_type IN ('PAID', 'UNPAID')),
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_maid_leave_date UNIQUE (maid_id, leave_date)
);

CREATE TABLE holidays (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES owners(id) ON DELETE CASCADE,
    holiday_date DATE NOT NULL,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_owner_holiday_date UNIQUE (owner_id, holiday_date)
);
