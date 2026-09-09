import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  RefreshControl,
} from 'react-native';
import { maidService, MaidSummary } from '../services/maids/maidService';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { LoadingView, ErrorView, EmptyStateView } from '../components/FeedbackStates';

interface MaidListScreenProps {
  onSelectMaid: (maidId: string) => void;
  onAddNewMaid: () => void;
}

export const MaidListScreen: React.FC<MaidListScreenProps> = ({
  onSelectMaid,
  onAddNewMaid,
}) => {
  const [maids, setMaids] = useState<MaidSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [refreshing, setRefreshing] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');

  const fetchMaids = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);

    try {
      const list = await maidService.listMaids(undefined, searchQuery.trim() || undefined);
      setMaids(list);
    } catch (err: any) {
      setError(err.message || 'Failed to load workers.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [searchQuery]);

  useEffect(() => {
    fetchMaids();
  }, [fetchMaids]);

  if (loading && !refreshing) {
    return <LoadingView message="Loading workers..." />;
  }

  if (error && maids.length === 0) {
    return <ErrorView message={error} onRetry={() => fetchMaids()} />;
  }

  return (
    <View style={styles.container}>
      {/* Search and Add Header */}
      <View style={styles.topBar}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search workers by name..."
          placeholderTextColor={Colors.placeholder}
          value={searchQuery}
          onChangeText={setSearchQuery}
          clearButtonMode="while-editing"
          accessibilityLabel="Search workers"
        />
        <TouchableOpacity
          style={styles.addButton}
          onPress={onAddNewMaid}
          activeOpacity={0.8}
          accessibilityRole="button"
          accessibilityLabel="Add New Worker"
        >
          <Text style={styles.addButtonText}>+ Add</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={maids}
        keyExtractor={(item) => item.id}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchMaids(true)} />
        }
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={
          <EmptyStateView
            title="No Workers Found"
            description={
              searchQuery
                ? 'No workers match your search query.'
                : 'You have not added any household workers yet.'
            }
            actionLabel={searchQuery ? undefined : 'Add Worker'}
            onAction={searchQuery ? undefined : onAddNewMaid}
            icon="👥"
          />
        }
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.card}
            activeOpacity={0.7}
            onPress={() => onSelectMaid(item.id)}
            accessibilityRole="button"
            accessibilityLabel={`Worker ${item.name}`}
          >
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>
                {item.name.charAt(0).toUpperCase()}
              </Text>
            </View>
            <View style={styles.info}>
              <View style={styles.nameRow}>
                <Text style={[Typography.h3, styles.name]}>{item.name}</Text>
                {!item.active && (
                  <View style={styles.inactiveBadge}>
                    <Text style={styles.inactiveBadgeText}>Inactive</Text>
                  </View>
                )}
              </View>
              {item.phone ? (
                <Text style={[Typography.subtext, styles.phone]}>{item.phone}</Text>
              ) : null}
              <View style={styles.salaryRow}>
                {item.activeSalaryMode && (
                  <Text style={[Typography.caption, styles.salaryMode]}>
                    {item.activeSalaryMode} RATE: ₹{item.activeSalaryAmount?.toLocaleString() || 0}
                  </Text>
                )}
              </View>
            </View>
            <Text style={styles.chevron}>➔</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  topBar: {
    flexDirection: 'row',
    padding: Spacing.lg,
    backgroundColor: Colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    gap: Spacing.md,
    alignItems: 'center',
  },
  searchInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.md,
    fontSize: 14,
    color: Colors.text,
    backgroundColor: Colors.background,
  },
  addButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.lg,
    borderRadius: BorderRadius.md,
    justifyContent: 'center',
    alignItems: 'center',
  },
  addButtonText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
  },
  listContent: {
    padding: Spacing.lg,
    flexGrow: 1,
  },
  card: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.lg,
    marginBottom: Spacing.md,
    borderWidth: 1,
    borderColor: Colors.border,
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: Colors.primaryLight,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: Spacing.md,
  },
  avatarText: {
    fontSize: 18,
    fontWeight: '700',
    color: Colors.primaryDark,
  },
  info: {
    flex: 1,
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  name: {
    color: Colors.text,
  },
  inactiveBadge: {
    backgroundColor: Colors.dangerLight,
    paddingHorizontal: Spacing.xs,
    paddingVertical: 2,
    borderRadius: BorderRadius.sm,
  },
  inactiveBadgeText: {
    fontSize: 10,
    fontWeight: '600',
    color: Colors.danger,
  },
  phone: {
    marginTop: 2,
  },
  salaryRow: {
    marginTop: Spacing.xs,
  },
  salaryMode: {
    color: Colors.textSecondary,
    fontWeight: '600',
  },
  chevron: {
    color: Colors.textMuted,
    fontSize: 16,
    marginLeft: Spacing.sm,
  },
});
