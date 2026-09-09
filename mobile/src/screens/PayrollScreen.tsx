import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  RefreshControl,
  Modal,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { maidService, MaidSummary } from '../services/maids/maidService';
import {
  payrollService,
  PayrollRun,
  PayrollAdjustmentType,
  PayrollAdjustment,
} from '../services/payroll/payrollService';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { LoadingView, ErrorView, EmptyStateView, ToastBanner } from '../components/FeedbackStates';

export const PayrollScreen: React.FC = () => {
  const [maids, setMaids] = useState<MaidSummary[]>([]);
  const [selectedMaidId, setSelectedMaidId] = useState<string | null>(null);

  // Default month: Current month in YYYY-MM
  const [selectedMonth, setSelectedMonth] = useState<string>(
    new Date().toISOString().substring(0, 7)
  );

  const [payrollRun, setPayrollRun] = useState<PayrollRun | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Modal states
  const [showAdjustmentModal, setShowAdjustmentModal] = useState<boolean>(false);
  const [adjType, setAdjType] = useState<PayrollAdjustmentType>('BONUS');
  const [adjAmount, setAdjAmount] = useState<string>('');
  const [adjReason, setAdjReason] = useState<string>('');
  const [adjLoading, setAdjLoading] = useState<boolean>(false);

  const [showPaymentModal, setShowPaymentModal] = useState<boolean>(false);
  const [paymentMethod, setPaymentMethod] = useState<string>('UPI');
  const [paymentNote, setPaymentNote] = useState<string>('');
  const [actionLoading, setActionLoading] = useState<boolean>(false);

  // Fetch maids list on mount
  useEffect(() => {
    fetchInitialMaids();
  }, []);

  const fetchInitialMaids = async () => {
    try {
      const list = await maidService.listMaids(true);
      setMaids(list);
      if (list.length > 0) {
        setSelectedMaidId(list[0].id);
      } else {
        setLoading(false);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load workers');
      setLoading(false);
    }
  };

  const fetchPayroll = useCallback(async (isRefresh = false) => {
    if (!selectedMaidId) return;
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);

    try {
      const run = await payrollService.createOrGetDraftRun(selectedMaidId, selectedMonth);
      setPayrollRun(run);
    } catch (err: any) {
      setError(err.message || 'Failed to calculate payroll');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [selectedMaidId, selectedMonth]);

  useEffect(() => {
    if (selectedMaidId) {
      fetchPayroll();
    }
  }, [selectedMaidId, selectedMonth, fetchPayroll]);

  const handleAddAdjustment = async () => {
    if (!payrollRun) return;
    const amount = parseFloat(adjAmount);
    if (isNaN(amount) || amount <= 0) {
      setToast({ message: 'Enter a valid adjustment amount', type: 'error' });
      return;
    }

    setAdjLoading(true);
    try {
      await payrollService.addAdjustment(payrollRun.id, {
        type: adjType,
        amount,
        reason: adjReason.trim() || undefined,
        adjustmentDate: `${selectedMonth}-01`,
      });
      const refreshed = await payrollService.createOrGetDraftRun(selectedMaidId!, selectedMonth);
      setPayrollRun(refreshed);
      setShowAdjustmentModal(false);
      setAdjAmount('');
      setAdjReason('');
      setToast({ message: 'Adjustment added successfully', type: 'success' });
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to add adjustment', type: 'error' });
    } finally {
      setAdjLoading(false);
    }
  };

  const handleDeleteAdjustment = async (adjustmentId: string) => {
    if (!payrollRun) return;
    setActionLoading(true);
    try {
      await payrollService.deleteAdjustment(payrollRun.id, adjustmentId);
      const refreshed = await payrollService.createOrGetDraftRun(selectedMaidId!, selectedMonth);
      setPayrollRun(refreshed);
      setToast({ message: 'Adjustment removed', type: 'success' });
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to remove adjustment', type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleFinalize = async () => {
    if (!payrollRun) return;
    setActionLoading(true);
    try {
      const updated = await payrollService.finalizePayrollRun(payrollRun.id);
      setPayrollRun(updated);
      setToast({ message: 'Payroll finalized and locked!', type: 'success' });
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to finalize payroll', type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleMarkPaid = async () => {
    if (!payrollRun) return;
    setActionLoading(true);
    try {
      const updated = await payrollService.markPaid(payrollRun.id, {
        paymentMethod,
        paymentNote: paymentNote.trim() || undefined,
      });
      setPayrollRun(updated);
      setShowPaymentModal(false);
      setToast({ message: 'Payment recorded successfully', type: 'success' });
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to record payment', type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  if (maids.length === 0 && !loading) {
    return (
      <View style={styles.container}>
        <EmptyStateView
          title="No Workers Available"
          description="Add active workers before managing payroll."
          icon="💳"
        />
      </View>
    );
  }

  const breakdown = payrollRun?.calculation;
  const isFinalized = payrollRun?.status === 'FINALIZED' || payrollRun?.status === 'PAID';
  const isPaid = payrollRun?.status === 'PAID';

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={() => fetchPayroll(true)} />
      }
    >
      <ToastBanner
        message={toast?.message || null}
        type={toast?.type}
        onDismiss={() => setToast(null)}
      />

      <View style={styles.header}>
        <Text style={Typography.h1}>Payroll & Settlement</Text>
        <Text style={Typography.subtext}>Review breakdown, make adjustments & settle pay</Text>
      </View>

      {/* Month & Worker Selector */}
      <View style={styles.selectorsCard}>
        <View style={styles.selectorRow}>
          <Text style={[Typography.subtext, styles.selectorLabel]}>Month (YYYY-MM):</Text>
          <TextInput
            style={styles.monthInput}
            value={selectedMonth}
            onChangeText={setSelectedMonth}
            placeholder="2026-09"
            maxLength={7}
          />
        </View>

        <Text style={[Typography.subtext, styles.selectorLabel, { marginTop: Spacing.sm }]}>
          Worker:
        </Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.maidsPills}>
          {maids.map((m) => (
            <TouchableOpacity
              key={m.id}
              style={[
                styles.maidPill,
                selectedMaidId === m.id && styles.maidPillActive,
              ]}
              onPress={() => setSelectedMaidId(m.id)}
              activeOpacity={0.8}
            >
              <Text
                style={[
                  styles.maidPillText,
                  selectedMaidId === m.id && styles.maidPillTextActive,
                ]}
              >
                {m.name}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {loading && !refreshing ? (
        <LoadingView message="Calculating salary..." />
      ) : error && !payrollRun ? (
        <ErrorView message={error} onRetry={() => fetchPayroll()} />
      ) : (
        <>
          {/* Status Banner */}
          <View
            style={[
              styles.statusBanner,
              isPaid
                ? { backgroundColor: Colors.successLight }
                : isFinalized
                ? { backgroundColor: Colors.primaryLight }
                : { backgroundColor: Colors.warningLight },
            ]}
          >
            <Text
              style={[
                styles.statusBannerText,
                isPaid
                  ? { color: Colors.success }
                  : isFinalized
                  ? { color: Colors.primaryDark }
                  : { color: Colors.warning },
              ]}
            >
              STATUS: {payrollRun?.status} {isPaid ? '✓' : ''}
            </Text>
            {isPaid && payrollRun?.paymentMethod && (
              <Text style={Typography.caption}>Method: {payrollRun.paymentMethod}</Text>
            )}
          </View>

          {/* Salary Breakdown Card */}
          <View style={styles.card}>
            <Text style={[Typography.h3, styles.cardTitle]}>Financial Breakdown</Text>
            {breakdown && (
              <View style={styles.breakdownTable}>
                <View style={styles.row}>
                  <Text style={Typography.body}>Base Earnings ({breakdown.salaryMode}):</Text>
                  <Text style={Typography.bodyMedium}>₹{breakdown.baseEarnings.toLocaleString()}</Text>
                </View>
                <View style={styles.row}>
                  <Text style={[Typography.body, { color: Colors.danger }]}>Shortfall Deduction:</Text>
                  <Text style={[Typography.bodyMedium, { color: Colors.danger }]}>
                    -₹{breakdown.shortfallDeduction.toLocaleString()}
                  </Text>
                </View>
                {breakdown.overtimePay > 0 && (
                  <View style={styles.row}>
                    <Text style={[Typography.body, { color: Colors.success }]}>Overtime Pay:</Text>
                    <Text style={[Typography.bodyMedium, { color: Colors.success }]}>
                      +₹{breakdown.overtimePay.toLocaleString()}
                    </Text>
                  </View>
                )}
                <View style={styles.row}>
                  <Text style={Typography.body}>Additions (Bonus/Other):</Text>
                  <Text style={[Typography.bodyMedium, { color: Colors.success }]}>
                    +₹{breakdown.additions.toLocaleString()}
                  </Text>
                </View>
                <View style={styles.row}>
                  <Text style={Typography.body}>Deductions & Advances:</Text>
                  <Text style={[Typography.bodyMedium, { color: Colors.danger }]}>
                    -₹{breakdown.deductions.toLocaleString()}
                  </Text>
                </View>
                <View style={[styles.row, styles.totalRow]}>
                  <Text style={[Typography.h2, { color: Colors.primary }]}>Final Payable:</Text>
                  <Text style={[Typography.h2, { color: Colors.primary }]}>
                    ₹{breakdown.finalPayable.toLocaleString()}
                  </Text>
                </View>
              </View>
            )}
          </View>

          {/* Adjustments Section */}
          <View style={styles.card}>
            <View style={styles.cardHeaderRow}>
              <Text style={[Typography.h3, styles.cardTitle]}>Adjustments & Advances</Text>
              {!isFinalized && (
                <TouchableOpacity
                  style={styles.addAdjButton}
                  onPress={() => setShowAdjustmentModal(true)}
                  activeOpacity={0.8}
                >
                  <Text style={styles.addAdjButtonText}>+ Add</Text>
                </TouchableOpacity>
              )}
            </View>

            {(!payrollRun?.adjustments || payrollRun.adjustments.length === 0) ? (
              <Text style={[Typography.subtext, { marginVertical: Spacing.sm }]}>
                No bonus, deduction, or advance adjustments added.
              </Text>
            ) : (
              payrollRun.adjustments.map((adj: PayrollAdjustment) => (
                <View key={adj.id} style={styles.adjItem}>
                  <View style={{ flex: 1 }}>
                    <Text style={[Typography.bodyMedium, { fontWeight: '600' }]}>
                      {adj.type}: ₹{adj.amount.toLocaleString()}
                    </Text>
                    {adj.reason ? (
                      <Text style={Typography.caption}>{adj.reason}</Text>
                    ) : null}
                  </View>
                  {!isFinalized && (
                    <TouchableOpacity
                      onPress={() => handleDeleteAdjustment(adj.id)}
                      disabled={actionLoading}
                      activeOpacity={0.7}
                    >
                      <Text style={styles.deleteAdjButton}>✕</Text>
                    </TouchableOpacity>
                  )}
                </View>
              ))
            )}
          </View>

          {/* Action Buttons */}
          <View style={styles.actionsContainer}>
            {!isFinalized && (
              <TouchableOpacity
                style={[styles.primaryButton, actionLoading && styles.disabledButton]}
                onPress={handleFinalize}
                disabled={actionLoading}
                activeOpacity={0.8}
              >
                {actionLoading ? (
                  <ActivityIndicator color="#FFFFFF" />
                ) : (
                  <Text style={Typography.button}>🔒 Finalize & Lock Payroll</Text>
                )}
              </TouchableOpacity>
            )}

            {isFinalized && !isPaid && (
              <TouchableOpacity
                style={[styles.successButton, actionLoading && styles.disabledButton]}
                onPress={() => setShowPaymentModal(true)}
                disabled={actionLoading}
                activeOpacity={0.8}
              >
                <Text style={Typography.button}>✓ Mark as Paid</Text>
              </TouchableOpacity>
            )}
          </View>
        </>
      )}

      {/* Adjustment Modal */}
      <Modal visible={showAdjustmentModal} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={[Typography.h3, { marginBottom: Spacing.md }]}>Add Adjustment</Text>

            <View style={styles.adjTypeRow}>
              {(['BONUS', 'DEDUCTION', 'ADVANCE'] as PayrollAdjustmentType[]).map((type) => (
                <TouchableOpacity
                  key={type}
                  style={[
                    styles.adjTypePill,
                    adjType === type && styles.adjTypePillActive,
                  ]}
                  onPress={() => setAdjType(type)}
                >
                  <Text
                    style={[
                      styles.adjTypePillText,
                      adjType === type && styles.adjTypePillTextActive,
                    ]}
                  >
                    {type}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[Typography.subtext, styles.label]}>Amount (₹)</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="e.g. 500"
              value={adjAmount}
              onChangeText={setAdjAmount}
              keyboardType="numeric"
            />

            <Text style={[Typography.subtext, styles.label]}>Reason / Note</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="e.g. Festival bonus / Advance payback"
              value={adjReason}
              onChangeText={setAdjReason}
            />

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={styles.modalCancel}
                onPress={() => setShowAdjustmentModal(false)}
              >
                <Text style={Typography.subtext}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.modalConfirm}
                onPress={handleAddAdjustment}
                disabled={adjLoading}
              >
                {adjLoading ? (
                  <ActivityIndicator color="#FFFFFF" size="small" />
                ) : (
                  <Text style={Typography.button}>Save</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Payment Confirmation Modal */}
      <Modal visible={showPaymentModal} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={[Typography.h3, { marginBottom: Spacing.md }]}>Confirm Payment</Text>

            <Text style={[Typography.subtext, styles.label]}>Payment Method</Text>
            <View style={styles.adjTypeRow}>
              {['UPI', 'CASH', 'BANK_TRANSFER'].map((method) => (
                <TouchableOpacity
                  key={method}
                  style={[
                    styles.adjTypePill,
                    paymentMethod === method && styles.adjTypePillActive,
                  ]}
                  onPress={() => setPaymentMethod(method)}
                >
                  <Text
                    style={[
                      styles.adjTypePillText,
                      paymentMethod === method && styles.adjTypePillTextActive,
                    ]}
                  >
                    {method}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[Typography.subtext, styles.label]}>Payment Note (Optional)</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Transaction ref / paid in hand"
              value={paymentNote}
              onChangeText={setPaymentNote}
            />

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={styles.modalCancel}
                onPress={() => setShowPaymentModal(false)}
              >
                <Text style={Typography.subtext}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.modalConfirm}
                onPress={handleMarkPaid}
                disabled={actionLoading}
              >
                {actionLoading ? (
                  <ActivityIndicator color="#FFFFFF" size="small" />
                ) : (
                  <Text style={Typography.button}>Confirm Paid</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xxxl,
  },
  header: {
    marginBottom: Spacing.lg,
  },
  selectorsCard: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    borderWidth: 1,
    borderColor: Colors.border,
    marginBottom: Spacing.lg,
  },
  selectorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  selectorLabel: {
    fontWeight: '600',
    color: Colors.text,
  },
  monthInput: {
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.md,
    fontSize: 14,
    fontWeight: '600',
    color: Colors.text,
    width: 110,
    textAlign: 'center',
  },
  maidsPills: {
    marginTop: Spacing.xs,
    flexDirection: 'row',
  },
  maidPill: {
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.full,
    borderWidth: 1,
    borderColor: Colors.border,
    marginRight: Spacing.sm,
    backgroundColor: Colors.surface,
  },
  maidPillActive: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  maidPillText: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  maidPillTextActive: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
  statusBanner: {
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.md,
    marginBottom: Spacing.lg,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statusBannerText: {
    fontSize: 13,
    fontWeight: '700',
  },
  card: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    borderWidth: 1,
    borderColor: Colors.border,
    marginBottom: Spacing.lg,
  },
  cardTitle: {
    marginBottom: Spacing.sm,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.sm,
  },
  breakdownTable: {
    gap: Spacing.sm,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  totalRow: {
    marginTop: Spacing.sm,
    paddingTop: Spacing.sm,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
  },
  addAdjButton: {
    backgroundColor: Colors.primaryLight,
    paddingVertical: 4,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.sm,
  },
  addAdjButtonText: {
    color: Colors.primaryDark,
    fontSize: 12,
    fontWeight: '700',
  },
  adjItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
    borderBottomWidth: 1,
    borderBottomColor: Colors.borderSubtle,
  },
  deleteAdjButton: {
    color: Colors.danger,
    fontSize: 16,
    padding: Spacing.xs,
  },
  actionsContainer: {
    gap: Spacing.md,
  },
  primaryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
  },
  successButton: {
    backgroundColor: Colors.success,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
  },
  disabledButton: {
    backgroundColor: Colors.disabled,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    padding: Spacing.xl,
  },
  modalContent: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.xl,
  },
  label: {
    marginBottom: Spacing.xs,
    fontWeight: '600',
    color: Colors.text,
  },
  modalInput: {
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.md,
    marginBottom: Spacing.md,
    fontSize: 15,
  },
  adjTypeRow: {
    flexDirection: 'row',
    gap: Spacing.sm,
    marginBottom: Spacing.md,
  },
  adjTypePill: {
    flex: 1,
    paddingVertical: Spacing.xs,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
  },
  adjTypePillActive: {
    backgroundColor: Colors.primaryLight,
    borderColor: Colors.primary,
  },
  adjTypePillText: {
    fontSize: 11,
    color: Colors.textSecondary,
    fontWeight: '600',
  },
  adjTypePillTextActive: {
    color: Colors.primaryDark,
    fontWeight: '700',
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    alignItems: 'center',
    gap: Spacing.md,
    marginTop: Spacing.md,
  },
  modalCancel: {
    padding: Spacing.sm,
  },
  modalConfirm: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    borderRadius: BorderRadius.md,
  },
});
