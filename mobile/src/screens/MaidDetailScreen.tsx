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
import { maidService, MaidDetail } from '../services/maids/maidService';
import {
  attendanceService,
  AttendanceSession,
} from '../services/attendance/attendanceService';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { LoadingView, ErrorView, ToastBanner } from '../components/FeedbackStates';

interface MaidDetailScreenProps {
  maidId: string;
  onBack: () => void;
  onEdit: (maidId: string) => void;
}

export const MaidDetailScreen: React.FC<MaidDetailScreenProps> = ({
  maidId,
  onBack,
  onEdit,
}) => {
  const [maid, setMaid] = useState<MaidDetail | null>(null);
  const [sessions, setSessions] = useState<AttendanceSession[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const loadData = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);

    try {
      const today = new Date().toISOString().split('T')[0];
      const thirtyDaysAgo = new Date(Date.now() - 30 * 86400000)
        .toISOString()
        .split('T')[0];

      const [maidRes, attendanceRes] = await Promise.all([
        maidService.getMaid(maidId),
        attendanceService.getAttendance(maidId, thirtyDaysAgo, today),
      ]);
      setMaid(maidRes);
      setSessions(attendanceRes);
    } catch (err: any) {
      setError(err.message || 'Failed to load worker details.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [maidId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const activeSession = sessions.find((s) => s.status === 'WORKING');

  const handleEntry = async () => {
    setActionLoading(true);
    try {
      await attendanceService.recordEntry(maidId);
      setToast({ message: 'Entry recorded successfully', type: 'success' });
      await loadData();
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to record entry', type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleExit = async () => {
    if (!activeSession) return;
    setActionLoading(true);
    try {
      await attendanceService.recordExit(maidId, activeSession.id);
      setToast({ message: 'Exit recorded successfully', type: 'success' });
      await loadData();
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to record exit', type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const formatMinutes = (minutes?: number) => {
    if (!minutes) return '0m';
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
  };

  const formatTime = (isoString?: string) => {
    if (!isoString) return '--:--';
    const date = new Date(isoString);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
  };

  if (loading && !refreshing) {
    return <LoadingView message="Loading worker profile..." />;
  }

  if (error && !maid) {
    return <ErrorView message={error} onRetry={() => loadData()} />;
  }

  const config = maid?.currentConfig;

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={() => loadData(true)} />
      }
    >
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onBack} activeOpacity={0.7}>
          <Text style={[Typography.bodyMedium, { color: Colors.primary }]}>← Back</Text>
        </TouchableOpacity>
        <Text style={Typography.h3}>Worker Profile</Text>
        <TouchableOpacity onPress={() => onEdit(maidId)} activeOpacity={0.7}>
          <Text style={[Typography.bodyMedium, { color: Colors.primary }]}>Edit</Text>
        </TouchableOpacity>
      </View>

      <ToastBanner
        message={toast?.message || null}
        type={toast?.type}
        onDismiss={() => setToast(null)}
      />

      {/* Header Info */}
      <View style={styles.profileCard}>
        <View style={styles.avatarLarge}>
          <Text style={styles.avatarTextLarge}>
            {maid?.name.charAt(0).toUpperCase()}
          </Text>
        </View>
        <Text style={[Typography.h2, styles.name]}>{maid?.name}</Text>
        <Text style={Typography.subtext}>{maid?.phone || 'No phone recorded'}</Text>
        <View style={styles.statusPill}>
          <Text style={styles.statusPillText}>
            {maid?.active ? 'Active Worker' : 'Inactive'}
          </Text>
        </View>
      </View>

      {/* Today's Quick Action */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Today's Status</Text>
        <View style={styles.todayRow}>
          <View>
            <Text style={Typography.subtext}>Current State</Text>
            <Text style={[Typography.h3, { color: activeSession ? Colors.primary : Colors.textSecondary }]}>
              {activeSession ? 'Working' : 'Not Working'}
            </Text>
          </View>

          {activeSession ? (
            <TouchableOpacity
              style={styles.exitButton}
              onPress={handleExit}
              disabled={actionLoading}
              activeOpacity={0.8}
            >
              {actionLoading ? (
                <ActivityIndicator size="small" color="#FFFFFF" />
              ) : (
                <Text style={Typography.button}>Record Exit</Text>
              )}
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={styles.entryButton}
              onPress={handleEntry}
              disabled={actionLoading}
              activeOpacity={0.8}
            >
              {actionLoading ? (
                <ActivityIndicator size="small" color="#FFFFFF" />
              ) : (
                <Text style={Typography.button}>Record Entry</Text>
              )}
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Employment / Salary Rules */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Employment & Salary</Text>
        {config ? (
          <View style={styles.configDetails}>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Salary Mode:</Text>
              <Text style={Typography.bodyMedium}>{config.salaryMode}</Text>
            </View>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Salary Amount:</Text>
              <Text style={Typography.bodyMedium}>₹{config.salaryAmount.toLocaleString()}</Text>
            </View>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Expected Work / Day:</Text>
              <Text style={Typography.bodyMedium}>{formatMinutes(config.expectedMinutesPerDay)}</Text>
            </View>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Grace Floor Threshold:</Text>
              <Text style={Typography.bodyMedium}>{config.shortfallThresholdMinutes} mins</Text>
            </View>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Overtime:</Text>
              <Text style={Typography.bodyMedium}>
                {config.overtimeEnabled ? `Enabled (${config.overtimeMultiplier}x)` : 'Disabled'}
              </Text>
            </View>
            <View style={styles.configRow}>
              <Text style={Typography.subtext}>Joining Date:</Text>
              <Text style={Typography.bodyMedium}>{maid?.joiningDate}</Text>
            </View>
          </View>
        ) : (
          <Text style={Typography.subtext}>No active employment contract configuration.</Text>
        )}
      </View>

      {/* Recent Attendance Sessions */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Recent Attendance (30 Days)</Text>
        {sessions.length === 0 ? (
          <Text style={Typography.subtext}>No attendance recorded in the last 30 days.</Text>
        ) : (
          sessions.slice(0, 10).map((session) => (
            <View key={session.id} style={styles.sessionRow}>
              <View>
                <Text style={Typography.bodyMedium}>{session.businessDate}</Text>
                <Text style={Typography.caption}>
                  {formatTime(session.entryAt)} - {formatTime(session.exitAt)}
                </Text>
              </View>
              <View style={styles.sessionRight}>
                <Text style={[Typography.bodyMedium, { fontWeight: '600' }]}>
                  {formatMinutes(session.durationMinutes)}
                </Text>
                <Text
                  style={[
                    Typography.caption,
                    {
                      color:
                        session.status === 'COMPLETED'
                          ? Colors.success
                          : session.status === 'WORKING'
                          ? Colors.primary
                          : Colors.danger,
                    },
                  ]}
                >
                  {session.status}
                </Text>
              </View>
            </View>
          ))
        )}
      </View>
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
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.lg,
  },
  profileCard: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.xl,
    alignItems: 'center',
    marginBottom: Spacing.lg,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  avatarLarge: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: Colors.primaryLight,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  avatarTextLarge: {
    fontSize: 26,
    fontWeight: '700',
    color: Colors.primaryDark,
  },
  name: {
    marginBottom: 2,
  },
  statusPill: {
    marginTop: Spacing.md,
    backgroundColor: Colors.successLight,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
    borderRadius: BorderRadius.full,
  },
  statusPillText: {
    color: Colors.success,
    fontSize: 12,
    fontWeight: '600',
  },
  card: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.lg,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  cardTitle: {
    marginBottom: Spacing.md,
  },
  todayRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  entryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.md,
  },
  exitButton: {
    backgroundColor: Colors.danger,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.md,
  },
  configDetails: {
    gap: Spacing.sm,
  },
  configRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  sessionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: Colors.borderSubtle,
  },
  sessionRight: {
    alignItems: 'flex-end',
  },
});
