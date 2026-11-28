
CREATE TABLE routine_folders (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 user_id UUID NOT NULL,
                                 name VARCHAR(100) NOT NULL,
                                 position INTEGER NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_routine_folders_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT ck_routine_folders_name
                                     CHECK (LENGTH(BTRIM(name)) BETWEEN 1 AND 100),

                                 CONSTRAINT ck_routine_folders_position
                                     CHECK (position >= 0)
);

CREATE UNIQUE INDEX uq_routine_folders_user_name
    ON routine_folders (user_id, LOWER(BTRIM(name)));

CREATE INDEX idx_routine_folders_user_position
    ON routine_folders (user_id, position);


ALTER TABLE routines
    ADD COLUMN folder_id UUID;

ALTER TABLE routines
    ADD CONSTRAINT fk_routines_folder
        FOREIGN KEY (folder_id)
            REFERENCES routine_folders(id)
            ON DELETE SET NULL;

CREATE INDEX idx_routines_folder
    ON routines (folder_id);
