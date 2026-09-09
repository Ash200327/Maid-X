export const Colors = {
  primary: '#2563EB',      // Rich indigo-blue
  primaryDark: '#1D4ED8',
  primaryLight: '#DBEAFE',
  success: '#16A34A',      // Vibrant green
  successLight: '#DCFCE7',
  warning: '#D97706',      // Amber
  warningLight: '#FEF3C7',
  danger: '#DC2626',       // Red
  dangerLight: '#FEE2E2',
  background: '#F8FAFC',   // Light gray/slate
  surface: '#FFFFFF',
  card: '#FFFFFF',
  border: '#E2E8F0',
  borderSubtle: '#F1F5F9',
  text: '#0F172A',         // Slate 900
  textSecondary: '#64748B',// Slate 500
  textMuted: '#94A3B8',    // Slate 400
  placeholder: '#94A3B8',
  disabled: '#CBD5E1',
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
};

export const BorderRadius = {
  sm: 6,
  md: 10,
  lg: 14,
  full: 9999,
};

export const Typography = {
  h1: { fontSize: 24, fontWeight: '700' as const, lineHeight: 30, color: Colors.text },
  h2: { fontSize: 20, fontWeight: '700' as const, lineHeight: 26, color: Colors.text },
  h3: { fontSize: 17, fontWeight: '600' as const, lineHeight: 22, color: Colors.text },
  body: { fontSize: 15, fontWeight: '400' as const, lineHeight: 20, color: Colors.text },
  bodyMedium: { fontSize: 15, fontWeight: '500' as const, lineHeight: 20, color: Colors.text },
  subtext: { fontSize: 13, fontWeight: '400' as const, lineHeight: 18, color: Colors.textSecondary },
  caption: { fontSize: 12, fontWeight: '500' as const, lineHeight: 16, color: Colors.textMuted },
  button: { fontSize: 15, fontWeight: '600' as const, lineHeight: 20, color: '#FFFFFF' },
};
