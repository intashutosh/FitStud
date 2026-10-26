-- 1. Exercise catalog: global exercises and user-created exercises
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    muscle_group VARCHAR(50),
    equipment VARCHAR(50),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exercises_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_exercises_name_not_blank
        CHECK (LENGTH(BTRIM(name)) > 0)
);

-- Global exercise names must be unique, ignoring case.
CREATE UNIQUE INDEX uq_exercises_global_name
    ON exercises (LOWER(name))
    WHERE owner_id IS NULL;

-- A user cannot create duplicate custom exercises, ignoring case.
CREATE UNIQUE INDEX uq_exercises_owner_name
    ON exercises (owner_id, LOWER(name))
    WHERE owner_id IS NOT NULL;

CREATE INDEX idx_exercises_owner_id
    ON exercises (owner_id);


-- 2. Routine templates created by users
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_routines_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_routines_name_not_blank
        CHECK (LENGTH(BTRIM(name)) > 0)
);

CREATE INDEX idx_routines_user_id
    ON routines (user_id);

CREATE INDEX idx_routines_user_created
    ON routines (user_id, created_at DESC);


-- 3. Exercises belonging to a routine, with explicit ordering
CREATE TABLE routine_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    routine_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    position INTEGER NOT NULL,
    notes VARCHAR(300),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_routine_exercises_routine
        FOREIGN KEY (routine_id)
        REFERENCES routines(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_routine_exercises_exercise
        FOREIGN KEY (exercise_id)
        REFERENCES exercises(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_routine_exercises_position
        CHECK (position >= 0)
);

CREATE UNIQUE INDEX uq_routine_exercises_position
    ON routine_exercises (routine_id, position);

CREATE INDEX idx_routine_exercises_exercise_id
    ON routine_exercises (exercise_id);


-- 4. Individual target sets for each routine exercise
CREATE TABLE routine_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    routine_exercise_id UUID NOT NULL,
    set_number INTEGER NOT NULL,

    target_reps INTEGER,
    target_weight_kg NUMERIC(7, 2),
    target_rpe NUMERIC(3, 1),
    rest_seconds INTEGER,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_routine_sets_routine_exercise
        FOREIGN KEY (routine_exercise_id)
        REFERENCES routine_exercises(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_routine_sets_set_number
        CHECK (set_number >= 1),

    CONSTRAINT ck_routine_sets_target_reps
        CHECK (target_reps IS NULL OR target_reps >= 1),

    CONSTRAINT ck_routine_sets_target_weight
        CHECK (target_weight_kg IS NULL OR target_weight_kg >= 0),

    CONSTRAINT ck_routine_sets_target_rpe
        CHECK (target_rpe IS NULL OR (target_rpe >= 1 AND target_rpe <= 10)),

    CONSTRAINT ck_routine_sets_rest_seconds
        CHECK (rest_seconds IS NULL OR rest_seconds >= 0)
);

CREATE UNIQUE INDEX uq_routine_sets_number
    ON routine_sets (routine_exercise_id, set_number);