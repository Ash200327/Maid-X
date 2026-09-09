import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
  ActivityIndicator,
} from 'react-native';
import {
  attendanceService,
  DashboardResponse,
  DashboardMaidEntry,
} from '../services/attendance/attendanceService';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { LoadingView, ErrorView, EmptyStateView, ToastBanner } from '../components/FeedbackStates';

interface DashboardScreenProps {
  onNavigateToPayroll: () => void;
  onNavigateToMaidDetail: (maidId: string) => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({
  onNavigateToPayroll,
  onNavigateToMaidDetail,
}) => {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const fetchDashboard = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);

    try {
      const res = await attendanceService.getDashboard();
      setData(res);
    } catch (err: any) {
      setError(err.message || 'Failed to load dashboard.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  const handleEntry = async (entry: DashboardMaidEntry) => {
    setActionLoadingId(entry.maidId);
    try {
      await attendanceService.recordEntry(entry.maidId);
      setToast({ message: `Entry recorded for ${entry.maidName}`, type: 'success' });
      await fetchDashboard();
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to record entry', type: 'error' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleExit = async (entry: DashboardMaidEntry) => {
    if (!entry.activeSessionId) return;
    setActionLoadingId(entry.maidId);
    try {
      await attendanceService.recordExit(entry.maidId, entry.activeSessionId);
      setToast({ message: `Exit recorded for ${entry.maidName}`, type: 'success' });
      await fetchDashboard();
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to record exit', type: 'error' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const formatMinutes = (minutes: number) => {
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hrs === 0) return `${mins}m`;
    return `${hrs}h ${mins}m`;
  };

  const getStateBadge = (state: DashboardMaidEntry['state']) => {
    switch (state) {
      case 'WORKING':
        return { label: 'Working', bg: Colors.primaryLight, text: Colors.primaryDark };
      case 'COMPLETED':
        return { label: 'Completed', bg: Colors.successLight, text: Colors.success };
      case 'INCOMPLETE':
        return { label: 'Incomplete', bg: Colors.dangerLight, text: Colors.danger };
      case 'ON_LEAVE':
        return { label: 'On Leave', bg: Colors.warningLight, text: Colors.warning };
      case 'ABSENT':
        return { label: 'Absent', bg: Colors.border, text: Colors.textSecondary };
      case 'NOT_STARTED':
      default:
        return { label: 'Not Started', bg: Colors.borderSubtle, text: Colors.textSecondary };
    }
  };

  if (loading && !data) {
    return <LoadingView message="Loading dashboard..." />;
  }

  if (error && !data) {
    return <ErrorView message={error} onRetry={() => fetchDashboard()} />;
  }

  const todayDateStr = data?.date
    ? new Date(data.date).toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
      })
    : 'Today';

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={() => fetchDashboard(true)} />
      }
    >
      <ToastBanner
        message={toast?.message || null}
        type={toast?.type}
        onDismiss={() => setToast(null)}
      />

      {/* Header Summary */}
      <View style={styles.header}>
        <View>
          <Text style={[Typography.subtext, styles.dateSubtitle]}>{todayDateStr}</Text>
          <Text style={Typography.h1}>Today's Attendance</Text>
        </View>
        <View style={styles.maidCountBadge}>
          <Text style={styles.maidCountText}>
            {data?.activeMaidCount || 0} Active {data?.activeMaidCount === 1 ? 'Worker' : 'Workers'}
          </Text>
        </View>
      </View>

      {/* Workers List / Quick Actions */}
      <View style={styles.section}>
        <Text style={[Typography.h3, styles.sectionTitle]}>Workers Status</Text>

        {(!data?.entries || data.entries.length === 0) ? (
          <EmptyStateView
            title="No Active Workers"
            description="Add your household workers in the Maids tab to start tracking daily attendance."
            icon="👥"
          />
        ) : (
          data.entries.map((entry) => {
            const badge = getStateBadge(entry.state);
            const isProcessing = actionLoadingId === entry.maidId;

            return (
              <TouchableOpacity
                key={entry.maidId}
                style={styles.workerCard}
                activeOpacity={0.7}
                onPress={() => onNavigateToMaidDetail(entry.maidId)}
              >
                <View style={styles.workerInfo}>
                  <Text style={[Typography.h3, styles.workerName]}>{entry.maidName}</Text>
                  <View style={styles.workerMetaRow}>
                    <View style={[styles.badge, { backgroundColor: badge.bg }]}>
                      <Text style={[styles.badgeText, { color: badge.text }]}>{badge.label}</Text>
                    </View>
                    {entry.totalWorkedMinutes > 0 && (
                      <Text style={[Typography.caption, styles.workedTime]}>
                        ⏱️ {formatMinutes(entry.totalWorkedMinutes)}
                      </Text>
                    )}
                  </View>
                </View>

                {/* Quick Action Button */}
                <View style={styles.actionContainer}>
                  {entry.state === 'NOT_STARTED' && (
                    <TouchableOpacity
                      style={styles.entryButton}
                      onPress={() => handleEntry(entry)}
                      disabled={isProcessing}
                      activeOpacity={0.8}
                      accessibilityRole="button"
                      accessibilityLabel={`Record Entry for ${entry.maidName}`}
                    >
                      {isProcessing ? (
                        <ActivityIndicator size="small" color="#FFFFFF" />
                      ) : (
                        <Text style={styles.actionButtonText}>Entry</Text>
                      )}
                    </TouchableOpacity>
                  )}

                  {entry.state === 'WORKING' && (
                    <TouchableOpacity
                      style={styles.exitButton}
                      onPress={() => handleExit(entry)}
                      disabled={isProcessing}
                      activeOpacity={0.8}
                      accessibilityRole="button"
                      accessibilityLabel={`Record Exit for ${entry.maidName}`}
                    >
                      {isProcessing ? (
                        <ActivityIndicator size="small" color="#FFFFFF" />
                      ) : (
                        <Text style={styles.actionButtonText}>Exit</Text>
                      )}
                    </TouchableOpacity>
                  )}

                  {entry.state !== 'NOT_STARTED' && entry.state !== 'WORKING' && (
                    <Text style={[Typography.caption, styles.chevron]}>➔</Text>
                  )}
                </View>
              </TouchableOpacity>
            );
          })
        )}
      </View>

      {/* Monthly Payroll Snapshot Banner */}
      {data?.currentMonth && (
        <View style={styles.payrollSnapshotCard}>
          <View style={styles.payrollSnapshotInfo}>
            <Text style={[Typography.caption, styles.payrollSnapshotLabel]}>
              CURRENT MONTH ESTIMATE
            </Text>
            <Text style={[Typography.h2, styles.payrollSnapshotAmount]}>
              {data.currentMonth.currency === 'INR' ? '₹' : data.currentMonth.currency}{' '}
              {data.currentMonth.salaryPayable.toLocaleString()}
            </Text>
            <Text style={Typography.subtext}>Estimated salary payable this month</Text>
          </View>
          <TouchableOpacity
            style={styles.viewPayrollButton}
            onPress={onNavigateToPayroll}
            activeOpacity={0.8}
            accessibilityRole="button"
            accessibilityLabel="View Payroll Details"
          >
            <Text style={Typography.button}>View</Text>
          </TouchableOpacity>
        </View>
      )}
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: Spacing.xl,
    marginTop: Spacing.sm,
  },
  dateSubtitle: {
    color: Colors.textSecondary,
    marginBottom: Spacing.xs,
  },
  maidCountBadge: {
    backgroundColor: Colors.surface,
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.md,
    borderRadius: BorderRadius.full,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  maidCountText: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.primary,
  },
  section: {
    marginBottom: Spacing.xl,
  },
  sectionTitle: {
    marginBottom: Spacing.md,
  },
  workerCard: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.md,
    borderWidth: 1,
    borderColor: Colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowRadius: 6,
    elevation: 1,
  },
  workerInfo: {
    flex: 1,
    marginRight: Spacing.md,
  },
  workerName: {
    marginBottom: Spacing.xs,
  },
  workerMetaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  badge: {
    paddingHorizontal: Spacing.sm,
    paddingVertical: 2,
    borderRadius: BorderRadius.sm,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
  },
  workedTime: {
    color: Colors.textSecondary,
  },
  actionContainer: {
    minWidth: 80,
    alignItems: 'flex-end',
  },
  entryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    minWidth: 80,
  },
  exitButton: {
    backgroundColor: Colors.danger,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    minWidth: 80,
  },
  actionButtonText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
  },
  chevron: {
    color: Colors.textMuted,
    fontSize: 18,
  },
  payrollSnapshotCard: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    borderWidth: 1,
    borderColor: Colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: Spacing.sm,
  },
  payrollSnapshotInfo: {
    flex: 1,
  },
  payrollSnapshotLabel: {
    letterSpacing: 0.5,
    marginBottom: Spacing.xs,
  },
  payrollSnapshotAmount: {
    color: Colors.primary,
    marginBottom: Spacing.xs,
  },
  viewPayrollButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    borderRadius: BorderRadius.md,
    marginLeft: Spacing.md,
  },
});
