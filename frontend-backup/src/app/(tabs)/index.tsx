import { useCallback, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Pressable,
  ActivityIndicator,
  RefreshControl,
  Alert,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';

import { useAuth } from '@/context/AuthContext';
import {
  getWorkoutHistory,
  getOngoingWorkouts,
  startBlankWorkout,
  startWorkoutFromRoutine,
} from '@/api/workout.api';
import { getMyRoutines } from '@/api/routine.api';

import { WorkoutResponse } from '@/types/workout';
import { RoutineResponse } from '@/types/routine';

export default function HomeScreen() {
  const router = useRouter();
  const { user } = useAuth();

  const [routines, setRoutines] = useState<RoutineResponse[]>([]);
  const [history, setHistory] = useState<WorkoutResponse[]>([]);
  const [ongoing, setOngoing] = useState<WorkoutResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [starting, setStarting] = useState(false);
  const [startingRoutineId, setStartingRoutineId] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    try {
      const [routineData, historyData, ongoingData] = await Promise.all([
        getMyRoutines(),
        getWorkoutHistory(),
        getOngoingWorkouts(),
      ]);

      setRoutines(routineData);
      setHistory(historyData);
      setOngoing(ongoingData);
    } catch (error: any) {
      console.error(
        'Dashboard loading failed:',
        error.response?.data ?? error.message
      );

      Alert.alert(
        'Unable to load dashboard',
        'Please check your connection and try again.'
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadDashboard();
    }, [loadDashboard])
  );

  async function handleStartBlankWorkout() {
    try {
      setStarting(true);

      const workout = await startBlankWorkout();

      router.push({
        pathname: '/workout/[id]',
        params: { id: workout.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not start workout',
        error.response?.data?.error || 'Please try again.'
      );
    } finally {
      setStarting(false);
    }
  }

  async function handleStartRoutine(routineId: string) {
    try {
      setStartingRoutineId(routineId);

      const workout = await startWorkoutFromRoutine(routineId);

      router.push({
        pathname: '/workout/[id]',
        params: { id: workout.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not start routine',
        error.response?.data?.error || 'Please try again.'
      );
    } finally {
      setStartingRoutineId(null);
    }
  }

  function handleResumeWorkout(workoutId: string) {
    router.push({
      pathname: '/workout/[id]',
      params: { id: workoutId },
    });
  }

  function formatDate(date: string) {
    return new Date(date).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  }

  function formatDuration(seconds: number | null) {
    if (seconds === null) return '--';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
  }

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#B8F36B" />
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={styles.content}
      refreshControl={
        <RefreshControl
          refreshing={refreshing}
          onRefresh={() => {
            setRefreshing(true);
            loadDashboard();
          }}
          tintColor="#B8F36B"
        />
      }
    >
      {/* Header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.brand}>
            FIT<Text style={styles.brandAccent}>STUD</Text>
          </Text>

          <Text style={styles.greeting}>
            What's up, {user?.displayName?.split(' ')[0] || 'Athlete'}?
          </Text>
        </View>

        <View style={styles.avatar}>
          <Text style={styles.avatarText}>
            {user?.displayName?.charAt(0)?.toUpperCase() || 'A'}
          </Text>
        </View>
      </View>

      {/* Start workout */}
      <View style={styles.heroCard}>
        <Text style={styles.eyebrow}>YOUR NEXT SESSION</Text>

        <Text style={styles.heroTitle}>Ready to get stronger?</Text>

        <Text style={styles.heroSubtitle}>
          Every workout counts. Make today a good one.
        </Text>

        <Pressable
          style={[styles.primaryButton, starting && styles.disabled]}
          onPress={handleStartBlankWorkout}
          disabled={starting}
        >
          {starting ? (
            <ActivityIndicator color="#101010" />
          ) : (
            <Text style={styles.primaryButtonText}>
              +  START EMPTY WORKOUT
            </Text>
          )}
        </Pressable>
      </View>

      {/* Ongoing workouts */}
      {ongoing.length > 0 && (
        <View style={styles.section}>
          <SectionHeader title="IN PROGRESS" />

          {ongoing.map((workout) => (
            <Pressable
              key={workout.id}
              style={styles.ongoingCard}
              onPress={() => handleResumeWorkout(workout.id)}
            >
              <View style={styles.ongoingInfo}>
                <View style={styles.statusRow}>
                  <View style={styles.statusDot} />
                  <Text style={styles.statusText}>WORKOUT ACTIVE</Text>
                </View>

                <Text style={styles.cardTitle}>
                  {workout.sourceRoutineId ? 'Routine workout' : 'Empty workout'}
                </Text>

                <Text style={styles.mutedText}>
                  Started {formatDate(workout.startedAt)}
                </Text>
              </View>

              <Text style={styles.resumeText}>RESUME →</Text>
            </Pressable>
          ))}
        </View>
      )}

      {/* Routines */}
      <View style={styles.section}>
        <SectionHeader
          title="YOUR ROUTINES"
          action="See all"
          onAction={() => router.push('/(tabs)/routines')}
        />

        {routines.length === 0 ? (
          <View style={styles.emptyCard}>
            <Text style={styles.emptyTitle}>No routines yet</Text>
            <Text style={styles.mutedText}>
              Create a routine to make your workouts easier to start.
            </Text>

            <Pressable
              onPress={() => router.push('/(tabs)/routines')}
              style={styles.secondaryButton}
            >
              <Text style={styles.secondaryButtonText}>CREATE ROUTINE</Text>
            </Pressable>
          </View>
        ) : (
          routines.slice(0, 4).map((routine) => (
            <View key={routine.id} style={styles.routineCard}>
              <View style={styles.routineInfo}>
                <Text style={styles.cardTitle}>{routine.name}</Text>

                {routine.description ? (
                  <Text style={styles.mutedText} numberOfLines={1}>
                    {routine.description}
                  </Text>
                ) : (
                  <Text style={styles.mutedText}>Your workout template</Text>
                )}
              </View>

              <Pressable
                style={styles.startRoutineButton}
                onPress={() => handleStartRoutine(routine.id)}
                disabled={startingRoutineId !== null || starting}
              >
                {startingRoutineId === routine.id ? (
                  <ActivityIndicator color="#101010" size="small" />
                ) : (
                  <Text style={styles.startRoutineText}>START</Text>
                )}
              </Pressable>
            </View>
          ))
        )}
      </View>

      {/* Recent workouts */}
      <View style={styles.section}>
        <SectionHeader
          title="RECENT ACTIVITY"
          action="See all"
          onAction={() => router.push('/(tabs)/history')}
        />

        {history.length === 0 ? (
          <View style={styles.emptyCard}>
            <Text style={styles.emptyTitle}>Your journey starts here.</Text>
            <Text style={styles.mutedText}>
              Complete your first workout and your activity will appear here.
            </Text>
          </View>
        ) : (
          history.slice(0, 4).map((workout) => (
            <Pressable
              key={workout.id}
              style={styles.historyCard}
              onPress={() => handleResumeWorkout(workout.id)}
            >
              <View style={styles.historyIcon}>
                <Text style={styles.historyIconText}>↗</Text>
              </View>

              <View style={styles.historyInfo}>
                <Text style={styles.cardTitle}>
                  {workout.sourceRoutineId ? 'Routine workout' : 'Workout'}
                </Text>

                <Text style={styles.mutedText}>
                  {formatDate(workout.finishedAt || workout.startedAt)}
                </Text>
              </View>

              <View style={styles.historyStats}>
                <Text style={styles.statValue}>
                  {workout.totalVolumeKg.toLocaleString('en-IN')} kg
                </Text>
                <Text style={styles.mutedText}>
                  {workout.totalSets} sets · {formatDuration(workout.durationSeconds)}
                </Text>
              </View>
            </Pressable>
          ))
        )}
      </View>
    </ScrollView>
  );
}

function SectionHeader({
  title,
  action,
  onAction,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
}) {
  return (
    <View style={styles.sectionHeader}>
      <Text style={styles.sectionTitle}>{title}</Text>

      {action && onAction && (
        <Pressable onPress={onAction}>
          <Text style={styles.sectionAction}>{action}</Text>
        </Pressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#101010',
  },
  content: {
    paddingHorizontal: 20,
    paddingTop: 58,
    paddingBottom: 35,
  },
  loadingContainer: {
    flex: 1,
    backgroundColor: '#101010',
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 30,
  },
  brand: {
    color: '#FFFFFF',
    fontSize: 21,
    fontWeight: '900',
    letterSpacing: 1.5,
  },
  brandAccent: {
    color: '#B8F36B',
  },
  greeting: {
    color: '#FFFFFF',
    fontSize: 23,
    fontWeight: '800',
    marginTop: 12,
  },
  avatar: {
    width: 45,
    height: 45,
    borderRadius: 23,
    backgroundColor: '#292929',
    borderWidth: 1,
    borderColor: '#414141',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    color: '#B8F36B',
    fontSize: 18,
    fontWeight: '800',
  },
  heroCard: {
    backgroundColor: '#1C2515',
    borderColor: '#3A4B29',
    borderWidth: 1,
    borderRadius: 18,
    padding: 22,
    marginBottom: 32,
  },
  eyebrow: {
    color: '#B8F36B',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.5,
  },
  heroTitle: {
    color: '#FFFFFF',
    fontSize: 25,
    fontWeight: '900',
    marginTop: 15,
  },
  heroSubtitle: {
    color: '#A7B19A',
    fontSize: 13,
    marginTop: 9,
    lineHeight: 20,
  },
  primaryButton: {
    backgroundColor: '#B8F36B',
    height: 52,
    borderRadius: 11,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 23,
  },
  primaryButtonText: {
    color: '#101010',
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 0.8,
  },
  disabled: {
    opacity: 0.5,
  },
  section: {
    marginBottom: 32,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  },
  sectionTitle: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '900',
    letterSpacing: 1,
  },
  sectionAction: {
    color: '#B8F36B',
    fontSize: 12,
    fontWeight: '700',
  },
  ongoingCard: {
    backgroundColor: '#1B1B1B',
    borderWidth: 1,
    borderColor: '#3A4B29',
    borderRadius: 14,
    padding: 17,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  ongoingInfo: {
    flex: 1,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 9,
  },
  statusDot: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#B8F36B',
    marginRight: 7,
  },
  statusText: {
    color: '#B8F36B',
    fontSize: 9,
    fontWeight: '900',
    letterSpacing: 1,
  },
  resumeText: {
    color: '#B8F36B',
    fontSize: 10,
    fontWeight: '900',
    marginLeft: 8,
  },
  routineCard: {
    backgroundColor: '#1B1B1B',
    borderRadius: 13,
    padding: 16,
    marginBottom: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  routineInfo: {
    flex: 1,
    marginRight: 10,
  },
  cardTitle: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  mutedText: {
    color: '#858585',
    fontSize: 11,
    marginTop: 6,
  },
  startRoutineButton: {
    backgroundColor: '#B8F36B',
    paddingHorizontal: 17,
    paddingVertical: 10,
    borderRadius: 8,
    minWidth: 68,
    alignItems: 'center',
  },
  startRoutineText: {
    color: '#101010',
    fontSize: 10,
    fontWeight: '900',
  },
  emptyCard: {
    backgroundColor: '#1B1B1B',
    borderRadius: 13,
    padding: 19,
  },
  emptyTitle: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
  secondaryButton: {
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderColor: '#B8F36B',
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginTop: 16,
  },
  secondaryButtonText: {
    color: '#B8F36B',
    fontSize: 10,
    fontWeight: '900',
  },
  historyCard: {
    backgroundColor: '#1B1B1B',
    borderRadius: 13,
    padding: 14,
    marginBottom: 9,
    flexDirection: 'row',
    alignItems: 'center',
  },
  historyIcon: {
    width: 38,
    height: 38,
    borderRadius: 10,
    backgroundColor: '#292929',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  historyIconText: {
    color: '#B8F36B',
    fontSize: 20,
    fontWeight: '700',
  },
  historyInfo: {
    flex: 1,
  },
  historyStats: {
    alignItems: 'flex-end',
  },
  statValue: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '800',
  },
});