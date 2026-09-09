import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';

interface LoadingScreenProps {
  message?: string;
}

export const LoadingView: React.FC<LoadingScreenProps> = ({ message = 'Loading...' }) => (
  <View style={styles.centerContainer} accessibilityRole="progressbar">
    <ActivityIndicator size="large" color={Colors.primary} />
    <Text style={[Typography.subtext, styles.loadingText]}>{message}</Text>
  </View>
);

interface ErrorViewProps {
  message?: string;
  onRetry?: () => void;
}

export const ErrorView: React.FC<ErrorViewProps> = ({
  message = 'Something went wrong. Please check your connection.',
  onRetry,
}) => (
  <View style={styles.centerContainer} accessibilityRole="alert">
    <View style={styles.errorIconContainer}>
      <Text style={styles.errorIconText}>!</Text>
    </View>
    <Text style={[Typography.h3, styles.errorTitle]}>Unable to Load Data</Text>
    <Text style={[Typography.subtext, styles.errorMessage]}>{message}</Text>
    {onRetry && (
      <TouchableOpacity
        style={styles.retryButton}
        onPress={onRetry}
        accessibilityRole="button"
        accessibilityLabel="Retry request"
        activeOpacity={0.8}
      >
        <Text style={Typography.button}>Try Again</Text>
      </TouchableOpacity>
    )}
  </View>
);

interface EmptyStateViewProps {
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
  icon?: string;
}

export const EmptyStateView: React.FC<EmptyStateViewProps> = ({
  title,
  description,
  actionLabel,
  onAction,
  icon = '📋',
}) => (
  <View style={styles.centerContainer}>
    <Text style={styles.emptyIcon}>{icon}</Text>
    <Text style={[Typography.h3, styles.emptyTitle]}>{title}</Text>
    {description && (
      <Text style={[Typography.subtext, styles.emptyDescription]}>{description}</Text>
    )}
    {actionLabel && onAction && (
      <TouchableOpacity
        style={styles.primaryButton}
        onPress={onAction}
        accessibilityRole="button"
        accessibilityLabel={actionLabel}
        activeOpacity={0.8}
      >
        <Text style={Typography.button}>{actionLabel}</Text>
      </TouchableOpacity>
    )}
  </View>
);

interface ToastBannerProps {
  message: string | null;
  type?: 'success' | 'error' | 'info';
  onDismiss?: () => void;
}

export const ToastBanner: React.FC<ToastBannerProps> = ({
  message,
  type = 'info',
  onDismiss,
}) => {
  if (!message) return null;

  const bg =
    type === 'success'
      ? Colors.successLight
      : type === 'error'
      ? Colors.dangerLight
      : Colors.primaryLight;

  const textColor =
    type === 'success'
      ? Colors.success
      : type === 'error'
      ? Colors.danger
      : Colors.primaryDark;

  return (
    <TouchableOpacity
      activeOpacity={0.9}
      onPress={onDismiss}
      style={[styles.toastContainer, { backgroundColor: bg }]}
      accessibilityRole="alert"
    >
      <Text style={[Typography.subtext, { color: textColor, fontWeight: '600', flex: 1 }]}>
        {message}
      </Text>
      {onDismiss && (
        <Text style={[Typography.caption, { color: textColor, marginLeft: Spacing.sm }]}>✕</Text>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  centerContainer: {
    flex: 1,
    padding: Spacing.xl,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: Spacing.md,
  },
  errorIconContainer: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: Colors.dangerLight,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  errorIconText: {
    color: Colors.danger,
    fontSize: 26,
    fontWeight: '700',
  },
  errorTitle: {
    marginBottom: Spacing.xs,
    textAlign: 'center',
  },
  errorMessage: {
    textAlign: 'center',
    marginBottom: Spacing.lg,
    maxWidth: 280,
  },
  retryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.md,
  },
  emptyIcon: {
    fontSize: 44,
    marginBottom: Spacing.md,
  },
  emptyTitle: {
    marginBottom: Spacing.xs,
    textAlign: 'center',
  },
  emptyDescription: {
    textAlign: 'center',
    marginBottom: Spacing.lg,
    maxWidth: 300,
  },
  primaryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.xl,
    borderRadius: BorderRadius.md,
  },
  toastContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    marginHorizontal: Spacing.lg,
    marginVertical: Spacing.sm,
    borderWidth: 1,
    borderColor: Colors.border,
  },
});
