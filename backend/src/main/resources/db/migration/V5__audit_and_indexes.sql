CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES owners(id) ON DELETE CASCADE,
    actor_owner_id UUID NOT NULL REFERENCES owners(id) ON DELETE CASCADE,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(40) NOT NULL,
    before_data JSONB,
    after_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index hardening
CREATE INDEX idx_maids_owner_active ON maids(owner_id, is_active);
CREATE INDEX idx_maids_owner_name ON maids(owner_id, name);
CREATE INDEX idx_employment_configs_maid_dates ON employment_configs(maid_id, effective_from, effective_to);
CREATE INDEX idx_attendance_maid_date ON attendance_sessions(maid_id, business_date);
CREATE INDEX idx_leave_records_maid_date ON leave_records(maid_id, leave_date);
CREATE INDEX idx_holidays_owner_date ON holidays(owner_id, holiday_date);
CREATE INDEX idx_payroll_runs_owner_month ON payroll_runs(owner_id, payroll_month);
CREATE INDEX idx_payroll_adjustments_run ON payroll_adjustments(payroll_run_id);
CREATE INDEX idx_audit_events_owner_created ON audit_events(owner_id, created_at DESC);
CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
