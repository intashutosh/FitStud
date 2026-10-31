-- 1. Workout sessions
CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,
    source_routine_id UUID,

    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    session_mode VARCHAR(40) NOT NULL DEFAULT 'STANDARD',
    visibility VARCHAR(30) NOT NULL DEFAULT 'PRIVATE',

    media_url TEXT,

    -- Denormalized metrics for workout history and feeds.
    duration_seconds INTEGER,
    total_volume_kg NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_sets INTEGER NOT NULL DEFAULT 0,
    pr_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workouts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_workouts_source_routine
        FOREIGN KEY (source_routine_id)
        REFERENCES routines(id)
        ON DELETE SET NULL,

    CONSTRAINT ck_workouts_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),

    CONSTRAINT ck_workouts_session_mode
        CHECK (
            session_mode IN (
                'STANDARD',
                'TIME_COMPRESSED',
                'LOW_STRESS_MAINTENANCE'
            )
        ),

    CONSTRAINT ck_workouts_visibility
        CHECK (
            visibility IN (
                'PUBLIC',
                'ORGANIZATION_ONLY',
                'FOLLOWERS_ONLY',
                'PRIVATE'
            )
        ),

    CONSTRAINT ck_workouts_finished_at
        CHECK (
            (status = 'IN_PROGRESS' AND finished_at IS NULL)
            OR
            (status = 'COMPLETED' AND finished_at IS NOT NULL)
        ),

    CONSTRAINT ck_workouts_duration
        CHECK (duration_seconds IS NULL OR duration_seconds >= 0),

    CONSTRAINT ck_workouts_total_volume
        CHECK (total_volume_kg >= 0),

    CONSTRAINT ck_workouts_total_sets
        CHECK (total_sets >= 0),

    CONSTRAINT ck_workouts_pr_count
        CHECK (pr_count >= 0)
);

CREATE INDEX idx_workouts_user_started
    ON workouts (user_id, started_at DESC);

CREATE INDEX idx_workouts_user_status
    ON workouts (user_id, status);

CREATE INDEX idx_workouts_source_routine
    ON workouts (source_routine_id);


-- 2. Exercises performed in a workout
CREATE TABLE workout_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    workout_id UUID NOT NULL,
    exercise_id UUID NOT NULL,

    position INTEGER NOT NULL,
    notes VARCHAR(300),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_exercises_workout
        FOREIGN KEY (workout_id)
        REFERENCES workouts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_workout_exercises_exercise
        FOREIGN KEY (exercise_id)
        REFERENCES exercises(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_workout_exercises_position
        CHECK (position >= 0)
);

CREATE UNIQUE INDEX uq_workout_exercises_position
    ON workout_exercises (workout_id, position);

CREATE INDEX idx_workout_exercises_exercise
    ON workout_exercises (exercise_id);


-- 3. Actual sets performed during a workout
CREATE TABLE workout_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    workout_exercise_id UUID NOT NULL,
    set_number INTEGER NOT NULL,

    weight_kg NUMERIC(7, 2),
    reps INTEGER,

    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_sets_workout_exercise
        FOREIGN KEY (workout_exercise_id)
        REFERENCES workout_exercises(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_workout_sets_number
        CHECK (set_number >= 1),

    CONSTRAINT ck_workout_sets_weight
        CHECK (weight_kg IS NULL OR weight_kg >= 0),

    CONSTRAINT ck_workout_sets_reps
        CHECK (reps IS NULL OR reps >= 1),

    CONSTRAINT ck_workout_sets_completion
        CHECK (
            (completed = FALSE AND completed_at IS NULL)
            OR
            (completed = TRUE AND completed_at IS NOT NULL)
        )
);

CREATE UNIQUE INDEX uq_workout_sets_number
    ON workout_sets (workout_exercise_id, set_number);