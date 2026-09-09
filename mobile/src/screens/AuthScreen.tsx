import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { ToastBanner } from '../components/FeedbackStates';

export const AuthScreen: React.FC = () => {
  const { login, register } = useAuth();
  const [isRegisterMode, setIsRegisterMode] = useState<boolean>(false);
  const [name, setName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async () => {
    setErrorMessage(null);
    if (!email.trim() || !password.trim()) {
      setErrorMessage('Please enter both email and password.');
      return;
    }

    if (isRegisterMode && !name.trim()) {
      setErrorMessage('Please enter your full name.');
      return;
    }

    setLoading(true);
    try {
      if (isRegisterMode) {
        await register({
          name: name.trim(),
          email: email.trim(),
          password,
          phone: phone.trim() || undefined,
        });
      } else {
        await login({
          email: email.trim(),
          password,
        });
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
        <View style={styles.header}>
          <Text style={styles.brandTitle}>Maid-X</Text>
          <Text style={styles.brandSubtitle}>Workforce & Salary Manager</Text>
        </View>

        <ToastBanner
          message={errorMessage}
          type="error"
          onDismiss={() => setErrorMessage(null)}
        />

        <View style={styles.card}>
          <Text style={[Typography.h2, styles.title]}>
            {isRegisterMode ? 'Create Account' : 'Welcome Back'}
          </Text>

          {isRegisterMode && (
            <View style={styles.field}>
              <Text style={[Typography.subtext, styles.label]}>Full Name</Text>
              <TextInput
                style={styles.input}
                placeholder="e.g. Rahul Sharma"
                placeholderTextColor={Colors.placeholder}
                value={name}
                onChangeText={setName}
                autoCapitalize="words"
                accessibilityLabel="Full Name input"
              />
            </View>
          )}

          <View style={styles.field}>
            <Text style={[Typography.subtext, styles.label]}>Email</Text>
            <TextInput
              style={styles.input}
              placeholder="name@example.com"
              placeholderTextColor={Colors.placeholder}
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              accessibilityLabel="Email input"
            />
          </View>

          <View style={styles.field}>
            <Text style={[Typography.subtext, styles.label]}>Password</Text>
            <TextInput
              style={styles.input}
              placeholder="••••••••"
              placeholderTextColor={Colors.placeholder}
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              accessibilityLabel="Password input"
            />
          </View>

          {isRegisterMode && (
            <View style={styles.field}>
              <Text style={[Typography.subtext, styles.label]}>Phone (Optional)</Text>
              <TextInput
                style={styles.input}
                placeholder="+91 9876543210"
                placeholderTextColor={Colors.placeholder}
                value={phone}
                onChangeText={setPhone}
                keyboardType="phone-pad"
                accessibilityLabel="Phone input"
              />
            </View>
          )}

          <TouchableOpacity
            style={[styles.submitButton, loading && styles.disabledButton]}
            onPress={handleSubmit}
            disabled={loading}
            activeOpacity={0.8}
            accessibilityRole="button"
            accessibilityLabel={isRegisterMode ? 'Register' : 'Login'}
          >
            {loading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={Typography.button}>
                {isRegisterMode ? 'Register' : 'Log In'}
              </Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.switchModeButton}
            onPress={() => {
              setErrorMessage(null);
              setIsRegisterMode(!isRegisterMode);
            }}
            activeOpacity={0.7}
          >
            <Text style={[Typography.subtext, styles.switchModeText]}>
              {isRegisterMode
                ? 'Already have an account? Log In'
                : "Don't have an account? Register"}
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: Spacing.xl,
  },
  header: {
    alignItems: 'center',
    marginBottom: Spacing.xxl,
  },
  brandTitle: {
    fontSize: 32,
    fontWeight: '800',
    color: Colors.primary,
    letterSpacing: -0.5,
  },
  brandSubtitle: {
    fontSize: 14,
    color: Colors.textSecondary,
    marginTop: Spacing.xs,
  },
  card: {
    backgroundColor: Colors.surface,
    borderRadius: BorderRadius.lg,
    padding: Spacing.xl,
    borderWidth: 1,
    borderColor: Colors.border,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 2,
  },
  title: {
    marginBottom: Spacing.lg,
  },
  field: {
    marginBottom: Spacing.md,
  },
  label: {
    fontWeight: '600',
    marginBottom: Spacing.xs,
    color: Colors.text,
  },
  input: {
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.lg,
    fontSize: 15,
    color: Colors.text,
    backgroundColor: Colors.surface,
  },
  submitButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    marginTop: Spacing.md,
  },
  disabledButton: {
    backgroundColor: Colors.disabled,
  },
  switchModeButton: {
    marginTop: Spacing.lg,
    alignItems: 'center',
  },
  switchModeText: {
    color: Colors.primary,
    fontWeight: '600',
  },
});
