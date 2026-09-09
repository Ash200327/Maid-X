CREATE TABLE payroll_runs (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES owners(id) ON DELETE RESTRICT,
    maid_id UUID NOT NULL REFERENCES maids(id) ON DELETE RESTRICT,
    payroll_month DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'FINALIZED', 'PAID')),
    finalized_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    payment_method VARCHAR(30),
    payment_note TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_maid_payroll_month UNIQUE (maid_id, payroll_month)
);

CREATE TABLE payroll_adjustments (
    id UUID PRIMARY KEY,
    payroll_run_id UUID NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('ADVANCE', 'BONUS', 'DEDUCTION', 'OTHER_ADD', 'OTHER_DEDUCTION')),
    amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    adjustment_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE payroll_calculation_snapshots (
    id UUID PRIMARY KEY,
    payroll_run_id UUID NOT NULL UNIQUE REFERENCES payroll_runs(id) ON DELETE CASCADE,
    input_snapshot JSONB NOT NULL,
    result_snapshot JSONB NOT NULL,
    engine_version VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
