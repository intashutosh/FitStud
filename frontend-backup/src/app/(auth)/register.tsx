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

export default function RegisterScreen() {
  const router = useRouter();
  const { signUp, user, isLoading } = useAuth();

  const [displayName, setDisplayName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (isLoading) return null;
  if (user) return <Redirect href="/(tabs)" />;

  async function handleRegister() {
    if (!displayName.trim() || !username.trim() || !email.trim() || !password) {
      Alert.alert('Missing details', 'Please complete all fields.');
      return;
    }

    if (!/^[A-Za-z0-9_]{3,30}$/.test(username.trim())) {
      Alert.alert(
        'Invalid username',
        'Username must contain 3–30 letters, numbers or underscores.'
      );
      return;
    }

    if (password.length < 8 || password.length > 72) {
      Alert.alert('Invalid password', 'Password must be between 8 and 72 characters.');
      return;
    }

    try {
      setSubmitting(true);

      await signUp({
        displayName: displayName.trim(),
        username: username.trim(),
        email: email.trim().toLowerCase(),
        password,
      });

      Alert.alert(
        'Account created!',
        'Your FitStud account is ready. Please log in.',
        [{ text: 'Continue', onPress: () => router.replace('/(auth)/login') }]
      );
    } catch (error: any) {
      const message =
        error.response?.data?.error ||
        error.response?.data?.message ||
        'Unable to create your account. Please try again.';

      Alert.alert('Registration failed', message);
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
          <Text style={styles.tagline}>START YOUR JOURNEY.</Text>
        </View>

        <View style={styles.headingContainer}>
          <Text style={styles.heading}>Create account.</Text>
          <Text style={styles.subtitle}>
            Build consistency. Track progress. Get stronger.
          </Text>
        </View>

        <View style={styles.form}>
          <AuthInput
            label="DISPLAY NAME"
            placeholder="Your name"
            value={displayName}
            onChangeText={setDisplayName}
            autoCapitalize="words"
          />

          <AuthInput
            label="USERNAME"
            placeholder="Choose a username"
            value={username}
            onChangeText={setUsername}
          />

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
            placeholder="At least 8 characters"
            value={password}
            onChangeText={setPassword}
            secure
            autoComplete="new-password"
          />

          <Pressable
            style={({ pressed }) => [
              styles.primaryButton,
              pressed && styles.pressed,
              submitting && styles.disabled,
            ]}
            onPress={handleRegister}
            disabled={submitting}
          >
            {submitting ? (
              <ActivityIndicator color="#101010" />
            ) : (
              <Text style={styles.primaryButtonText}>CREATE ACCOUNT</Text>
            )}
          </Pressable>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Already have an account? </Text>
          <Link href="/(auth)/login" asChild>
            <Pressable>
              <Text style={styles.link}>Log in</Text>
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
    backgroundColor: '#101010',
  },
  content: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 25,
    paddingVertical: 40,
  },
  brandContainer: {
    marginBottom: 35,
  },
  logo: {
    color: '#FFFFFF',
    fontSize: 30,
    fontWeight: '900',
    letterSpacing: 2,
  },
  logoAccent: {
    color: '#B8F36B',
  },
  tagline: {
    color: '#777777',
    fontSize: 10,
    letterSpacing: 2,
    marginTop: 7,
    fontWeight: '600',
  },
  headingContainer: {
    marginBottom: 28,
  },
  heading: {
    color: '#FFFFFF',
    fontSize: 29,
    fontWeight: '800',
  },
  subtitle: {
    color: '#858585',
    fontSize: 13,
    marginTop: 8,
    lineHeight: 20,
  },
  form: {
    marginBottom: 25,
  },
  primaryButton: {
    height: 55,
    backgroundColor: '#B8F36B',
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  primaryButtonText: {
    color: '#101010',
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
    color: '#858585',
    fontSize: 13,
  },
  link: {
    color: '#B8F36B',
    fontSize: 13,
    fontWeight: '700',
  },
});