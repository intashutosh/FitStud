
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';

import { getWorkoutHistory } from '@/api/workout.api';
import { WorkoutResponse } from '@/types/workout';

const COLORS = {
  background: '#101010',
  surface: '#1B1B1B',
  elevated: '#242424',
  primary: '#579BFF',
  text: '#FFFFFF',
  secondary: '#929292',
  muted: '#666666',
  border: '#303030',
  success: '#48C78E',
};

function formatDate(dateString: string): string {
  const date = new Date(dateString);

  if (Number.isNaN(date.getTime())) return 'Unknown date';

  return date.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

function formatDuration(seconds: number | null): string {
  if (seconds === null || seconds < 0) return '--';

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);

  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes} min`;
}

function formatVolume(volume: number): string {
  return `${Number(volume || 0).toLocaleString('en-IN', {
    maximumFractionDigits: 1,
  })} kg`;
}

function getWorkoutTitle(workout: WorkoutResponse): string {
  return workout.sourceRoutineId ? 'Routine workout' : 'Workout session';
}

export default function HistoryScreen() {
  const router = useRouter();

  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadHistory = useCallback(async () => {
    try {
      const data = await getWorkoutHistory();

      const completedWorkouts = data
        .filter((workout) => workout.status === 'COMPLETED')
        .sort(
          (a, b) =>
            new Date(b.startedAt).getTime() -
            new Date(a.startedAt).getTime(),
        );

      setWorkouts(completedWorkouts);
    } catch (error: any) {
      Alert.alert(
        'Could not load history',
        error?.response?.data?.error ??
          'Please check your connection and try again.',
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      loadHistory();
    }, [loadHistory]),
  );

  const summary = useMemo(() => {
    return workouts.reduce(
      (total, workout) => ({
        sets: total.sets + workout.totalSets,
        volume: total.volume + Number(workout.totalVolumeKg || 0),
      }),
      { sets: 0, volume: 0 },
    );
  }, [workouts]);

  const handleOpenWorkout = (workout: WorkoutResponse) => {
    router.push({
      pathname: '/workout/[id]',
      params: { id: workout.id },
    });
  };

  const renderWorkout = ({ item }: { item: WorkoutResponse }) => (
    <Pressable
      style={styles.workoutCard}
      onPress={() => handleOpenWorkout(item)}
    >
      <View style={styles.workoutTop}>
        <View style={styles.workoutIcon}>
          <Text style={styles.workoutIconText}>↗</Text>
        </View>

        <View style={styles.workoutHeading}>
          <Text style={styles.workoutName}>{getWorkoutTitle(item)}</Text>
          <Text style={styles.workoutDate}>
            {formatDate(item.startedAt)}
          </Text>
        </View>

        <View style={styles.completedBadge}>
          <View style={styles.completedDot} />
          <Text style={styles.completedText}>Completed</Text>
        </View>
      </View>

      <View style={styles.metricsRow}>
        <View style={styles.metric}>
          <Text style={styles.metricValue}>
            {formatDuration(item.durationSeconds)}
          </Text>
          <Text style={styles.metricLabel}>DURATION</Text>
        </View>

        <View style={styles.metricDivider} />

        <View style={styles.metric}>
          <Text style={styles.metricValue}>{item.totalSets}</Text>
          <Text style={styles.metricLabel}>SETS</Text>
        </View>

        <View style={styles.metricDivider} />

        <View style={styles.metric}>
          <Text style={styles.metricValue}>
            {formatVolume(item.totalVolumeKg)}
          </Text>
          <Text style={styles.metricLabel}>VOLUME</Text>
        </View>
      </View>

      <View style={styles.cardFooter}>
        <Text style={styles.cardFooterText}>
          {item.prCount > 0
            ? `🏆 ${item.prCount} personal record${item.prCount > 1 ? 's' : ''}`
            : 'View workout details'}
        </Text>
        <Text style={styles.chevron}>›</Text>
      </View>
    </Pressable>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.eyebrow}>YOUR PROGRESS</Text>
        <Text style={styles.title}>Workout History</Text>
        <Text style={styles.subtitle}>
          Every session is a step forward.
        </Text>
      </View>

      {!loading && workouts.length > 0 && (
        <View style={styles.summaryContainer}>
          <View style={styles.summaryCard}>
            <Text style={styles.summaryValue}>{workouts.length}</Text>
            <Text style={styles.summaryLabel}>WORKOUTS</Text>
          </View>

          <View style={styles.summaryCard}>
            <Text style={styles.summaryValue}>
              {summary.sets.toLocaleString('en-IN')}
            </Text>
            <Text style={styles.summaryLabel}>TOTAL SETS</Text>
          </View>

          <View style={styles.summaryCard}>
            <Text style={styles.summaryValue}>
              {formatVolume(summary.volume)}
            </Text>
            <Text style={styles.summaryLabel}>TOTAL VOLUME</Text>
          </View>
        </View>
      )}

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={COLORS.primary} />
        </View>
      ) : (
        <FlatList
          data={workouts}
          keyExtractor={(item) => item.id}
          renderItem={renderWorkout}
          contentContainerStyle={[
            styles.listContent,
            workouts.length === 0 && styles.emptyList,
          ]}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={() => {
                setRefreshing(true);
                loadHistory();
              }}
              tintColor={COLORS.primary}
            />
          }
          ListHeaderComponent={
            workouts.length > 0 ? (
              <Text style={styles.sectionLabel}>RECENT SESSIONS</Text>
            ) : undefined
          }
          ListEmptyComponent={
            <View style={styles.emptyState}>
              <View style={styles.emptyIcon}>
                <Text style={styles.emptyIconText}>◷</Text>
              </View>

              <Text style={styles.emptyTitle}>Your journey starts here</Text>

              <Text style={styles.emptyDescription}>
                Completed workouts will appear here. Start a workout and
                your progress will be saved automatically.
              </Text>

              <Pressable
                style={styles.startButton}
                onPress={() => router.push('/(tabs)')}
              >
                <Text style={styles.startButtonText}>Start a workout</Text>
              </Pressable>
            </View>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.background,
  },
  header: {
    paddingHorizontal: 22,
    paddingTop: 23,
    paddingBottom: 18,
  },
  eyebrow: {
    color: COLORS.primary,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.5,
    marginBottom: 7,
  },
  title: {
    color: COLORS.text,
    fontSize: 28,
    fontWeight: '800',
  },
  subtitle: {
    color: COLORS.secondary,
    fontSize: 13,
    marginTop: 6,
  },
  summaryContainer: {
    flexDirection: 'row',
    paddingHorizontal: 18,
    gap: 9,
    marginBottom: 23,
  },
  summaryCard: {
    flex: 1,
    backgroundColor: COLORS.surface,
    borderRadius: 13,
    borderWidth: 1,
    borderColor: COLORS.border,
    paddingHorizontal: 10,
    paddingVertical: 13,
  },
  summaryValue: {
    color: COLORS.text,
    fontSize: 17,
    fontWeight: '800',
  },
  summaryLabel: {
    color: COLORS.muted,
    fontSize: 8,
    fontWeight: '800',
    letterSpacing: 0.8,
    marginTop: 6,
  },
  listContent: {
    paddingHorizontal: 18,
    paddingBottom: 35,
  },
  emptyList: {
    flexGrow: 1,
  },
  sectionLabel: {
    color: COLORS.muted,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.3,
    marginBottom: 13,
    marginHorizontal: 3,
  },
  workoutCard: {
    backgroundColor: COLORS.surface,
    borderRadius: 17,
    borderWidth: 1,
    borderColor: COLORS.border,
    padding: 15,
    marginBottom: 12,
  },
  workoutTop: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  workoutIcon: {
    width: 42,
    height: 42,
    borderRadius: 13,
    backgroundColor: '#18283F',
    alignItems: 'center',
    justifyContent: 'center',
  },
  workoutIconText: {
    color: COLORS.primary,
    fontSize: 24,
    fontWeight: '700',
  },
  workoutHeading: {
    flex: 1,
    marginLeft: 11,
  },
  workoutName: {
    color: COLORS.text,
    fontSize: 14,
    fontWeight: '800',
  },
  workoutDate: {
    color: COLORS.secondary,
    fontSize: 11,
    marginTop: 5,
  },
  completedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#173326',
    borderRadius: 20,
    paddingHorizontal: 9,
    paddingVertical: 6,
  },
  completedDot: {
    height: 6,
    width: 6,
    borderRadius: 3,
    backgroundColor: COLORS.success,
    marginRight: 5,
  },
  completedText: {
    color: COLORS.success,
    fontSize: 9,
    fontWeight: '800',
  },
  metricsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 19,
    marginBottom: 15,
  },
  metric: {
    flex: 1,
    alignItems: 'center',
  },
  metricValue: {
    color: COLORS.text,
    fontSize: 14,
    fontWeight: '800',
  },
  metricLabel: {
    color: COLORS.muted,
    fontSize: 8,
    fontWeight: '800',
    letterSpacing: 0.8,
    marginTop: 5,
  },
  metricDivider: {
    height: 28,
    width: 1,
    backgroundColor: COLORS.border,
  },
  cardFooter: {
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    paddingTop: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  cardFooterText: {
    color: COLORS.secondary,
    fontSize: 11,
  },
  chevron: {
    color: COLORS.primary,
    fontSize: 23,
    fontWeight: '500',
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  emptyIcon: {
    width: 68,
    height: 68,
    borderRadius: 22,
    backgroundColor: '#18283F',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  emptyIconText: {
    color: COLORS.primary,
    fontSize: 37,
  },
  emptyTitle: {
    color: COLORS.text,
    fontSize: 19,
    fontWeight: '800',
    textAlign: 'center',
  },
  emptyDescription: {
    color: COLORS.secondary,
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 21,
    marginTop: 9,
  },
  startButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 12,
    paddingHorizontal: 21,
    paddingVertical: 13,
    marginTop: 22,
  },
  startButtonText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '800',
  },
});
