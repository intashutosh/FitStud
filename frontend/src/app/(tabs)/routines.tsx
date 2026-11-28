import React, { useCallback, useMemo, useState } from 'react';
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
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';

import {
  createRoutineFolder,
  deleteRoutineFolder,
  getRoutineFolders,
  moveRoutineToFolder,
  removeRoutineFromFolder,
  renameRoutineFolder,
} from '@/api/routine-folder.api';
import {
  createRoutine,
  deleteRoutine,
  getMyRoutines,
} from '@/api/routine.api';
import {
  getOngoingWorkouts,
  startBlankWorkout,
  startWorkoutFromRoutine,
} from '@/api/workout.api';

import { RoutineFolderResponse } from '@/types/routine-folder';
import { RoutineResponse } from '@/types/routine';
import { WorkoutResponse } from '@/types/workout';

const C = {
  background: '#101010',
  surface: '#1B1B1B',
  elevated: '#242424',
  primary: '#579BFF',
  primaryMuted: 'rgba(108, 92, 231, 0.25)',
  text: '#FFFFFF',
  secondary: '#929292',
  muted: '#666666',
  border: '#303030',
  danger: '#E47777',
};

export default function RoutinesScreen() {
  const router = useRouter();

  // Data state
  const [routines, setRoutines] = useState<RoutineResponse[]>([]);
  const [ongoing, setOngoing] = useState<WorkoutResponse[]>([]);
  const [folders, setFolders] = useState<RoutineFolderResponse[]>([]);
  const [selectedFolderId, setSelectedFolderId] = useState<string>('MY_ROUTINES');

  // Loading & workout start state
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [starting, setStarting] = useState(false);
  const [startingId, setStartingId] = useState<string | null>(null);

  // Routine modal & search state
  const [modalVisible, setModalVisible] = useState(false);
  const [creating, setCreating] = useState(false);
  const [routineName, setRoutineName] = useState('');
  const [description, setDescription] = useState('');
  const [search, setSearch] = useState('');

  // Folder modal state
  const [folderModalVisible, setFolderModalVisible] = useState(false);
  const [folderName, setFolderName] = useState('');
  const [editingFolder, setEditingFolder] = useState<RoutineFolderResponse | null>(null);
  const [savingFolder, setSavingFolder] = useState(false);

  // Data fetching
  const loadData = useCallback(async () => {
    try {
      const [routineData, ongoingData, folderData] = await Promise.all([
        getMyRoutines(),
        getOngoingWorkouts(),
        getRoutineFolders(),
      ]);

      setRoutines(routineData);
      setOngoing(ongoingData);
      setFolders(folderData);
    } catch (error: any) {
      console.error(
        'Workout screen loading failed:',
        error?.response?.data ?? error?.message
      );

      Alert.alert(
        'Unable to load workouts',
        'Please check your connection and try again.'
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      loadData();
    }, [loadData])
  );

  const filteredRoutines = useMemo(() => {
    let result = routines;

    if (selectedFolderId !== 'MY_ROUTINES') {
      result = result.filter(
        (routine) => String(routine.folderId) === String(selectedFolderId)
      );
    }

    const query = search.trim().toLowerCase();

    if (query) {
      result = result.filter((routine) =>
        routine.name.toLowerCase().includes(query)
      );
    }

    return result;
  }, [routines, search, selectedFolderId]);

  const folderChips = useMemo(
    () => [
      {
        id: 'MY_ROUTINES',
        name: 'My Routines',
        routineCount: routines.length,
      },
      ...folders.map((folder) => ({
        id: folder.id,
        name: folder.name,
        routineCount: folder.routineCount,
      })),
    ],
    [folders, routines]
  );

  // Workout handlers
  const handleStartEmpty = async () => {
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
        error?.response?.data?.error || 'Please try again.'
      );
    } finally {
      setStarting(false);
    }
  };

  const handleResume = (workoutId: string) => {
    router.push({
      pathname: '/workout/[id]',
      params: { id: workoutId },
    });
  };

  const handleStartRoutine = async (routine: RoutineResponse) => {
    try {
      setStartingId(routine.id);
      const workout = await startWorkoutFromRoutine(routine.id);
      router.push({
        pathname: '/workout/[id]',
        params: { id: workout.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not start routine',
        error?.response?.data?.error || 'Please try again.'
      );
    } finally {
      setStartingId(null);
    }
  };

  // Routine handlers
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

      await loadData();

      router.push({
        pathname: '/routine/[id]',
        params: { id: created.id },
      });
    } catch (error: any) {
      Alert.alert(
        'Could not create routine',
        error?.response?.data?.error || 'Please try again.'
      );
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = (routine: RoutineResponse) => {
    Alert.alert(
      'Delete routine?',
      `Are you sure you want to delete "${routine.name}"? Completed workout history will not be deleted.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteRoutine(routine.id);
              setRoutines((current) =>
                current.filter((item) => item.id !== routine.id)
              );
            } catch {
              Alert.alert('Error', 'Could not delete this routine.');
            }
          },
        },
      ]
    );
  };

  const openRoutine = (routine: RoutineResponse) => {
    router.push({
      pathname: '/routine/[id]',
      params: { id: routine.id },
    });
  };

  const handleMoveRoutine = (routine: RoutineResponse) => {
    const options = folders.map((folder) => ({
      text: `${folder.name} (${folder.routineCount})`,
      onPress: async () => {
        try {
          await moveRoutineToFolder(routine.id, { folderId: folder.id });
          await loadData();
        } catch {
          Alert.alert('Error', 'Could not move this routine.');
        }
      },
    }));

    if (routine.folderId) {
      options.push({
        text: 'Remove from folder',
        onPress: async () => {
          try {
            await removeRoutineFromFolder(routine.id);
            await loadData();
          } catch {
            Alert.alert('Error', 'Could not remove this routine from its folder.');
          }
        },
      });
    }

    if (options.length === 0) {
      Alert.alert(
        'No folders',
        'Create a folder first to organize your routines.'
      );
      return;
    }

    Alert.alert('Organize routine', routine.name, [
      ...options,
      { text: 'Cancel', style: 'cancel' },
    ]);
  };

  const showRoutineOptions = (routine: RoutineResponse) => {
    Alert.alert(routine.name, 'Manage this routine', [
      {
        text: 'Move to folder',
        onPress: () => handleMoveRoutine(routine),
      },
      {
        text: 'Delete routine',
        style: 'destructive',
        onPress: () => handleDelete(routine),
      },
      { text: 'Cancel', style: 'cancel' },
    ]);
  };

  // Folder handlers
  const openCreateFolder = () => {
    setEditingFolder(null);
    setFolderName('');
    setFolderModalVisible(true);
  };

  const openRenameFolder = (folder: RoutineFolderResponse) => {
    setEditingFolder(folder);
    setFolderName(folder.name);
    setFolderModalVisible(true);
  };

  const handleSaveFolder = async () => {
    const name = folderName.trim();

    if (!name) {
      Alert.alert('Name required', 'Enter a folder name.');
      return;
    }

    try {
      setSavingFolder(true);

      if (editingFolder) {
        await renameRoutineFolder(editingFolder.id, { name });
      } else {
        await createRoutineFolder({ name });
      }

      setFolderModalVisible(false);
      setFolderName('');
      setEditingFolder(null);

      await loadData();
    } catch (error: any) {
      Alert.alert(
        'Could not save folder',
        error?.response?.data?.message ||
          error?.response?.data?.error ||
          'A folder with this name may already exist.'
      );
    } finally {
      setSavingFolder(false);
    }
  };

  const handleDeleteFolder = (folder: RoutineFolderResponse) => {
    Alert.alert(
      'Delete folder?',
      `Delete "${folder.name}"? Your routines will be preserved in My Routines.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteRoutineFolder(folder.id);

              if (selectedFolderId === folder.id) {
                setSelectedFolderId('MY_ROUTINES');
              }

              await loadData();
            } catch {
              Alert.alert('Error', 'Could not delete this folder.');
            }
          },
        },
      ]
    );
  };

  const showFolderOptions = (folder: RoutineFolderResponse) => {
    Alert.alert(folder.name, 'Manage this folder', [
      { text: 'Rename', onPress: () => openRenameFolder(folder) },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: () => handleDeleteFolder(folder),
      },
      { text: 'Cancel', style: 'cancel' },
    ]);
  };

  // Renderers
  const renderRoutine = ({
    item,
    index,
  }: {
    item: RoutineResponse;
    index: number;
  }) => (
    <Pressable style={styles.routineCard} onPress={() => openRoutine(item)}>
      <View style={styles.routineTop}>
        <View style={styles.routineNumber}>
          <Text style={styles.routineNumberText}>
            {String(index + 1).padStart(2, '0')}
          </Text>
        </View>

        <Pressable
          hitSlop={12}
          onPress={() => showRoutineOptions(item)}
          style={styles.moreButton}
        >
          <Text style={styles.moreText}>•••</Text>
        </Pressable>
      </View>

      <Text style={styles.routineName}>{item.name}</Text>

      <Text style={styles.routineDescription} numberOfLines={2}>
        {item.description || 'Your personal workout template'}
      </Text>

      <View style={styles.cardDivider} />

      <View style={styles.routineBottom}>
        <Pressable onPress={() => openRoutine(item)} style={styles.editButton}>
          <Text style={styles.editButtonText}>EDIT ROUTINE</Text>
        </Pressable>

        <Pressable
          style={[
            styles.startRoutineButton,
            startingId !== null && styles.disabled,
          ]}
          disabled={startingId !== null || starting}
          onPress={() => handleStartRoutine(item)}
        >
          {startingId === item.id ? (
            <ActivityIndicator size="small" color="#FFFFFF" />
          ) : (
            <Text style={styles.startRoutineText}>START</Text>
          )}
        </Pressable>
      </View>
    </Pressable>
  );

  if (loading) {
    return (
      <SafeAreaView style={styles.loadingContainer} edges={['top']}>
        <ActivityIndicator color={C.primary} size="large" />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Workout</Text>
        <View style={styles.headerRight}>
          <View style={styles.proBadge}>
            <Text style={styles.proText}>FITSTUD</Text>
          </View>
        </View>
      </View>

      {/* Workout sections */}
      <View style={styles.sectionTabs}>
        <View style={styles.activeTab}>
          <Text style={styles.activeTabText}>Routines</Text>
        </View>

        <Pressable
          style={styles.inactiveTab}
          onPress={() =>
            Alert.alert(
              'Coming soon',
              'Trainer features are planned for a future update.'
            )
          }
        >
          <Text style={styles.inactiveTabText}>Trainer</Text>
        </Pressable>

        <Pressable
          style={styles.inactiveTab}
          onPress={() =>
            Alert.alert(
              'Coming soon',
              'Exercise discovery is planned for a future update.'
            )
          }
        >
          <Text style={styles.inactiveTabText}>Explore</Text>
        </Pressable>
      </View>

      <FlatList
        data={filteredRoutines}
        keyExtractor={(item) => item.id}
        renderItem={renderRoutine}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => {
              setRefreshing(true);
              loadData();
            }}
            tintColor={C.primary}
          />
        }
        ListHeaderComponent={
          <View>
            {/* Start empty workout */}
            <Pressable
              style={[styles.emptyWorkoutButton, starting && styles.disabled]}
              onPress={handleStartEmpty}
              disabled={starting}
            >
              {starting ? (
                <ActivityIndicator color={C.text} />
              ) : (
                <>
                  <Text style={styles.plusIcon}>＋</Text>
                  <Text style={styles.emptyWorkoutText}>
                    Start Empty Workout
                  </Text>
                </>
              )}
            </Pressable>

            {/* Ongoing workouts */}
            {ongoing.length > 0 && (
              <View style={styles.ongoingSection}>
                <Text style={styles.sectionHeading}>IN PROGRESS</Text>

                {ongoing.map((workout) => (
                  <Pressable
                    key={workout.id}
                    style={styles.ongoingCard}
                    onPress={() => handleResume(workout.id)}
                  >
                    <View style={styles.ongoingLeft}>
                      <View style={styles.liveDot} />
                      <View style={{ flex: 1 }}>
                        <Text style={styles.ongoingTitle}>
                          {workout.sourceRoutineId
                            ? 'Routine workout'
                            : 'Empty workout'}
                        </Text>
                        <Text style={styles.ongoingSubtitle}>
                          Your workout is waiting
                        </Text>
                      </View>
                    </View>

                    <Text style={styles.resumeText}>RESUME →</Text>
                  </Pressable>
                ))}
              </View>
            )}

            {/* Routine heading & actions */}
            <View style={styles.routineHeading}>
              <View>
                <Text style={styles.sectionHeading}>Routines</Text>
                <Text style={styles.routineCount}>
                  {routines.length}{' '}
                  {routines.length === 1 ? 'routine' : 'routines'}
                </Text>
              </View>

              <View style={styles.headingActions}>
                <Pressable
                  style={styles.folderButton}
                  onPress={openCreateFolder}
                >
                  <Text style={styles.folderButtonText}>＋ Folder</Text>
                </Pressable>

                <Pressable
                  style={styles.addButton}
                  onPress={() => setModalVisible(true)}
                >
                  <Text style={styles.addButtonText}>＋</Text>
                </Pressable>
              </View>
            </View>

            {/* Folder filter chips */}
            <FlatList
              horizontal
              data={folderChips}
              keyExtractor={(item) => item.id}
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.folderList}
              renderItem={({ item }) => {
                const isSelected = selectedFolderId === item.id;

                return (
                  <Pressable
                    style={[
                      styles.folderChip,
                      isSelected && styles.folderChipActive,
                    ]}
                    onPress={() => setSelectedFolderId(item.id)}
                    onLongPress={() => {
                      const folder = folders.find((f) => f.id === item.id);
                      if (folder) showFolderOptions(folder);
                    }}
                  >
                    <Text
                      style={[
                        styles.folderChipText,
                        isSelected && styles.folderChipTextActive,
                      ]}
                    >
                      {item.name}
                    </Text>

                    <Text
                      style={[
                        styles.folderChipCount,
                        isSelected && styles.folderChipTextActive,
                      ]}
                    >
                      {item.routineCount}
                    </Text>
                  </Pressable>
                );
              }}
            />

            {/* Search */}
            {routines.length > 0 && (
              <View style={styles.searchBox}>
                <Text style={styles.searchIcon}>⌕</Text>
                <TextInput
                  value={search}
                  onChangeText={setSearch}
                  placeholder="Search routines"
                  placeholderTextColor={C.muted}
                  style={styles.searchInput}
                  autoCapitalize="none"
                />
              </View>
            )}

            {/* Empty states */}
            {routines.length === 0 && (
              <View style={styles.emptyState}>
                <Text style={styles.emptyStateIcon}>▤</Text>
                <Text style={styles.emptyStateTitle}>No routines yet</Text>
                <Text style={styles.emptyStateDescription}>
                  Create workout templates for your training days and start your
                  next session in seconds.
                </Text>
                <Pressable
                  style={styles.createFirstButton}
                  onPress={() => setModalVisible(true)}
                >
                  <Text style={styles.createFirstText}>＋ CREATE ROUTINE</Text>
                </Pressable>
              </View>
            )}

            {routines.length > 0 && filteredRoutines.length === 0 && (
              <Text style={styles.noSearchResults}>
                No routines match your search or folder filter.
              </Text>
            )}
          </View>
        }
        ListFooterComponent={
          routines.length > 0 ? (
            <Text style={styles.footerText}>
              BUILD YOUR ROUTINE. SHOW UP. GET STRONGER.
            </Text>
          ) : undefined
        }
      />

      {/* Create routine modal */}
      <Modal
        visible={modalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>New Routine</Text>
              <Pressable onPress={() => setModalVisible(false)}>
                <Text style={styles.closeButton}>✕</Text>
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>ROUTINE NAME</Text>
            <TextInput
              value={routineName}
              onChangeText={setRoutineName}
              placeholder="e.g. Push Day"
              placeholderTextColor={C.muted}
              style={styles.input}
              maxLength={100}
            />

            <Text style={styles.inputLabel}>DESCRIPTION (OPTIONAL)</Text>
            <TextInput
              value={description}
              onChangeText={setDescription}
              placeholder="Describe your training routine..."
              placeholderTextColor={C.muted}
              style={[styles.input, styles.descriptionInput]}
              multiline
              maxLength={500}
            />

            <Pressable
              style={[styles.createButton, creating && styles.disabled]}
              disabled={creating}
              onPress={handleCreate}
            >
              {creating ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.createButtonText}>CREATE ROUTINE</Text>
              )}
            </Pressable>
          </View>
        </View>
      </Modal>

      {/* Create / Rename folder modal */}
      <Modal
        visible={folderModalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setFolderModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>
                {editingFolder ? 'Rename Folder' : 'New Folder'}
              </Text>
              <Pressable onPress={() => setFolderModalVisible(false)}>
                <Text style={styles.closeButton}>✕</Text>
              </Pressable>
            </View>

            <Text style={styles.inputLabel}>FOLDER NAME</Text>
            <TextInput
              value={folderName}
              onChangeText={setFolderName}
              placeholder="e.g. Push Pull Legs"
              placeholderTextColor={C.muted}
              style={styles.input}
              maxLength={100}
              autoFocus
            />

            <Pressable
              style={[styles.createButton, savingFolder && styles.disabled]}
              disabled={savingFolder}
              onPress={handleSaveFolder}
            >
              {savingFolder ? (
                <ActivityIndicator color="#FFFFFF" />
              ) : (
                <Text style={styles.createButtonText}>
                  {editingFolder ? 'SAVE CHANGES' : 'CREATE FOLDER'}
                </Text>
              )}
            </Pressable>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  // Layout
  container: {
    flex: 1,
    backgroundColor: C.background,
  },
  loadingContainer: {
    flex: 1,
    backgroundColor: C.background,
    alignItems: 'center',
    justifyContent: 'center',
  },
  listContent: {
    paddingHorizontal: 16,
    paddingTop: 17,
    paddingBottom: 35,
  },
  disabled: {
    opacity: 0.55,
  },

  // Header & tabs
  header: {
    paddingHorizontal: 20,
    paddingTop: 14,
    paddingBottom: 17,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerTitle: {
    color: C.text,
    fontSize: 28,
    fontWeight: '900',
  },
  headerRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  proBadge: {
    backgroundColor: '#E8C547',
    borderRadius: 6,
    paddingHorizontal: 9,
    paddingVertical: 5,
  },
  proText: {
    color: '#101010',
    fontSize: 11,
    fontWeight: '900',
    letterSpacing: 1,
  },
  sectionTabs: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: C.border,
    paddingHorizontal: 20,
  },
  activeTab: {
    paddingHorizontal: 14,
    paddingBottom: 12,
    borderBottomWidth: 2,
    borderBottomColor: C.primary,
    marginRight: 24,
  },
  activeTabText: {
    color: C.text,
    fontSize: 15,
    fontWeight: '800',
  },
  inactiveTab: {
    paddingHorizontal: 8,
    paddingBottom: 12,
    marginRight: 20,
  },
  inactiveTabText: {
    color: C.secondary,
    fontSize: 15,
    fontWeight: '600',
  },

  // Empty workout & ongoing
  emptyWorkoutButton: {
    height: 52,
    backgroundColor: C.surface,
    borderRadius: 10,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 25,
  },
  plusIcon: {
    color: C.text,
    fontSize: 24,
    marginRight: 12,
  },
  emptyWorkoutText: {
    color: C.text,
    fontSize: 15,
    fontWeight: '600',
  },
  ongoingSection: {
    marginBottom: 25,
  },
  sectionHeading: {
    color: C.text,
    fontSize: 14,
    fontWeight: '900',
    letterSpacing: 1,
  },
  ongoingCard: {
    backgroundColor: C.surface,
    borderWidth: 1,
    borderColor: C.primaryMuted,
    borderRadius: 11,
    padding: 15,
    marginTop: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  ongoingLeft: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  liveDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#48C78E',
    marginRight: 11,
  },
  ongoingTitle: {
    color: C.text,
    fontSize: 15,
    fontWeight: '700',
  },
  ongoingSubtitle: {
    color: C.secondary,
    fontSize: 13,
    marginTop: 4,
  },
  resumeText: {
    color: C.primary,
    fontSize: 12,
    fontWeight: '900',
    marginLeft: 8,
  },

  // Routines header & folder chips
  routineHeading: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  routineCount: {
    color: C.secondary,
    fontSize: 13,
    marginTop: 4,
  },
  headingActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  folderButton: {
    height: 38,
    backgroundColor: C.surface,
    borderWidth: 1,
    borderColor: C.border,
    borderRadius: 9,
    paddingHorizontal: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  folderButtonText: {
    color: C.text,
    fontSize: 13,
    fontWeight: '700',
  },
  addButton: {
    width: 38,
    height: 38,
    backgroundColor: C.primary,
    borderRadius: 9,
    alignItems: 'center',
    justifyContent: 'center',
  },
  addButtonText: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '700',
  },
  folderList: {
    paddingBottom: 14,
    gap: 9,
  },
  folderChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: C.surface,
    borderWidth: 1,
    borderColor: C.border,
    borderRadius: 9,
    paddingHorizontal: 13,
    paddingVertical: 9,
    gap: 8,
  },
  folderChipActive: {
    backgroundColor: 'rgba(87, 155, 255, 0.16)',
    borderColor: C.primary,
  },
  folderChipText: {
    color: C.secondary,
    fontSize: 13,
    fontWeight: '700',
  },
  folderChipTextActive: {
    color: C.primary,
  },
  folderChipCount: {
    color: C.muted,
    fontSize: 12,
    fontWeight: '700',
  },

  // Search
  searchBox: {
    height: 45,
    backgroundColor: C.surface,
    borderRadius: 9,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    marginBottom: 14,
  },
  searchIcon: {
    color: C.secondary,
    fontSize: 23,
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    color: C.text,
    fontSize: 15,
    paddingVertical: 8,
  },

  // Routine cards
  routineCard: {
    backgroundColor: C.surface,
    borderRadius: 12,
    padding: 15,
    marginBottom: 12,
  },
  routineTop: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  routineNumber: {
    width: 34,
    height: 34,
    borderRadius: 8,
    backgroundColor: C.elevated,
    alignItems: 'center',
    justifyContent: 'center',
  },
  routineNumberText: {
    color: C.primary,
    fontSize: 13,
    fontWeight: '900',
  },
  moreButton: {
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  moreText: {
    color: C.secondary,
    fontSize: 18,
    fontWeight: '900',
  },
  routineName: {
    color: C.text,
    fontSize: 19,
    fontWeight: '800',
    marginTop: 12,
  },
  routineDescription: {
    color: C.secondary,
    fontSize: 14,
    lineHeight: 20,
    marginTop: 5,
  },
  cardDivider: {
    height: 1,
    backgroundColor: C.border,
    marginTop: 15,
    marginBottom: 12,
  },
  routineBottom: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  editButton: {
    paddingVertical: 9,
    paddingHorizontal: 3,
  },
  editButtonText: {
    color: C.secondary,
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 0.7,
  },
  startRoutineButton: {
    backgroundColor: '#0788F9',
    borderRadius: 8,
    minWidth: 105,
    paddingVertical: 10,
    alignItems: 'center',
  },
  startRoutineText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '900',
    letterSpacing: 0.5,
  },

  // Empty & footer states
  emptyState: {
    alignItems: 'center',
    paddingHorizontal: 25,
    paddingVertical: 55,
  },
  emptyStateIcon: {
    color: C.primary,
    fontSize: 45,
    marginBottom: 15,
  },
  emptyStateTitle: {
    color: C.text,
    fontSize: 20,
    fontWeight: '800',
  },
  emptyStateDescription: {
    color: C.secondary,
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 21,
    marginTop: 9,
  },
  createFirstButton: {
    backgroundColor: C.primary,
    borderRadius: 9,
    paddingHorizontal: 20,
    paddingVertical: 13,
    marginTop: 20,
  },
  createFirstText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '900',
  },
  noSearchResults: {
    color: C.secondary,
    textAlign: 'center',
    paddingVertical: 35,
    fontSize: 15,
  },
  footerText: {
    color: C.muted,
    textAlign: 'center',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1.2,
    marginTop: 20,
    marginBottom: 10,
  },

  // Modals
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: C.surface,
    borderTopLeftRadius: 22,
    borderTopRightRadius: 22,
    padding: 22,
    paddingBottom: 35,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  modalTitle: {
    color: C.text,
    fontSize: 22,
    fontWeight: '900',
  },
  closeButton: {
    color: C.secondary,
    fontSize: 22,
    padding: 5,
  },
  inputLabel: {
    color: C.secondary,
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 1,
    marginTop: 13,
    marginBottom: 9,
  },
  input: {
    backgroundColor: C.elevated,
    borderWidth: 1,
    borderColor: C.border,
    borderRadius: 10,
    color: C.text,
    fontSize: 16,
    paddingHorizontal: 13,
    paddingVertical: 13,
  },
  descriptionInput: {
    minHeight: 85,
    textAlignVertical: 'top',
  },
  createButton: {
    backgroundColor: C.primary,
    borderRadius: 10,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 23,
  },
  createButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '900',
    letterSpacing: 0.8,
  },
});