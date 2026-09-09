import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Switch,
  ActivityIndicator,
} from 'react-native';
import {
  maidService,
  SalaryMode,
  CreateMaidPayload,
} from '../services/maids/maidService';
import { Colors, Spacing, Typography, BorderRadius } from '../theme/colors';
import { ToastBanner } from '../components/FeedbackStates';

interface AddMaidScreenProps {
  onSuccess: (maidId: string) => void;
  onCancel: () => void;
}

const WEEKDAYS = [
  { day: 1, label: 'Mon' },
  { day: 2, label: 'Tue' },
  { day: 3, label: 'Wed' },
  { day: 4, label: 'Thu' },
  { day: 5, label: 'Fri' },
  { day: 6, label: 'Sat' },
  { day: 7, label: 'Sun' },
];

export const AddMaidScreen: React.FC<AddMaidScreenProps> = ({ onSuccess, onCancel }) => {
  // Maid basic info
  const [name, setName] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [joiningDate, setJoiningDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [notes, setNotes] = useState<string>('');

  // Employment config
  const [salaryMode, setSalaryMode] = useState<SalaryMode>('MONTHLY');
  const [salaryAmount, setSalaryAmount] = useState<string>('12000');
  const [expectedHours, setExpectedHours] = useState<string>('8');
  const [workingDays, setWorkingDays] = useState<number[]>([1, 2, 3, 4, 5, 6]);
  const [shortfallThreshold, setShortfallThreshold] = useState<string>('15');
  const [overtimeEnabled, setOvertimeEnabled] = useState<boolean>(true);
  const [overtimeMultiplier, setOvertimeMultiplier] = useState<string>('1.5');

  const [loading, setLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const toggleWeekday = (day: number) => {
    if (workingDays.includes(day)) {
      if (workingDays.length === 1) return; // Must have at least 1 working day
      setWorkingDays(workingDays.filter((d) => d !== day));
    } else {
      setWorkingDays([...workingDays, day].sort());
    }
  };

  const handleSave = async () => {
    setErrorMessage(null);
    if (!name.trim()) {
      setErrorMessage('Worker name is required.');
      return;
    }

    const parsedSalary = parseFloat(salaryAmount);
    if (isNaN(parsedSalary) || parsedSalary < 0) {
      setErrorMessage('Please enter a valid salary amount.');
      return;
    }

    const parsedHours = parseFloat(expectedHours);
    if (isNaN(parsedHours) || parsedHours <= 0) {
      setErrorMessage('Expected hours must be greater than 0.');
      return;
    }

    setLoading(true);
    try {
      const payload: CreateMaidPayload = {
        name: name.trim(),
        phone: phone.trim() || undefined,
        joiningDate: joiningDate.trim(),
        notes: notes.trim() || undefined,
        employmentConfig: {
          salaryMode,
          salaryAmount: parsedSalary,
          expectedMinutesPerDay: Math.round(parsedHours * 60),
          shortfallThresholdMinutes: parseInt(shortfallThreshold, 10) || 15,
          overtimeEnabled,
          overtimeMultiplier: parseFloat(overtimeMultiplier) || 1.5,
          workingDays,
        },
      };

      const created = await maidService.createMaid(payload);
      onSuccess(created.id);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to save worker.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onCancel} activeOpacity={0.7}>
          <Text style={[Typography.bodyMedium, { color: Colors.textSecondary }]}>Cancel</Text>
        </TouchableOpacity>
        <Text style={Typography.h3}>Add Worker</Text>
        <TouchableOpacity onPress={handleSave} disabled={loading} activeOpacity={0.7}>
          <Text style={[Typography.bodyMedium, { color: Colors.primary, fontWeight: '700' }]}>
            Save
          </Text>
        </TouchableOpacity>
      </View>

      <ToastBanner
        message={errorMessage}
        type="error"
        onDismiss={() => setErrorMessage(null)}
      />

      {/* Basic Profile */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Basic Details</Text>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Full Name *</Text>
          <TextInput
            style={styles.input}
            placeholder="e.g. Rani Devi"
            placeholderTextColor={Colors.placeholder}
            value={name}
            onChangeText={setName}
          />
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Phone Number</Text>
          <TextInput
            style={styles.input}
            placeholder="+91 9876543210"
            placeholderTextColor={Colors.placeholder}
            value={phone}
            onChangeText={setPhone}
            keyboardType="phone-pad"
          />
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Joining Date (YYYY-MM-DD) *</Text>
          <TextInput
            style={styles.input}
            placeholder="2026-01-15"
            placeholderTextColor={Colors.placeholder}
            value={joiningDate}
            onChangeText={setJoiningDate}
          />
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Notes</Text>
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="Optional notes or tasks..."
            placeholderTextColor={Colors.placeholder}
            value={notes}
            onChangeText={setNotes}
            multiline
            numberOfLines={3}
          />
        </View>
      </View>

      {/* Salary & Employment Config */}
      <View style={styles.card}>
        <Text style={[Typography.h3, styles.cardTitle]}>Salary & Working Rules</Text>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Salary Mode</Text>
          <View style={styles.modeRow}>
            {(['MONTHLY', 'DAILY', 'HOURLY'] as SalaryMode[]).map((mode) => (
              <TouchableOpacity
                key={mode}
                style={[
                  styles.modeButton,
                  salaryMode === mode && styles.modeButtonSelected,
                ]}
                onPress={() => setSalaryMode(mode)}
                activeOpacity={0.8}
              >
                <Text
                  style={[
                    styles.modeButtonText,
                    salaryMode === mode && styles.modeButtonTextSelected,
                  ]}
                >
                  {mode}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>
            Salary Rate (₹) {salaryMode === 'MONTHLY' ? '/ month' : salaryMode === 'DAILY' ? '/ day' : '/ hr'}
          </Text>
          <TextInput
            style={styles.input}
            placeholder="12000"
            placeholderTextColor={Colors.placeholder}
            value={salaryAmount}
            onChangeText={setSalaryAmount}
            keyboardType="numeric"
          />
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Expected Hours / Day</Text>
          <TextInput
            style={styles.input}
            placeholder="8"
            placeholderTextColor={Colors.placeholder}
            value={expectedHours}
            onChangeText={setExpectedHours}
            keyboardType="numeric"
          />
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>Working Days</Text>
          <View style={styles.weekdaysRow}>
            {WEEKDAYS.map(({ day, label }) => {
              const isSelected = workingDays.includes(day);
              return (
                <TouchableOpacity
                  key={day}
                  style={[styles.weekdayPill, isSelected && styles.weekdayPillActive]}
                  onPress={() => toggleWeekday(day)}
                  activeOpacity={0.7}
                >
                  <Text
                    style={[
                      styles.weekdayPillText,
                      isSelected && styles.weekdayPillTextActive,
                    ]}
                  >
                    {label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
        </View>

        <View style={styles.field}>
          <Text style={[Typography.subtext, styles.label]}>
            Shortfall Threshold (Grace Floor Mins)
          </Text>
          <TextInput
            style={styles.input}
            placeholder="15"
            placeholderTextColor={Colors.placeholder}
            value={shortfallThreshold}
            onChangeText={setShortfallThreshold}
            keyboardType="numeric"
          />
        </View>

        <View style={styles.switchRow}>
          <View style={{ flex: 1 }}>
            <Text style={[Typography.bodyMedium, { color: Colors.text }]}>
              Overtime Enabled
            </Text>
            <Text style={Typography.caption}>Allow tracking and pay for extra hours</Text>
          </View>
          <Switch
            value={overtimeEnabled}
            onValueChange={setOvertimeEnabled}
            trackColor={{ false: Colors.border, true: Colors.primaryLight }}
            thumbColor={overtimeEnabled ? Colors.primary : '#FFFFFF'}
          />
        </View>

        {overtimeEnabled && (
          <View style={[styles.field, { marginTop: Spacing.md }]}>
            <Text style={[Typography.subtext, styles.label]}>Overtime Multiplier</Text>
            <TextInput
              style={styles.input}
              placeholder="1.5"
              placeholderTextColor={Colors.placeholder}
              value={overtimeMultiplier}
              onChangeText={setOvertimeMultiplier}
              keyboardType="numeric"
            />
          </View>
        )}
      </View>

      <TouchableOpacity
        style={[styles.primaryButton, loading && styles.disabledButton]}
        onPress={handleSave}
        disabled={loading}
        activeOpacity={0.8}
      >
        {loading ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={Typography.button}>Save Worker</Text>
        )}
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
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.lg,
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
  field: {
    marginBottom: Spacing.md,
  },
  label: {
    fontWeight: '600',
    color: Colors.text,
    marginBottom: Spacing.xs,
  },
  input: {
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.md,
    fontSize: 15,
    color: Colors.text,
    backgroundColor: Colors.surface,
  },
  textArea: {
    height: 70,
    textAlignVertical: 'top',
  },
  modeRow: {
    flexDirection: 'row',
    gap: Spacing.sm,
  },
  modeButton: {
    flex: 1,
    paddingVertical: Spacing.sm,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    backgroundColor: Colors.surface,
  },
  modeButtonSelected: {
    borderColor: Colors.primary,
    backgroundColor: Colors.primaryLight,
  },
  modeButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.textSecondary,
  },
  modeButtonTextSelected: {
    color: Colors.primaryDark,
    fontWeight: '700',
  },
  weekdaysRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.xs,
  },
  weekdayPill: {
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.sm,
    borderRadius: BorderRadius.sm,
    borderWidth: 1,
    borderColor: Colors.border,
    backgroundColor: Colors.surface,
  },
  weekdayPillActive: {
    borderColor: Colors.primary,
    backgroundColor: Colors.primary,
  },
  weekdayPillText: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  weekdayPillTextActive: {
    color: '#FFFFFF',
    fontWeight: '600',
  },
  switchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: Spacing.xs,
  },
  primaryButton: {
    backgroundColor: Colors.primary,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
    alignItems: 'center',
    marginTop: Spacing.sm,
  },
  disabledButton: {
    backgroundColor: Colors.disabled,
  },
});
