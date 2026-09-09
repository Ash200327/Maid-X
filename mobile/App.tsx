import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, SafeAreaView, Platform } from 'react-native';
import { Colors, Spacing, Typography, BorderRadius } from './src/theme/colors';
import { AuthProvider, useAuth } from './src/context/AuthContext';
import { LoadingView } from './src/components/FeedbackStates';
import { AuthScreen } from './src/screens/AuthScreen';
import { DashboardScreen } from './src/screens/DashboardScreen';
import { MaidListScreen } from './src/screens/MaidListScreen';
import { AddMaidScreen } from './src/screens/AddMaidScreen';
import { MaidDetailScreen } from './src/screens/MaidDetailScreen';
import { PayrollScreen } from './src/screens/PayrollScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';
import { StatusBar } from 'expo-status-bar';

type Tab = 'home' | 'maids' | 'payroll' | 'settings';

function MainNavigator() {
  const { isAuthenticated, isLoading } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('home');
  const [selectedMaidId, setSelectedMaidId] = useState<string | null>(null);
  const [isAddingMaid, setIsAddingMaid] = useState<boolean>(false);

  if (isLoading) {
    return <LoadingView message="Initializing Maid-X..." />;
  }

  if (!isAuthenticated) {
    return <AuthScreen />;
  }

  const renderContent = () => {
    if (isAddingMaid) {
      return (
        <AddMaidScreen
          onSuccess={(id) => {
            setIsAddingMaid(false);
            setSelectedMaidId(id);
          }}
          onCancel={() => setIsAddingMaid(false)}
        />
      );
    }

    if (selectedMaidId) {
      return (
        <MaidDetailScreen
          maidId={selectedMaidId}
          onBack={() => setSelectedMaidId(null)}
          onEdit={(id) => {
            // Can be expanded to edit modal
          }}
        />
      );
    }

    switch (activeTab) {
      case 'home':
        return (
          <DashboardScreen
            onNavigateToPayroll={() => setActiveTab('payroll')}
            onNavigateToMaidDetail={(maidId) => setSelectedMaidId(maidId)}
          />
        );
      case 'maids':
        return (
          <MaidListScreen
            onSelectMaid={(maidId) => setSelectedMaidId(maidId)}
            onAddNewMaid={() => setIsAddingMaid(true)}
          />
        );
      case 'payroll':
        return <PayrollScreen />;
      case 'settings':
        return <SettingsScreen />;
    }
  };

  const handleTabPress = (tab: Tab) => {
    setSelectedMaidId(null);
    setIsAddingMaid(false);
    setActiveTab(tab);
  };

  const tabs: { key: Tab; label: string; icon: string }[] = [
    { key: 'home', label: 'Home', icon: '🏠' },
    { key: 'maids', label: 'Workers', icon: '👥' },
    { key: 'payroll', label: 'Payroll', icon: '💳' },
    { key: 'settings', label: 'Settings', icon: '⚙️' },
  ];

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <View style={styles.container}>{renderContent()}</View>

      {/* Bottom Navigation Bar */}
      {!isAddingMaid && !selectedMaidId && (
        <View style={styles.tabBar}>
          {tabs.map((tab) => {
            const isActive = activeTab === tab.key;
            return (
              <TouchableOpacity
                key={tab.key}
                style={styles.tabItem}
                onPress={() => handleTabPress(tab.key)}
                activeOpacity={0.7}
                accessibilityRole="tab"
                accessibilityState={{ selected: isActive }}
                accessibilityLabel={`${tab.label} tab`}
              >
                <Text style={[styles.tabIcon, isActive && styles.tabIconActive]}>
                  {tab.icon}
                </Text>
                <Text style={[styles.tabLabel, isActive && styles.tabLabelActive]}>
                  {tab.label}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>
      )}
    </SafeAreaView>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainNavigator />
    </AuthProvider>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: Colors.background,
    paddingTop: Platform.OS === 'android' ? 30 : 0,
  },
  container: {
    flex: 1,
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: Colors.surface,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    paddingVertical: Spacing.xs,
    paddingBottom: Platform.OS === 'ios' ? Spacing.sm : Spacing.xs,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: Spacing.xs,
  },
  tabIcon: {
    fontSize: 20,
    marginBottom: 2,
    opacity: 0.6,
  },
  tabIconActive: {
    opacity: 1,
    transform: [{ scale: 1.1 }],
  },
  tabLabel: {
    fontSize: 11,
    fontWeight: '500',
    color: Colors.textSecondary,
  },
  tabLabelActive: {
    color: Colors.primary,
    fontWeight: '700',
  },
});
