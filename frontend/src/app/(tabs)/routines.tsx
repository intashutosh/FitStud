
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Modal,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';

import {
  createRoutine,
  deleteRoutine,
  getMyRoutines,
} from '@/api/routine.api';
import { startWorkoutFromRoutine } from '@/api/workout.api';
import { RoutineResponse } from '@/types/routine';

const COLORS = {
  background: '#101010',
  surface: '#1B1B1B',
  elevated: '#242424',
  primary: '#579BFF',
  text: '#FFFFFF',
  secondary: '#929292',
  muted: '#666666',
  border: '#303030',
  danger: '#E47777',
};

export default function RoutinesScreen() {
  const router = useRouter();

  const [routines, setRoutines] = useState<RoutineResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [creating, setCreating] = useState(false);
  const [startingId, setStartingId] = useState<string | null>(null);

  const [modalVisible, setModalVisible] = useState(false);
  const [routineName, setRoutineName] = useState('');
  const [description, setDescription] = useState('');

  const loadRoutines = useCallback(async () => {
    try {
      const data = await getMyRoutines();
      setRoutines(data);
    } catch {
      Alert.alert('Error', 'Could not load your routines. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      loadRoutines();
    }, [loadRoutines]),
  );

  const handleRefresh = () => {
    setRefreshing(true);
    loadRoutines();
  };

  const handleCreate = async () => {
    const name = routineName.trim();

    if (!name) {
      Alert.alert('Routine name required', 'Enter a name for your routine.');
      return;
    }

    try {
      setCreating(true);

      const created = await createRoutine({
        name,
        description: description.trim() || undefined,
      });

      setModalVisible(false);
      setRoutineName('');
      setDescription('');

      await loadRoutines();

      router.push({
        pathname: '/routine/[id]',
        params: { id: created.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not create routine',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = (routine: RoutineResponse) => {
    Alert.alert(
      'Delete routine?',
      `Are you sure you want to delete "${routine.name}"? This will not delete completed workout history.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteRoutine(routine.id);
              setRoutines((current) =>
                current.filter((item) => item.id !== routine.id),
              );
            } catch {
              Alert.alert('Error', 'Could not delete this routine.');
            }
          },
        },
      ],
    );
  };

  const handleStartWorkout = async (routine: RoutineResponse) => {
    try {
      setStartingId(routine.id);
      const workout = await startWorkoutFromRoutine(routine.id);

      router.push({
        pathname: '/workout/[id]',
        params: { id: workout.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not start workout',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setStartingId(null);
    }
  };

  const renderRoutine = ({ item }: { item: RoutineResponse }) => (
    <Pressable
      style={styles.routineCard}
      onPress={() =>
        router.push({
          pathname: '/routine/[id]',
          params: { id: item.id },
        })
      }
    >
      <View style={styles.cardTop}>
        <View style={styles.routineIcon}>
          <Text style={styles.routineIconText}>▤</Text>
        </View>

        <Pressable
          hitSlop={10}
          onPress={() => handleDelete(item)}
          style={styles.moreButton}
        >
          <Text style={styles.moreButtonText}>•••</Text>
        </Pressable>
      </View>

      <Text style={styles.routineName}>{item.name}</Text>

      {item.description ? (
        <Text style={styles.routineDescription} numberOfLines={2}>
          {item.description}
        </Text>
      ) : (
        <Text style={styles.routineDescription}>No description</Text>
      )}

      <View style={styles.cardBottom}>
        <Text style={styles.cardHint}>Tap to edit routine</Text>

        <Pressable
          style={styles.startButton}
          disabled={startingId !== null}
          onPress={(event) => {
            // The card itself opens the editor; this button starts the workout.
            event.stopPropagation?.();
            handleStartWorkout(item);
          }}
        >
          {startingId === item.id ? (
            <ActivityIndicator size="small" color="#FFFFFF" />
          ) : (
            <Text style={styles.startButtonText}>Start</Text>
          )}
        </Pressable>
      </View>
    </Pressable>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>YOUR TRAINING</Text>
          <Text style={styles.title}>Routines</Text>
          <Text style={styles.subtitle}>
            Build a plan. Show up. Get stronger.
          </Text>
        </View>

        <Pressable
          style={styles.addButton}
          onPress={() => setModalVisible(true)}
        >
          <Text style={styles.addButtonText}>＋</Text>
        </Pressable>
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator color={COLORS.primary} size="large" />
        </View>
      ) : (
        <FlatList
          data={routines}
          keyExtractor={(item) => item.id}
          renderItem={renderRoutine}
          contentContainerStyle={[
            styles.listContent,
            routines.length === 0 && styles.emptyListContent,
          ]}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={handleRefresh}
              tintColor={COLORS.primary}
            />
          }
          ListHeaderComponent={
            routines.length > 0 ? (
              <Text style={styles.sectionLabel}>
                {routines.length} {routines.length === 1 ? 'ROUTINE' : 'ROUTINES'}
              </Text>
            ) : undefined
          }
          ListEmptyComponent={
            <View style={styles.emptyState}>
              <View style={styles.emptyIcon}>
                <Text style={styles.emptyIconText}>＋</Text>
              </View>
              <Text style={styles.emptyTitle}>No routines yet</Text>
              <Text style={styles.emptyDescription}>
                Create your first routine and add the exercises you want to
                follow during your workouts.
              </Text>
              <Pressable
                style={styles.emptyButton}
                onPress={() => setModalVisible(true)}
              >
                <Text style={styles.emptyButtonText}>Create routine</Text>
              </Pressable>
            </View>
          }
        />
      )}

      <Modal
        visible={modalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Create routine</Text>
              <Pressable onPress={() => setModalVisible(false)}>
                <Text style={styles.closeButton}>✕</Text>
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>ROUTINE NAME</Text>
            <TextInput
              value={routineName}
              onChangeText={setRoutineName}
              placeholder="e.g. Push Day"
              placeholderTextColor={COLORS.muted}
              style={styles.input}
              maxLength={100}
            />

            <Text style={styles.inputLabel}>DESCRIPTION (OPTIONAL)</Text>
            <TextInput
              value={description}
              onChangeText={setDescription}
              placeholder="A short description..."
              placeholderTextColor={COLORS.muted}
              style={[styles.input, styles.descriptionInput]}
              multiline
              maxLength={500}
            />

            <Pressable
              style={[styles.primaryButton, creating && styles.disabledButton]}
              disabled={creating}
              onPress={handleCreate}
            >
              {creating ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.primaryButtonText}>Create routine</Text>
              )}
            </Pressable>
          </View>
        </View>
      </Modal>
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
    paddingTop: 22,
    paddingBottom: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  eyebrow: {
    color: COLORS.primary,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1.5,
    marginBottom: 7,
  },
  title: {
    color: COLORS.text,
    fontSize: 30,
    fontWeight: '800',
  },
  subtitle: {
    color: COLORS.secondary,
    fontSize: 13,
    marginTop: 5,
  },
  addButton: {
    width: 46,
    height: 46,
    borderRadius: 15,
    backgroundColor: COLORS.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  addButtonText: {
    color: '#FFFFFF',
    fontSize: 29,
    lineHeight: 32,
    fontWeight: '500',
  },
  listContent: {
    paddingHorizontal: 18,
    paddingBottom: 35,
  },
  emptyListContent: {
    flexGrow: 1,
  },
  sectionLabel: {
    color: COLORS.muted,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1.2,
    marginHorizontal: 4,
    marginBottom: 13,
  },
  routineCard: {
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 18,
    padding: 17,
    marginBottom: 13,
  },
  cardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  routineIcon: {
    height: 39,
    width: 39,
    borderRadius: 12,
    backgroundColor: '#18283F',
    alignItems: 'center',
    justifyContent: 'center',
  },
  routineIconText: {
    color: COLORS.primary,
    fontSize: 21,
  },
  moreButton: {
    padding: 7,
  },
  moreButtonText: {
    color: COLORS.secondary,
    fontSize: 18,
    fontWeight: '800',
  },
  routineName: {
    color: COLORS.text,
    fontSize: 19,
    fontWeight: '700',
    marginTop: 15,
  },
  routineDescription: {
    color: COLORS.secondary,
    fontSize: 13,
    marginTop: 6,
    lineHeight: 19,
  },
  cardBottom: {
    marginTop: 17,
    paddingTop: 13,
    borderTopWidth: 1,
    borderTopColor: COLORS.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  cardHint: {
    color: COLORS.muted,
    fontSize: 12,
  },
  startButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 10,
    minWidth: 78,
    paddingVertical: 9,
    alignItems: 'center',
  },
  startButtonText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700',
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
    width: 66,
    height: 66,
    borderRadius: 22,
    backgroundColor: '#18283F',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  emptyIconText: {
    color: COLORS.primary,
    fontSize: 36,
  },
  emptyTitle: {
    color: COLORS.text,
    fontSize: 21,
    fontWeight: '800',
  },
  emptyDescription: {
    color: COLORS.secondary,
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 21,
    marginTop: 9,
    marginBottom: 22,
  },
  emptyButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 12,
    paddingHorizontal: 22,
    paddingVertical: 13,
  },
  emptyButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.72)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: COLORS.surface,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
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
    color: COLORS.text,
    fontSize: 21,
    fontWeight: '800',
  },
  closeButton: {
    color: COLORS.secondary,
    fontSize: 21,
    padding: 4,
  },
  inputLabel: {
    color: COLORS.secondary,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.1,
    marginBottom: 9,
    marginTop: 14,
  },
  input: {
    backgroundColor: COLORS.elevated,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 12,
    color: COLORS.text,
    fontSize: 15,
    paddingHorizontal: 14,
    paddingVertical: 13,
  },
  descriptionInput: {
    minHeight: 85,
    textAlignVertical: 'top',
  },
  primaryButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 12,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 25,
  },
  disabledButton: {
    opacity: 0.6,
  },
  primaryButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '800',
  },
});
