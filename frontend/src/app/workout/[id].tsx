import { colors } from '@/theme/colors';
import { useCallback, useEffect, useState } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  Pressable,
  StyleSheet,
  ActivityIndicator,
  Modal,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';

import {
  getWorkoutDetails,
  addWorkoutExercise,
  addWorkoutSet,
  updateWorkoutSet,
  updateSetCompletion,
  deleteWorkoutSet,
  deleteWorkoutExercise,
  getPreviousPerformance,
  finishWorkout,
} from '@/api/workout.api';

import { getExerciseCatalog } from '@/api/exercise.api';

import {
  WorkoutDetailsResponse,
  WorkoutSetResponse,
} from '@/types/workout';

import { ExerciseResponse } from '@/types/exercise';

export default function ActiveWorkoutScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();

  const [workout, setWorkout] = useState<WorkoutDetailsResponse | null>(null);
  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [previous, setPrevious] = useState<
    Record<string, WorkoutSetResponse[]>
  >({});

  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [exerciseModal, setExerciseModal] = useState(false);
  const [search, setSearch] = useState('');
  const isCompleted = workout?.workout.status === 'COMPLETED';

  const [setValues, setSetValues] = useState<
    Record<string, { weight: string; reps: string }>
  >({});

  const loadWorkout = useCallback(async () => {
    if (!id) return;

    try {
      const data = await getWorkoutDetails(id);
      setWorkout(data);

      const catalog = await getExerciseCatalog();
      setExercises(catalog);

      const previousResults = await Promise.all(
        data.exercises.map(async (item) => {
          try {
            const result = await getPreviousPerformance(
              id,
              item.exercise.exerciseId
            );
            return [item.exercise.exerciseId, result] as const;
          } catch {
            return [item.exercise.exerciseId, []] as const;
          }
        })
      );

      setPrevious(Object.fromEntries(previousResults));

      const values: Record<string, { weight: string; reps: string }> = {};

      data.exercises.forEach((item) => {
        item.sets.forEach((set) => {
          values[set.id] = {
            weight: set.weightKg?.toString() ?? '',
            reps: set.reps?.toString() ?? '',
          };
        });
      });

      setSetValues(values);
    } catch (error: any) {
      Alert.alert(
        'Unable to load workout',
        error.response?.data?.error || 'Please try again.'
      );
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadWorkout();
  }, [loadWorkout]);

  async function refreshWorkout() {
    const data = await getWorkoutDetails(id);
    setWorkout(data);

    const values: Record<string, { weight: string; reps: string }> = {};

    data.exercises.forEach((item) => {
      item.sets.forEach((set) => {
        values[set.id] = {
          weight: set.weightKg?.toString() ?? '',
          reps: set.reps?.toString() ?? '',
        };
      });
    });

    setSetValues(values);
  }

  async function handleAddExercise(exercise: ExerciseResponse) {
    if (!workout) return;

    try {
      setBusy(true);

      await addWorkoutExercise(
        id,
        exercise.id,
        workout.exercises.length
      );

      const previousSets = await getPreviousPerformance(id, exercise.id);

      setPrevious((current) => ({
        ...current,
        [exercise.id]: previousSets,
      }));

      await refreshWorkout();
      setExerciseModal(false);
      setSearch('');
    } catch (error: any) {
      Alert.alert(
        'Unable to add exercise',
        error.response?.data?.error || 'Please try again.'
      );
    } finally {
      setBusy(false);
    }
  }

  async function handleAddSet(workoutExerciseId: string) {
  try {
    setBusy(true);
    await addWorkoutSet(id, workoutExerciseId);
    await refreshWorkout();
  } catch (error: any) {
    Alert.alert(
      'Unable to add set',
      error.response?.data?.error || 'Please try again.'
    );
  } finally {
    setBusy(false);
  }
}

  
async function handleSaveSet(
  workoutExerciseId: string,
  set: WorkoutSetResponse
) {
  const values = setValues[set.id];

  if (!values) return;

  const weight = values.weight.trim() === '' ? null : Number(values.weight);
  const reps = values.reps.trim() === '' ? null : Number(values.reps);

  if (
    (weight !== null && (!Number.isFinite(weight) || weight < 0)) ||
    (reps !== null && (!Number.isInteger(reps) || reps < 1))
  ) {
    Alert.alert('Invalid set', 'Enter a valid weight and whole number of reps.');
    return;
  }

  try {
    setBusy(true);

    await updateWorkoutSet(
      id,
      workoutExerciseId,
      set.id,
      weight,
      reps
    );

    await refreshWorkout();
  } catch (error: any) {
    Alert.alert(
      'Unable to save set',
      error.response?.data?.error || 'Please try again.'
    );
  } finally {
    setBusy(false);
  }
}


  
async function handleCompletion(
  workoutExerciseId: string,
  set: WorkoutSetResponse
) {
  try {
    await updateSetCompletion(
      id,
      workoutExerciseId,
      set.id,
      !set.completed
    );

    await refreshWorkout();
  } catch (error: any) {
    Alert.alert(
      'Unable to update set',
      error.response?.data?.error || 'Please try again.'
    );
  }
}


  
async function handleDeleteSet(
  workoutExerciseId: string,
  setId: string
) {
  try {
    setBusy(true);
    await deleteWorkoutSet(id, workoutExerciseId, setId);
    await refreshWorkout();
  } catch (error: any) {
    Alert.alert(
      'Unable to delete set',
      error.response?.data?.error || 'Please try again.'
    );
  } finally {
    setBusy(false);
  }
}


  
async function handleDeleteExercise(workoutExerciseId: string) {
  Alert.alert(
    'Remove exercise?',
    'This will remove the exercise and its sets from this workout.',
    [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Remove',
        style: 'destructive',
        onPress: async () => {
          try {
            setBusy(true);
            await deleteWorkoutExercise(id, workoutExerciseId);
            await refreshWorkout();
          } catch (error: any) {
            Alert.alert(
              'Unable to remove exercise',
              error.response?.data?.error || 'Please try again.'
            );
          } finally {
            setBusy(false);
          }
        },
      },
    ]
  );
}


  function handleFinishWorkout() {
    if (workout?.workout.status !== 'IN_PROGRESS') {
  return;
}
    Alert.alert(
      'Finish workout?',
      'Your completed sets will be saved to workout history.',
      [
        { text: 'Keep training', style: 'cancel' },
        {
          text: 'Finish',
          onPress: async () => {
            try {
              setBusy(true);
              await finishWorkout(id);

              Alert.alert('Workout completed!', 'Great work. Keep it consistent.', [
                {
                  text: 'Done',
                  onPress: () => router.replace('/(tabs)'),
                },
              ]);
            } catch (error: any) {
              Alert.alert(
                'Unable to finish workout',
                error.response?.data?.error || 'Please try again.'
              );
            } finally {
              setBusy(false);
            }
          },
        },
      ]
    );
  }

  const filteredExercises = exercises.filter((exercise) =>
    exercise.name.toLowerCase().includes(search.toLowerCase())
  );

  if (loading || !workout) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.screen}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.topBar}>
  <Pressable onPress={() => router.back()}>
    <Text style={styles.backText}>‹ Back</Text>
  </Pressable>

  <Text style={styles.topTitle}>
    {isCompleted ? 'WORKOUT DETAILS' : 'ACTIVE WORKOUT'}
  </Text>

  {!isCompleted ? (
    <Pressable onPress={handleFinishWorkout}>
      <Text style={styles.finishText}>FINISH</Text>
    </Pressable>
  ) : (
    <View style={styles.readOnlyBadge}>
      <Text style={styles.readOnlyText}>COMPLETED</Text>
    </View>
  )}
</View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
      >
        <Text style={styles.heading}>
  {isCompleted ? 'Workout completed.' : "Let's get to work."}
</Text>

<Text style={styles.subtitle}>
  {isCompleted
    ? 'Review your performance from this session.'
    : 'Log your sets and track your progress.'}
</Text>

        <View style={styles.summaryCard}>
          <View>
            <Text style={styles.summaryLabel}>EXERCISES</Text>
            <Text style={styles.summaryValue}>
              {workout.exercises.length}
            </Text>
          </View>

          <View>
            <Text style={styles.summaryLabel}>TOTAL SETS</Text>
            <Text style={styles.summaryValue}>
              {workout.exercises.reduce(
                (total, item) => total + item.sets.length,
                0
              )}
            </Text>
          </View>
        </View>

        {workout.exercises.map((item) => {
          const exercise = item.exercise;
          const previousSets = previous[exercise.exerciseId] || [];

          return (
            <View key={exercise.id} style={styles.exerciseCard}>
              <View style={styles.exerciseHeader}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.exerciseName}>
                    {exercise.exerciseName}
                  </Text>

                  <Text style={styles.exerciseSub}>
                    {item.sets.length} sets
                  </Text>
                </View>

                {!isCompleted && (
  <Pressable
    onPress={() => handleDeleteExercise(exercise.id)}
  >
    <Text style={styles.removeText}>REMOVE</Text>
  </Pressable>
)}
              </View>

              {previousSets.length > 0 && (
                <View style={styles.previousCard}>
                  <Text style={styles.previousTitle}>
                    LAST PERFORMANCE
                  </Text>

                  <Text style={styles.previousValue}>
                    {previousSets
                      .map((set) =>
                        `${set.weightKg ?? '-'} kg × ${set.reps ?? '-'}`
                      )
                      .join('   ·   ')}
                  </Text>
                </View>
              )}

              <View style={styles.setHeader}>
                <Text style={[styles.column, { flex: 0.7 }]}>SET</Text>
                <Text style={styles.column}>KG</Text>
                <Text style={styles.column}>REPS</Text>
                <Text style={[styles.column, { flex: 0.7 }]}>DONE</Text>
              </View>

              {item.sets.map((set) => {
                const values = setValues[set.id] || {
                  weight: '',
                  reps: '',
                };

                return (
                  <View key={set.id} style={styles.setRow}>
                    <View style={[styles.setNumber, { flex: 0.7 }]}>
                      <Text style={styles.setNumberText}>
                        {set.setNumber}
                      </Text>
                    </View>

                    <TextInput
                      style={[
                        styles.setInput,
                        set.completed && styles.completedInput,
                      ]}
                      value={values.weight}
                      onChangeText={(value) =>
                        setSetValues((current) => ({
                          ...current,
                          [set.id]: { ...values, weight: value },
                        }))
                      }
                      keyboardType="decimal-pad"
                      placeholder="0"
                      placeholderTextColor="#666"
                      editable={!isCompleted && !set.completed}
                    />

                    <TextInput
                      style={[
                        styles.setInput,
                        set.completed && styles.completedInput,
                      ]}
                      value={values.reps}
                      onChangeText={(value) =>
                        setSetValues((current) => ({
                          ...current,
                          [set.id]: { ...values, reps: value },
                        }))
                      }
                      keyboardType="number-pad"
                      placeholder="0"
                      placeholderTextColor="#666"
                      editable={!isCompleted && !set.completed}
                    />

                    <Pressable
                      style={[
                        styles.checkButton,
                        set.completed && styles.checkedButton,
                      ]}
                      disabled={isCompleted || busy}
                      onPress={() => handleCompletion(exercise.id, set)}
                    >
                      <Text style={styles.checkText}>
                        {set.completed ? '✓' : ''}
                      </Text>
                    </Pressable>

                    {!isCompleted && (
                      <>
                        <Pressable
                          onPress={() => handleSaveSet(exercise.id, set)}
                          style={styles.saveSetButton}
                          disabled={busy || set.completed}
                        >
                          <Text style={styles.saveSetText}>SAVE</Text>
                        </Pressable>

                        <Pressable
                          onPress={() => handleDeleteSet(exercise.id, set.id)}
                          style={styles.deleteSetButton}
                          disabled={busy}
                        >
                          <Text style={styles.deleteSetText}>×</Text>
                        </Pressable>
                      </>
                    )}

                  </View>
                );
              })}

              {!isCompleted && (
  <Pressable
    style={styles.addSetButton}
    onPress={() => handleAddSet(exercise.id)}
    disabled={busy}
  >
    <Text style={styles.addSetText}>+ ADD SET</Text>
  </Pressable>
)}
            </View>
          );
        })}

        {!isCompleted && (
  <>
    <Pressable
      style={styles.addExerciseButton}
      onPress={() => setExerciseModal(true)}
    >
      <Text style={styles.addExerciseText}>+ ADD EXERCISE</Text>
    </Pressable>

    <Pressable
      style={styles.finishButton}
      onPress={handleFinishWorkout}
      disabled={busy}
    >
      <Text style={styles.finishButtonText}>FINISH WORKOUT</Text>
    </Pressable>
  </>
)}
      </ScrollView>

      <Modal
        visible={exerciseModal && !isCompleted}
        animationType="slide"
        onRequestClose={() => setExerciseModal(false)}
      >
        <View style={styles.modalScreen}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Add Exercise</Text>

            <Pressable onPress={() => setExerciseModal(false)}>
              <Text style={styles.closeText}>CLOSE</Text>
            </Pressable>
          </View>

          <TextInput
            style={styles.searchInput}
            placeholder="Search exercises..."
            placeholderTextColor="#777"
            value={search}
            onChangeText={setSearch}
            autoCapitalize="none"
          />

          <ScrollView keyboardShouldPersistTaps="handled">
            {filteredExercises.map((exercise) => {
              const alreadyAdded = workout.exercises.some(
                (item) => item.exercise.exerciseId === exercise.id
              );

              return (
                <Pressable
                  key={exercise.id}
                  style={styles.exerciseOption}
                  disabled={alreadyAdded || busy}
                  onPress={() => handleAddExercise(exercise)}
                >
                  <View style={{ flex: 1 }}>
                    <Text style={styles.optionName}>
                      {exercise.name}
                    </Text>

                    <Text style={styles.optionSub}>
                      {exercise.muscleGroup || 'Exercise'}
                      {exercise.equipment
                        ? ` · ${exercise.equipment}`
                        : ''}
                    </Text>
                  </View>

                  <Text style={styles.optionAction}>
                    {alreadyAdded ? 'ADDED' : '+ ADD'}
                  </Text>
                </Pressable>
              );
            })}

            {filteredExercises.length === 0 && (
              <Text style={styles.noResults}>No exercises found.</Text>
            )}
          </ScrollView>
        </View>
      </Modal>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.background },
  loading: {
    flex: 1,
    backgroundColor: colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
  topBar: {
    height: 58,
    paddingHorizontal: 18,
    paddingTop: 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.surfaceSubtle,
  },
  backText: { color: colors.primary, fontSize: 15, fontWeight: '600' },
  topTitle: { color: colors.text, fontSize: 11, fontWeight: '900' },
  finishText: { color: colors.primary, fontSize: 12, fontWeight: '900' },
  scroll: { flex: 1 },
  content: { padding: 18, paddingBottom: 45 },
  heading: { color: colors.text, fontSize: 25, fontWeight: '900' },
  subtitle: { color: colors.textSecondary, fontSize: 13, marginTop: 7 },
  summaryCard: {
    backgroundColor: colors.surface,
    borderRadius: 13,
    padding: 18,
    marginTop: 22,
    marginBottom: 22,
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  summaryLabel: {
    color: colors.textSecondary,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
  },
  summaryValue: {
    color: colors.primary,
    fontSize: 23,
    fontWeight: '900',
    marginTop: 7,
  },
  exerciseCard: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 15,
    marginBottom: 16,
  },
  exerciseHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  exerciseName: { color: colors.text, fontSize: 17, fontWeight: '800' },
  exerciseSub: { color: colors.textSecondary, fontSize: 11, marginTop: 5 },
  removeText: {
    color: colors.danger,
    fontSize: 10,
    fontWeight: '800',
    padding: 8,
  },
  previousCard: {
    backgroundColor: colors.surfaceBlue,
    padding: 11,
    borderRadius: 9,
    marginBottom: 15,
  },
  previousTitle: {
    color: colors.primary,
    fontSize: 9,
    fontWeight: '900',
    letterSpacing: 1,
  },
  previousValue: { color: '#DCEBFF', fontSize: 11, marginTop: 7 },
  setHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  column: {
    flex: 1,
    color: colors.textSecondary,
    textAlign: 'center',
    fontSize: 10,
    fontWeight: '800',
  },
  setRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 9,
    gap: 5,
  },
  setNumber: { alignItems: 'center', justifyContent: 'center' },
  setNumberText: { color: colors.primary, fontWeight: '800', fontSize: 13 },
  setInput: {
    flex: 1,
    height: 39,
    backgroundColor: colors.surfaceSubtle,
    borderRadius: 7,
    color: colors.text,
    textAlign: 'center',
    fontSize: 13,
    paddingHorizontal: 2,
  },
  completedInput: { opacity: 0.65 },
  checkButton: {
    flex: 0.7,
    height: 34,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#555',
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkedButton: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  checkText: { color: colors.background, fontWeight: '900', fontSize: 16 },
  saveSetButton: { paddingHorizontal: 5, paddingVertical: 8 },
  saveSetText: { color: colors.primary, fontSize: 9, fontWeight: '900' },
  deleteSetButton: { paddingHorizontal: 4, paddingVertical: 5 },
  deleteSetText: { color: colors.danger, fontSize: 19 },
  addSetButton: {
    borderTopWidth: 1,
    borderTopColor: '#333',
    marginTop: 6,
    paddingTop: 14,
    alignItems: 'center',
  },
  addSetText: { color: colors.primary, fontSize: 11, fontWeight: '900' },
  addExerciseButton: {
    borderWidth: 1,
    borderColor: colors.primary,
    borderRadius: 11,
    padding: 16,
    alignItems: 'center',
    marginTop: 5,
  },
  addExerciseText: { color: colors.primary, fontSize: 12, fontWeight: '900' },
  finishButton: {
    backgroundColor: colors.primary,
    borderRadius: 11,
    padding: 17,
    alignItems: 'center',
    marginTop: 15,
  },
  finishButtonText: { color: colors.background, fontSize: 13, fontWeight: '900' },
  modalScreen: {
    flex: 1,
    backgroundColor: colors.background,
    paddingTop: 55,
    paddingHorizontal: 20,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  modalTitle: { color: colors.text, fontSize: 23, fontWeight: '900' },
  closeText: { color: colors.primary, fontSize: 11, fontWeight: '900' },
  searchInput: {
    backgroundColor: colors.surface,
    borderRadius: 10,
    height: 48,
    paddingHorizontal: 15,
    color: colors.text,
    marginBottom: 15,
  },
  exerciseOption: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: colors.surfaceSubtle,
  },
  optionName: { color: colors.text, fontSize: 14, fontWeight: '700' },
  optionSub: { color: colors.textSecondary, fontSize: 11, marginTop: 5 },
  optionAction: { color: colors.primary, fontSize: 10, fontWeight: '900' },
  noResults: {
    color: colors.textSecondary,
    textAlign: 'center',
    marginTop: 30,
  },
  readOnlyBadge: {
  backgroundColor: '#173326',
  paddingHorizontal: 10,
  paddingVertical: 6,
  borderRadius: 20,
},

readOnlyText: {
  color: '#48C78E',
  fontSize: 9,
  fontWeight: '900',
  letterSpacing: 0.5,
},
});