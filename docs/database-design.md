# FitStud — Database Design

## Core Entities

1. **User** — account details and profile.
2. **Exercise** — exercise name, muscle group, and equipment.
3. **Routine** — a planned workout template.
4. **RoutineExercise** — exercises included in a routine and their order.
5. **Workout** — an actual workout session performed by a user.
6. **WorkoutExercise** — exercises performed during a workout.
7. **WorkoutSet** — set number, weight, repetitions, and completion status.


## Entity Relationships

- One User has many Routines.
- One User has many Workouts.
- One Exercise can appear in many Routines.
- One Routine has many RoutineExercises.
- One RoutineExercise references one Exercise.
- One Workout has many WorkoutExercises.
- One WorkoutExercise references one Exercise.
- One WorkoutExercise has many WorkoutSets.

## Key Rules

- Every user's routines and workouts belong to that user.
- Deleting a routine must not delete completed workout history.
- Workout history stores the actual weight and repetitions performed.
- Routine changes must not modify previously completed workouts.


## Database Fields

### User
- id: UUID, primary key
- name: String
- email: String, unique
- password_hash: String
- created_at: Timestamp

### Exercise
- id: UUID, primary key
- name: String
- muscle_group: String
- equipment: String
- is_custom: Boolean

### Routine
- id: UUID, primary key
- user_id: UUID, foreign key
- name: String
- created_at: Timestamp

### RoutineExercise
- id: UUID, primary key
- routine_id: UUID, foreign key
- exercise_id: UUID, foreign key
- position: Integer
- target_sets: Integer

### Workout
- id: UUID, primary key
- user_id: UUID, foreign key
- routine_id: UUID, nullable foreign key
- started_at: Timestamp
- completed_at: Timestamp, nullable

### WorkoutExercise
- id: UUID, primary key
- workout_id: UUID, foreign key
- exercise_id: UUID, foreign key
- position: Integer

### WorkoutSet
- id: UUID, primary key
- workout_exercise_id: UUID, foreign key
- set_number: Integer
- weight_kg: Decimal
- repetitions: Integer
- completed: Boolean