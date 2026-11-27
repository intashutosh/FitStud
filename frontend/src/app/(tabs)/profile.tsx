
import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Modal,
  Platform,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  KeyboardAvoidingView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import api from '@/api/client';
import { getWorkoutHistory } from '@/api/workout.api';
import { useAuth } from '@/context/AuthContext';
import { WorkoutResponse } from '@/types/workout';

interface UserProfile {
  id: string;
  email: string;
  username: string;
  displayName: string;
  bio: string | null;
  profilePhotoUrl: string | null;
  createdAt: string;
}

export default function ProfileScreen() {
  const { signOut } = useAuth();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const [editVisible, setEditVisible] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editName, setEditName] = useState('');
  const [editUsername, setEditUsername] = useState('');
  const [editBio, setEditBio] = useState('');

  const loadProfile = useCallback(async () => {
    try {
      const [profileResponse, workoutResponse] = await Promise.all([
        api.get<UserProfile>('/users/me'),
        getWorkoutHistory(),
      ]);

      setProfile(profileResponse.data);
      setWorkouts(
        workoutResponse.filter((workout) => workout.status === 'COMPLETED')
      );
    } catch (error: any) {
      Alert.alert(
        'Unable to load profile',
        error?.response?.data?.message || 'Please try again.'
      );
    }
  }, []);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      await loadProfile();
      setLoading(false);
    };

    load();
  }, [loadProfile]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadProfile();
    setRefreshing(false);
  };

  const openEditModal = () => {
    if (!profile) return;

    setEditName(profile.displayName);
    setEditUsername(profile.username);
    setEditBio(profile.bio || '');
    setEditVisible(true);
  };

  const saveProfile = async () => {
    const displayName = editName.trim();
    const username = editUsername.trim();
    const bio = editBio.trim();

    if (!displayName || !username) {
      Alert.alert('Invalid details', 'Name and username are required.');
      return;
    }

    if (!/^[A-Za-z0-9_]{3,30}$/.test(username)) {
      Alert.alert(
        'Invalid username',
        'Username must be 3–30 characters and contain only letters, numbers, and underscores.'
      );
      return;
    }

    if (bio.length > 300) {
      Alert.alert('Bio too long', 'Your bio cannot exceed 300 characters.');
      return;
    }

    try {
      setSaving(true);

      const response = await api.put<UserProfile>('/users/me', {
        displayName,
        username,
        bio: bio || null,
      });

      setProfile(response.data);
      setEditVisible(false);
      Alert.alert('Success', 'Your profile has been updated.');
    } catch (error: any) {
      Alert.alert(
        'Update failed',
        error?.response?.data?.message || 'Could not update your profile.'
      );
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    Alert.alert(
      'Log out',
      'Are you sure you want to log out of FitStud?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Log out',
          style: 'destructive',
          onPress: () => signOut(),
        },
      ]
    );
  };

  const totalVolume = workouts.reduce(
    (total, workout) => total + (workout.totalVolumeKg || 0),
    0
  );

  const totalSets = workouts.reduce(
    (total, workout) => total + (workout.totalSets || 0),
    0
  );

  const initials = profile?.displayName
    ? profile.displayName.trim().charAt(0).toUpperCase()
    : '?';

  if (loading) {
    return (
      <SafeAreaView style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#579BFF" />
      </SafeAreaView>
    );
  }

  if (!profile) {
    return (
      <SafeAreaView style={styles.loadingContainer}>
        <Text style={styles.errorText}>Unable to load your profile.</Text>
        <TouchableOpacity style={styles.retryButton} onPress={loadProfile}>
          <Text style={styles.retryText}>Try again</Text>
        </TouchableOpacity>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor="#579BFF"
          />
        }
      >
        <View style={styles.header}>
          <Text style={styles.headerTitle}>PROFILE</Text>
          <TouchableOpacity onPress={handleLogout}>
            <Text style={styles.logoutText}>LOG OUT</Text>
          </TouchableOpacity>
        </View>

        {/* Profile information */}
        <View style={styles.profileSection}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{initials}</Text>
          </View>

          <Text style={styles.displayName}>{profile.displayName}</Text>
          <Text style={styles.username}>@{profile.username}</Text>

          <Text style={styles.bio}>
            {profile.bio || 'No bio added yet.'}
          </Text>

          <Text style={styles.email}>{profile.email}</Text>

          <TouchableOpacity
            style={styles.editButton}
            onPress={openEditModal}
            activeOpacity={0.8}
          >
            <Text style={styles.editButtonText}>EDIT PROFILE</Text>
          </TouchableOpacity>
        </View>

        {/* Statistics */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>YOUR ACTIVITY</Text>

          <View style={styles.statsGrid}>
            <View style={styles.statCard}>
              <Text style={styles.statValue}>{workouts.length}</Text>
              <Text style={styles.statLabel}>WORKOUTS</Text>
            </View>

            <View style={styles.statCard}>
              <Text style={styles.statValue}>{totalSets}</Text>
              <Text style={styles.statLabel}>TOTAL SETS</Text>
            </View>

            <View style={styles.statCard}>
              <Text style={styles.statValue}>
                {Math.round(totalVolume).toLocaleString()}
              </Text>
              <Text style={styles.statLabel}>VOLUME (KG)</Text>
            </View>
          </View>
        </View>

        {/* Account information */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>ACCOUNT</Text>

          <View style={styles.infoCard}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Email</Text>
              <Text style={styles.infoValue}>{profile.email}</Text>
            </View>

            <View style={styles.separator} />

            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Username</Text>
              <Text style={styles.infoValue}>@{profile.username}</Text>
            </View>

            <View style={styles.separator} />

            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>Member since</Text>
              <Text style={styles.infoValue}>
                {new Date(profile.createdAt).toLocaleDateString('en-IN', {
                  month: 'short',
                  year: 'numeric',
                })}
              </Text>
            </View>
          </View>
        </View>

        <Text style={styles.footer}>FITSTUD · BUILD YOUR CONSISTENCY</Text>
      </ScrollView>

      {/* Edit Profile Modal */}
      <Modal
        visible={editVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setEditVisible(false)}
      >
        <KeyboardAvoidingView
          style={styles.modalOverlay}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>EDIT PROFILE</Text>
              <TouchableOpacity onPress={() => setEditVisible(false)}>
                <Text style={styles.closeText}>CLOSE</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.inputLabel}>DISPLAY NAME</Text>
            <TextInput
              style={styles.input}
              value={editName}
              onChangeText={setEditName}
              placeholder="Your name"
              placeholderTextColor="#666666"
              maxLength={80}
            />

            <Text style={styles.inputLabel}>USERNAME</Text>
            <TextInput
              style={styles.input}
              value={editUsername}
              onChangeText={setEditUsername}
              placeholder="username"
              placeholderTextColor="#666666"
              autoCapitalize="none"
              maxLength={30}
            />

            <Text style={styles.inputLabel}>BIO</Text>
            <TextInput
              style={[styles.input, styles.bioInput]}
              value={editBio}
              onChangeText={setEditBio}
              placeholder="Tell people a little about yourself..."
              placeholderTextColor="#666666"
              multiline
              maxLength={300}
              textAlignVertical="top"
            />

            <TouchableOpacity
              style={[styles.saveButton, saving && styles.disabledButton]}
              onPress={saveProfile}
              disabled={saving}
            >
              {saving ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.saveButtonText}>SAVE CHANGES</Text>
              )}
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#101010',
  },
  loadingContainer: {
    flex: 1,
    backgroundColor: '#101010',
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    paddingHorizontal: 22,
    paddingTop: 15,
    paddingBottom: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  headerTitle: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '900',
    letterSpacing: 2,
  },
  logoutText: {
    color: '#E47777',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1,
  },
  profileSection: {
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingTop: 15,
    paddingBottom: 30,
  },
  avatar: {
    width: 94,
    height: 94,
    borderRadius: 47,
    backgroundColor: '#18283F',
    borderWidth: 2,
    borderColor: '#579BFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 17,
  },
  avatarText: {
    color: '#579BFF',
    fontSize: 38,
    fontWeight: '900',
  },
  displayName: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: '900',
  },
  username: {
    color: '#929292',
    fontSize: 14,
    marginTop: 5,
  },
  bio: {
    color: '#CCCCCC',
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 20,
    marginTop: 15,
  },
  email: {
    color: '#666666',
    fontSize: 12,
    marginTop: 10,
  },
  editButton: {
    marginTop: 20,
    borderWidth: 1,
    borderColor: '#579BFF',
    borderRadius: 9,
    paddingVertical: 12,
    paddingHorizontal: 30,
  },
  editButtonText: {
    color: '#579BFF',
    fontSize: 11,
    fontWeight: '900',
    letterSpacing: 1,
  },
  section: {
    paddingHorizontal: 20,
    marginBottom: 28,
  },
  sectionTitle: {
    color: '#929292',
    fontSize: 11,
    fontWeight: '900',
    letterSpacing: 1.5,
    marginBottom: 14,
  },
  statsGrid: {
    flexDirection: 'row',
    gap: 10,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#1B1B1B',
    borderRadius: 12,
    paddingVertical: 20,
    paddingHorizontal: 8,
    alignItems: 'center',
  },
  statValue: {
    color: '#579BFF',
    fontSize: 22,
    fontWeight: '900',
  },
  statLabel: {
    color: '#929292',
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.6,
    marginTop: 8,
    textAlign: 'center',
  },
  infoCard: {
    backgroundColor: '#1B1B1B',
    borderRadius: 12,
    paddingHorizontal: 16,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 17,
    gap: 12,
  },
  infoLabel: {
    color: '#929292',
    fontSize: 13,
  },
  infoValue: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '600',
    flexShrink: 1,
    textAlign: 'right',
  },
  separator: {
    height: 1,
    backgroundColor: '#303030',
  },
  footer: {
    color: '#555555',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.5,
    textAlign: 'center',
    marginTop: 5,
    marginBottom: 30,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: '#1B1B1B',
    borderTopLeftRadius: 22,
    borderTopRightRadius: 22,
    padding: 22,
    paddingBottom: 35,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 25,
  },
  modalTitle: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '900',
    letterSpacing: 1.5,
  },
  closeText: {
    color: '#929292',
    fontSize: 11,
    fontWeight: '800',
  },
  inputLabel: {
    color: '#929292',
    fontSize: 10,
    fontWeight: '900',
    letterSpacing: 1,
    marginBottom: 9,
    marginTop: 12,
  },
  input: {
    backgroundColor: '#242424',
    borderWidth: 1,
    borderColor: '#303030',
    borderRadius: 9,
    color: '#FFFFFF',
    fontSize: 14,
    paddingHorizontal: 14,
    paddingVertical: 13,
  },
  bioInput: {
    height: 90,
  },
  saveButton: {
    backgroundColor: '#579BFF',
    borderRadius: 10,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 25,
  },
  disabledButton: {
    opacity: 0.6,
  },
  saveButtonText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 1,
  },
  errorText: {
    color: '#FFFFFF',
    fontSize: 14,
  },
  retryButton: {
    marginTop: 15,
    backgroundColor: '#579BFF',
    paddingHorizontal: 22,
    paddingVertical: 12,
    borderRadius: 8,
  },
  retryText: {
    color: '#FFFFFF',
    fontWeight: '800',
  },
});
