import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Alert,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { ToastBanner } from '../components/FeedbackStates';

export const SettingsScreen: React.FC = () => {
  const { owner, logout, refreshProfile } = useAuth();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
      // Ignored
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <ToastBanner
        message={toast?.message || null}
        type={toast?.type}
        onDismiss={() => setToast(null)}
      />

      <View style={styles.header}>
        <Text style={Typography.h1}>Settings</Text>
        <Text style={Typography.subtext}>Account & System Configuration</Text>
      </View>

      {/* Owner Profile Card */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Account Profile</Text>

        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Name:</Text>
          <Text style={Typography.bodyMedium}>{owner?.name || 'Owner'}</Text>
        </View>

        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Email:</Text>
          <Text style={Typography.bodyMedium}>{owner?.email || '--'}</Text>
        </View>

        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Phone:</Text>
          <Text style={Typography.bodyMedium}>{owner?.phone || 'Not configured'}</Text>
        </View>

        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Timezone:</Text>
          <Text style={Typography.bodyMedium}>{owner?.timezone || 'Asia/Kolkata'}</Text>
        </View>

        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Currency:</Text>
          <Text style={Typography.bodyMedium}>{owner?.currencyCode || 'INR'}</Text>
        </View>
      </View>

      {/* App Info Card */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>App Information</Text>
        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Version:</Text>
          <Text style={Typography.bodyMedium}>1.0.0 (Phase 9 Build)</Text>
        </View>
        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Target Platform:</Text>
          <Text style={Typography.bodyMedium}>Expo / React Native</Text>
        </View>
        <View style={styles.profileRow}>
          <Text style={Typography.subtext}>Authoritative Clock:</Text>
          <Text style={Typography.bodyMedium}>Server Time (UTC/Timezone)</Text>
        </View>
      </View>

      {/* Logout Button */}
      <TouchableOpacity
        style={styles.logoutButton}
        onPress={handleLogout}
        activeOpacity={0.8}
        accessibilityRole="button"
        accessibilityLabel="Log out of account"
      >
        <Text style={styles.logoutText}>Log Out</Text>
      </TouchableOpacity>
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
    marginBottom: Spacing.xl,
    marginTop: Spacing.sm,
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
    marginBottom: Spacing.md,
  },
  profileRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
    borderBottomWidth: 1,
    borderBottomColor: Colors.borderSubtle,
  },
  logoutButton: {
    backgroundColor: Colors.dangerLight,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    marginTop: Spacing.md,
    borderWidth: 1,
    borderColor: Colors.danger,
  },
  logoutText: {
    color: Colors.danger,
    fontWeight: '700',
    fontSize: 15,
  },
});
