import { colors } from '@/theme/colors';
import { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { Link, Redirect, useRouter } from 'expo-router';

import AuthInput from '@/components/auth/AuthInput';
import { useAuth } from '@/context/AuthContext';

export default function LoginScreen() {
  const router = useRouter();
  const { signIn, user, isLoading } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isLoading) return null;
  if (user) return <Redirect href="/(tabs)" />;

  async function handleLogin() {
    if (!email.trim() || !password) {
      Alert.alert('Missing details', 'Please enter your email and password.');
      return;
    }

    try {
      setSubmitting(true);

      await signIn({
        email: email.trim().toLowerCase(),
        password,
      });

      router.replace('/(tabs)');
    } catch (error: any) {
      const message =
        error.response?.data?.error ||
        error.response?.data?.message ||
        'Unable to login. Please check your credentials and try again.';

      Alert.alert('Login failed', message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <KeyboardAvoidingView
      style={styles.screen}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.brandContainer}>
          <Text style={styles.logo}>FIT<Text style={styles.logoAccent}>STUD</Text></Text>
          <Text style={styles.tagline}>CONSISTENCY BUILDS STRENGTH.</Text>
        </View>

        <View style={styles.headingContainer}>
          <Text style={styles.heading}>Welcome back.</Text>
          <Text style={styles.subtitle}>
            Pick up where your fitness journey left off.
          </Text>
        </View>

        <View style={styles.form}>
          <AuthInput
            label="EMAIL ADDRESS"
            placeholder="you@example.com"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoComplete="email"
          />

          <AuthInput
            label="PASSWORD"
            placeholder="Enter your password"
            value={password}
            onChangeText={setPassword}
            secure
            autoComplete="current-password"
          />

          <Pressable
            style={({ pressed }) => [
              styles.primaryButton,
              pressed && styles.pressed,
              submitting && styles.disabled,
            ]}
            onPress={handleLogin}
            disabled={submitting}
          >
            {submitting ? (
              <ActivityIndicator color="#101010" />
            ) : (
              <Text style={styles.primaryButtonText}>LOG IN</Text>
            )}
          </Pressable>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>New to FitStud? </Text>
          <Link href="/(auth)/register" asChild>
            <Pressable>
              <Text style={styles.link}>Create an account</Text>
            </Pressable>
          </Link>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 25,
    paddingVertical: 45,
  },
  brandContainer: {
    marginBottom: 65,
  },
  logo: {
    color: colors.text,
    fontSize: 32,
    fontWeight: '900',
    letterSpacing: 2,
  },
  logoAccent: {
    color: colors.primary,
  },
  tagline: {
    color: colors.textSecondary,
    fontSize: 10,
    letterSpacing: 2,
    marginTop: 8,
    fontWeight: '600',
  },
  headingContainer: {
    marginBottom: 35,
  },
  heading: {
    color: colors.text,
    fontSize: 31,
    fontWeight: '800',
  },
  subtitle: {
    color: colors.textSecondary,
    fontSize: 14,
    marginTop: 9,
    lineHeight: 21,
  },
  form: {
    marginBottom: 30,
  },
  primaryButton: {
    height: 55,
    backgroundColor: colors.primary,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 10,
  },
  primaryButtonText: {
    color: colors.background,
    fontSize: 14,
    fontWeight: '900',
    letterSpacing: 1,
  },
  pressed: {
    opacity: 0.8,
  },
  disabled: {
    opacity: 0.6,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  footerText: {
    color: colors.textSecondary,
    fontSize: 13,
  },
  link: {
    color: colors.primary,
    fontSize: 13,
    fontWeight: '700',
  },
});