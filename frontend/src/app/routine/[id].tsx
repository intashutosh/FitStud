
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Modal,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';

import {
  addRoutineExercise,
  getRoutineDetails,
  removeRoutineExercise,
  reorderRoutineExercise,
  updateRoutine,
} from '@/api/routine.api';
import { getExerciseCatalog } from '@/api/exercise.api';
import { startWorkoutFromRoutine } from '@/api/workout.api';
import { ExerciseResponse } from '@/types/exercise';
import {
  RoutineDetailsResponse,
  RoutineExerciseResponse,
} from '@/types/routine';

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

export default function RoutineBuilderScreen() {
  const router = useRouter();
  const { id } = useLocalSearchParams<{ id: string }>();
  const routineId = Array.isArray(id) ? id[0] : id;

  const [details, setDetails] = useState<RoutineDetailsResponse | null>(null);
  const [catalog, setCatalog] = useState<ExerciseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [starting, setStarting] = useState(false);

  const [addModalVisible, setAddModalVisible] = useState(false);
  const [search, setSearch] = useState('');

  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editName, setEditName] = useState('');
  const [editDescription, setEditDescription] = useState('');

  const loadDetails = useCallback(async () => {
    if (!routineId) return;

    try {
      const [routineData, exerciseData] = await Promise.all([
        getRoutineDetails(routineId),
        getExerciseCatalog(),
      ]);

      setDetails(routineData);
      setCatalog(exerciseData);
    } catch (error: any) {
      Alert.alert(
        'Could not load routine',
        error?.response?.data?.error ?? 'Please try again.',
        [{ text: 'Go back', onPress: () => router.back() }],
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [routineId, router]);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      loadDetails();
    }, [loadDetails]),
  );

  const handleRefresh = () => {
    setRefreshing(true);
    loadDetails();
  };

  const selectedExerciseIds = useMemo(
    () => new Set(details?.exercises.map((item) => item.exerciseId) ?? []),
    [details],
  );

  const filteredCatalog = useMemo(() => {
    const query = search.trim().toLowerCase();

    return catalog.filter((exercise) => {
      if (selectedExerciseIds.has(exercise.id)) return false;

      return (
        !query ||
        exercise.name.toLowerCase().includes(query) ||
        exercise.muscleGroup?.toLowerCase().includes(query) ||
        exercise.equipment?.toLowerCase().includes(query)
      );
    });
  }, [catalog, search, selectedExerciseIds]);

  const handleAddExercise = async (exercise: ExerciseResponse) => {
    if (!details || !routineId) return;

    try {
      setSaving(true);

      await addRoutineExercise(routineId, {
        exerciseId: exercise.id,
        position: details.exercises.length,
      });

      setAddModalVisible(false);
      setSearch('');
      await loadDetails();
    } catch (error: any) {
      Alert.alert(
        'Could not add exercise',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveExercise = (exercise: RoutineExerciseResponse) => {
    Alert.alert(
      'Remove exercise?',
      `Remove ${exercise.exerciseName} from this routine?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Remove',
          style: 'destructive',
          onPress: async () => {
            try {
              await removeRoutineExercise(routineId, exercise.exerciseId);
              await loadDetails();
            } catch (error: any) {
              Alert.alert(
                'Could not remove exercise',
                error?.response?.data?.error ?? 'Please try again.',
              );
            }
          },
        },
      ],
    );
  };

  const handleMoveExercise = async (
    exercise: RoutineExerciseResponse,
    direction: -1 | 1,
  ) => {
    if (!details) return;

    const currentIndex = details.exercises.findIndex(
      (item) => item.id === exercise.id,
    );
    const newPosition = currentIndex + direction;

    if (newPosition < 0 || newPosition >= details.exercises.length) return;

    try {
      setSaving(true);
      await reorderRoutineExercise(
        routineId,
        exercise.exerciseId,
        newPosition,
      );
      await loadDetails();
    } catch (error: any) {
      Alert.alert(
        'Could not reorder exercise',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setSaving(false);
    }
  };

  const openEditModal = () => {
    if (!details) return;

    setEditName(details.routine.name);
    setEditDescription(details.routine.description ?? '');
    setEditModalVisible(true);
  };

  const handleUpdateRoutine = async () => {
    if (!details || !editName.trim()) {
      Alert.alert('Name required', 'Enter a name for your routine.');
      return;
    }

    try {
      setSaving(true);

      await updateRoutine(routineId, {
        name: editName.trim(),
        description: editDescription.trim() || undefined,
      });

      setEditModalVisible(false);
      await loadDetails();
    } catch (error: any) {
      Alert.alert(
        'Could not update routine',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setSaving(false);
    }
  };

  const handleStartWorkout = async () => {
    if (!routineId) return;

    try {
      setStarting(true);
      const workout = await startWorkoutFromRoutine(routineId);

      router.replace({
        pathname: '/workout/[id]',
        params: { id: workout.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not start workout',
        error?.response?.data?.error ?? 'Please try again.',
      );
    } finally {
      setStarting(false);
    }
  };

  const renderRoutineExercise = ({
    item,
    index,
  }: {
    item: RoutineExerciseResponse;
    index: number;
  }) => (
    <View style={styles.exerciseCard}>
      <View style={styles.positionBox}>
        <Text style={styles.positionText}>{index + 1}</Text>
      </View>

      <View style={styles.exerciseInfo}>
        <Text style={styles.exerciseName}>{item.exerciseName}</Text>
        {item.notes ? (
          <Text style={styles.exerciseNotes}>{item.notes}</Text>
        ) : (
          <Text style={styles.exerciseSubtext}>Exercise</Text>
        )}
      </View>

      <View style={styles.exerciseActions}>
        <Pressable
          style={[
            styles.orderButton,
            index === 0 && styles.inactiveButton,
          ]}
          disabled={index === 0 || saving}
          onPress={() => handleMoveExercise(item, -1)}
        >
          <Text style={styles.orderButtonText}>↑</Text>
        </Pressable>

        <Pressable
          style={[
            styles.orderButton,
            index === (details?.exercises.length ?? 0) - 1 &&
              styles.inactiveButton,
          ]}
          disabled={
            index === (details?.exercises.length ?? 0) - 1 || saving
          }
          onPress={() => handleMoveExercise(item, 1)}
        >
          <Text style={styles.orderButtonText}>↓</Text>
        </Pressable>

        <Pressable
          style={styles.removeButton}
          onPress={() => handleRemoveExercise(item)}
        >
          <Text style={styles.removeButtonText}>✕</Text>
        </Pressable>
      </View>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.loadingScreen}>
        <ActivityIndicator color={COLORS.primary} size="large" />
      </View>
    );
  }

  if (!details) {
    return (
      <View style={styles.loadingScreen}>
        <Text style={styles.emptyText}>Routine could not be found.</Text>
        <Pressable onPress={() => router.back()}>
          <Text style={styles.backText}>Go back</Text>
        </Pressable>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={details.exercises}
        keyExtractor={(item) => item.id}
        renderItem={renderRoutineExercise}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            tintColor={COLORS.primary}
          />
        }
        ListHeaderComponent={
          <View>
            <Pressable style={styles.backButton} onPress={() => router.back()}>
              <Text style={styles.backButtonText}>‹  Routines</Text>
            </Pressable>

            <View style={styles.titleRow}>
              <View style={styles.titleContent}>
                <Text style={styles.eyebrow}>ROUTINE BUILDER</Text>
                <Text style={styles.title}>{details.routine.name}</Text>
                <Text style={styles.description}>
                  {details.routine.description || 'Build your workout plan.'}
                </Text>
              </View>

              <Pressable style={styles.editButton} onPress={openEditModal}>
                <Text style={styles.editButtonText}>Edit</Text>
              </Pressable>
            </View>

            <View style={styles.statsRow}>
              <View style={styles.statCard}>
                <Text style={styles.statValue}>
                  {details.exercises.length}
                </Text>
                <Text style={styles.statLabel}>EXERCISES</Text>
              </View>
              <View style={styles.statCard}>
                <Text style={styles.statValue}>∞</Text>
                <Text style={styles.statLabel}>YOUR PACE</Text>
              </View>
            </View>

            <View style={styles.sectionHeader}>
              <View>
                <Text style={styles.sectionTitle}>Exercises</Text>
                <Text style={styles.sectionSubtitle}>
                  Arrange them in your preferred order
                </Text>
              </View>

              <Pressable
                style={styles.addExerciseButton}
                onPress={() => setAddModalVisible(true)}
              >
                <Text style={styles.addExerciseButtonText}>＋ Add</Text>
              </Pressable>
            </View>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.emptyExercises}>
            <Text style={styles.emptyExercisesTitle}>No exercises added</Text>
            <Text style={styles.emptyText}>
              Add exercises to build this routine.
            </Text>
            <Pressable
              style={styles.emptyAddButton}
              onPress={() => setAddModalVisible(true)}
            >
              <Text style={styles.emptyAddButtonText}>Add first exercise</Text>
            </Pressable>
          </View>
        }
        ListFooterComponent={
          <View style={styles.footer}>
            <Pressable
              style={[
                styles.startWorkoutButton,
                starting && styles.inactiveButton,
              ]}
              disabled={starting || saving}
              onPress={handleStartWorkout}
            >
              {starting ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.startWorkoutText}>Start this workout</Text>
              )}
            </Pressable>
          </View>
        }
      />

      <Modal
        visible={addModalVisible}
        animationType="slide"
        onRequestClose={() => setAddModalVisible(false)}
      >
        <View style={styles.modalScreen}>
          <View style={styles.modalHeader}>
            <View>
              <Text style={styles.modalEyebrow}>EXERCISE LIBRARY</Text>
              <Text style={styles.modalTitle}>Add exercise</Text>
            </View>
            <Pressable onPress={() => setAddModalVisible(false)}>
              <Text style={styles.closeButton}>✕</Text>
            </Pressable>
          </View>

          <TextInput
            value={search}
            onChangeText={setSearch}
            placeholder="Search exercises or muscle groups..."
            placeholderTextColor={COLORS.muted}
            style={styles.searchInput}
            autoCapitalize="none"
          />

          {saving ? (
            <ActivityIndicator
              color={COLORS.primary}
              size="large"
              style={styles.modalLoader}
            />
          ) : (
            <FlatList
              data={filteredCatalog}
              keyExtractor={(item) => item.id}
              keyboardShouldPersistTaps="handled"
              contentContainerStyle={styles.catalogList}
              renderItem={({ item }) => (
                <Pressable
                  style={styles.catalogItem}
                  onPress={() => handleAddExercise(item)}
                >
                  <View style={styles.catalogIcon}>
                    <Text style={styles.catalogIconText}>＋</Text>
                  </View>
                  <View style={styles.catalogInfo}>
                    <Text style={styles.catalogName}>{item.name}</Text>
                    <Text style={styles.catalogMeta}>
                      {[item.muscleGroup, item.equipment]
                        .filter(Boolean)
                        .join(' · ') || 'Exercise'}
                    </Text>
                  </View>
                  <Text style={styles.catalogAdd}>Add</Text>
                </Pressable>
              )}
              ListEmptyComponent={
                <Text style={styles.noResults}>
                  No matching exercises found.
                </Text>
              }
            />
          )}
        </View>
      </Modal>

      <Modal
        visible={editModalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setEditModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.editModal}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Edit routine</Text>
              <Pressable onPress={() => setEditModalVisible(false)}>
                <Text style={styles.closeButton}>✕</Text>
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>ROUTINE NAME</Text>
            <TextInput
              value={editName}
              onChangeText={setEditName}
              placeholder="Routine name"
              placeholderTextColor={COLORS.muted}
              style={styles.input}
              maxLength={100}
            />

            <Text style={styles.inputLabel}>DESCRIPTION</Text>
            <TextInput
              value={editDescription}
              onChangeText={setEditDescription}
              placeholder="Description (optional)"
              placeholderTextColor={COLORS.muted}
              style={[styles.input, styles.descriptionInput]}
              multiline
              maxLength={500}
            />

            <Pressable
              style={styles.saveButton}
              disabled={saving}
              onPress={handleUpdateRoutine}
            >
              {saving ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.saveButtonText}>Save changes</Text>
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
  loadingScreen: {
    flex: 1,
    backgroundColor: COLORS.background,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 15,
  },
  listContent: {
    paddingHorizontal: 18,
    paddingBottom: 35,
  },
  backButton: {
    marginTop: 18,
    marginBottom: 25,
    alignSelf: 'flex-start',
  },
  backButtonText: {
    color: COLORS.primary,
    fontSize: 15,
    fontWeight: '700',
  },
  titleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  titleContent: {
    flex: 1,
    paddingRight: 10,
  },
  eyebrow: {
    color: COLORS.primary,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.4,
    marginBottom: 7,
  },
  title: {
    color: COLORS.text,
    fontSize: 29,
    fontWeight: '800',
  },
  description: {
    color: COLORS.secondary,
    fontSize: 13,
    lineHeight: 19,
    marginTop: 7,
  },
  editButton: {
    backgroundColor: COLORS.elevated,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 10,
    paddingHorizontal: 15,
    paddingVertical: 9,
  },
  editButtonText: {
    color: COLORS.text,
    fontSize: 13,
    fontWeight: '700',
  },
  statsRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 22,
  },
  statCard: {
    flex: 1,
    backgroundColor: COLORS.surface,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: COLORS.border,
    padding: 14,
  },
  statValue: {
    color: COLORS.text,
    fontSize: 22,
    fontWeight: '800',
  },
  statLabel: {
    color: COLORS.muted,
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 1,
    marginTop: 5,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 30,
    marginBottom: 15,
  },
  sectionTitle: {
    color: COLORS.text,
    fontSize: 20,
    fontWeight: '800',
  },
  sectionSubtitle: {
    color: COLORS.secondary,
    fontSize: 12,
    marginTop: 4,
  },
  addExerciseButton: {
    backgroundColor: '#18283F',
    borderRadius: 10,
    paddingHorizontal: 13,
    paddingVertical: 9,
  },
  addExerciseButtonText: {
    color: COLORS.primary,
    fontSize: 13,
    fontWeight: '800',
  },
  exerciseCard: {
    backgroundColor: COLORS.surface,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: COLORS.border,
    padding: 12,
    marginBottom: 10,
    flexDirection: 'row',
    alignItems: 'center',
  },
  positionBox: {
    width: 34,
    height: 34,
    borderRadius: 10,
    backgroundColor: COLORS.elevated,
    alignItems: 'center',
    justifyContent: 'center',
  },
  positionText: {
    color: COLORS.primary,
    fontSize: 13,
    fontWeight: '800',
  },
  exerciseInfo: {
    flex: 1,
    marginLeft: 12,
  },
  exerciseName: {
    color: COLORS.text,
    fontSize: 14,
    fontWeight: '700',
  },
  exerciseNotes: {
    color: COLORS.secondary,
    fontSize: 11,
    marginTop: 4,
  },
  exerciseSubtext: {
    color: COLORS.muted,
    fontSize: 11,
    marginTop: 4,
  },
  exerciseActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  orderButton: {
    width: 29,
    height: 31,
    borderRadius: 8,
    backgroundColor: COLORS.elevated,
    alignItems: 'center',
    justifyContent: 'center',
  },
  orderButtonText: {
    color: COLORS.text,
    fontSize: 17,
    fontWeight: '700',
  },
  inactiveButton: {
    opacity: 0.35,
  },
  removeButton: {
    width: 29,
    height: 31,
    alignItems: 'center',
    justifyContent: 'center',
  },
  removeButtonText: {
    color: COLORS.danger,
    fontSize: 15,
    fontWeight: '700',
  },
  emptyExercises: {
    alignItems: 'center',
    paddingVertical: 35,
    backgroundColor: COLORS.surface,
    borderRadius: 15,
    borderWidth: 1,
    borderColor: COLORS.border,
  },
  emptyExercisesTitle: {
    color: COLORS.text,
    fontSize: 16,
    fontWeight: '700',
  },
  emptyText: {
    color: COLORS.secondary,
    fontSize: 13,
    textAlign: 'center',
    marginTop: 8,
    lineHeight: 19,
  },
  emptyAddButton: {
    backgroundColor: COLORS.primary,
    paddingHorizontal: 18,
    paddingVertical: 11,
    borderRadius: 10,
    marginTop: 18,
  },
  emptyAddButtonText: {
    color: '#FFFFFF',
    fontWeight: '800',
    fontSize: 13,
  },
  footer: {
    marginTop: 22,
  },
  startWorkoutButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
  },
  startWorkoutText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '800',
  },
  modalScreen: {
    flex: 1,
    backgroundColor: COLORS.background,
    paddingTop: 60,
    paddingHorizontal: 18,
  },
  modalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  modalEyebrow: {
    color: COLORS.primary,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.2,
    marginBottom: 5,
  },
  modalTitle: {
    color: COLORS.text,
    fontSize: 22,
    fontWeight: '800',
  },
  closeButton: {
    color: COLORS.secondary,
    fontSize: 21,
    padding: 5,
  },
  searchInput: {
    backgroundColor: COLORS.surface,
    borderWidth: 1,
    borderColor: COLORS.border,
    borderRadius: 12,
    color: COLORS.text,
    fontSize: 14,
    paddingHorizontal: 14,
    paddingVertical: 13,
    marginBottom: 14,
  },
  catalogList: {
    paddingBottom: 35,
  },
  catalogItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 13,
    borderBottomWidth: 1,
    borderBottomColor: COLORS.border,
  },
  catalogIcon: {
    height: 39,
    width: 39,
    borderRadius: 12,
    backgroundColor: '#18283F',
    alignItems: 'center',
    justifyContent: 'center',
  },
  catalogIconText: {
    color: COLORS.primary,
    fontSize: 22,
  },
  catalogInfo: {
    flex: 1,
    marginLeft: 12,
  },
  catalogName: {
    color: COLORS.text,
    fontSize: 14,
    fontWeight: '700',
  },
  catalogMeta: {
    color: COLORS.secondary,
    fontSize: 11,
    marginTop: 4,
  },
  catalogAdd: {
    color: COLORS.primary,
    fontSize: 13,
    fontWeight: '800',
    paddingHorizontal: 8,
  },
  noResults: {
    color: COLORS.secondary,
    textAlign: 'center',
    marginTop: 35,
    fontSize: 13,
  },
  modalLoader: {
    marginTop: 35,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.72)',
    justifyContent: 'flex-end',
  },
  editModal: {
    backgroundColor: COLORS.surface,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 22,
    paddingBottom: 35,
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
    minHeight: 80,
    textAlignVertical: 'top',
  },
  saveButton: {
    backgroundColor: COLORS.primary,
    borderRadius: 12,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 24,
  },
  saveButtonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '800',
  },
  backText: {
    color: COLORS.primary,
    fontSize: 14,
  },
});
