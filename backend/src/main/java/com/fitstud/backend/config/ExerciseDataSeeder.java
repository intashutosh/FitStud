package com.fitstud.backend.config;

import com.fitstud.backend.entity.Exercise;
import com.fitstud.backend.enums.Equipment;
import com.fitstud.backend.enums.ExerciseCategory;
import com.fitstud.backend.enums.MechanicType;
import com.fitstud.backend.enums.MuscleGroup;
import com.fitstud.backend.repository.ExerciseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ExerciseDataSeeder implements CommandLineRunner {

    private final ExerciseRepository exerciseRepository;

    public ExerciseDataSeeder(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (exerciseRepository.count() > 0) {
            return; // Catalog already seeded
        }

        // --- CHEST ---
        Exercise barbellBench = buildExercise(
                "Barbell Bench Press",
                MuscleGroup.CHEST,
                Set.of(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                Equipment.BARBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                1, // Tier 1: Core primary compound
                60,
                "Retract shoulder blades, lower bar to mid-chest, and press up explosively."
        );

        Exercise inclineDbPress = buildExercise(
                "Incline Dumbbell Press",
                MuscleGroup.CHEST,
                Set.of(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
                Equipment.DUMBBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                2, // Tier 2: Secondary compound
                50,
                "Set bench to 30 degrees. Lower dumbbells with control to upper chest."
        );

        Exercise cableFly = buildExercise(
                "Cable Chest Fly",
                MuscleGroup.CHEST,
                Set.of(MuscleGroup.SHOULDERS),
                Equipment.CABLE,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.ISOLATION,
                3, // Tier 3: Accessory / compressible in short sessions
                35,
                "Keep a slight bend in elbows and bring handles together in a wide arc."
        );

        // --- BACK ---
        Exercise pullUp = buildExercise(
                "Pull-Up",
                MuscleGroup.LATS,
                Set.of(MuscleGroup.BICEPS, MuscleGroup.UPPER_BACK),
                Equipment.BODYWEIGHT,
                ExerciseCategory.BODYWEIGHT_REPS,
                MechanicType.COMPOUND,
                1,
                50,
                "Pull chest to the bar by driving elbows down toward your hips."
        );

        Exercise closeGripLatPulldown = buildExercise(
                "Close-Grip Lat Pulldown",
                MuscleGroup.LATS,
                Set.of(MuscleGroup.BICEPS, MuscleGroup.UPPER_BACK),
                Equipment.CABLE,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                2,
                45,
                "Lean back slightly, pull handle to upper chest while squeezing lats."
        );

        Exercise barbellRow = buildExercise(
                "Barbell Bent-Over Row",
                MuscleGroup.UPPER_BACK,
                Set.of(MuscleGroup.LATS, MuscleGroup.BICEPS, MuscleGroup.LOWER_BACK),
                Equipment.BARBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                1,
                55,
                "Hinge hips to 45 degrees, keep back flat, and row bar to lower ribs."
        );

        // --- SHOULDERS ---
        Exercise overheadPress = buildExercise(
                "Overhead Barbell Press",
                MuscleGroup.SHOULDERS,
                Set.of(MuscleGroup.TRICEPS, MuscleGroup.UPPER_BACK),
                Equipment.BARBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                1,
                55,
                "Brace core and glutes, press bar vertically overhead and lock out."
        );

        Exercise lateralRaise = buildExercise(
                "Dumbbell Lateral Raise",
                MuscleGroup.SHOULDERS,
                Set.of(),
                Equipment.DUMBBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.ISOLATION,
                3,
                35,
                "Raise dumbbells out to sides with slight forward lean until elbows reach shoulder height."
        );

        // --- LEGS ---
        Exercise barbellSquat = buildExercise(
                "Barbell Back Squat",
                MuscleGroup.QUADRICEPS,
                Set.of(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK),
                Equipment.BARBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                1,
                65,
                "Brace core, sit hips back and down until thighs are at least parallel to floor."
        );

        Exercise romanianDeadlift = buildExercise(
                "Romanian Deadlift (RDL)",
                MuscleGroup.HAMSTRINGS,
                Set.of(MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK),
                Equipment.BARBELL,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                2,
                55,
                "Push hips straight back with soft knees until you feel a deep hamstring stretch."
        );

        Exercise legPress = buildExercise(
                "Leg Press",
                MuscleGroup.QUADRICEPS,
                Set.of(MuscleGroup.GLUTES),
                Equipment.MACHINE,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.COMPOUND,
                2,
                50,
                "Lower sled until knees are at 90 degrees without letting lower back round."
        );

        // --- ARMS & CORE ---
        Exercise barbellCurl = buildExercise(
                "EZ-Bar Bicep Curl",
                MuscleGroup.BICEPS,
                Set.of(MuscleGroup.FOREARMS),
                Equipment.EZ_BAR,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.ISOLATION,
                3,
                35,
                "Keep elbows pinned to sides and curl bar up without swinging."
        );

        Exercise tricepPushdown = buildExercise(
                "Cable Rope Tricep Pushdown",
                MuscleGroup.TRICEPS,
                Set.of(),
                Equipment.CABLE,
                ExerciseCategory.WEIGHT_REPS,
                MechanicType.ISOLATION,
                3,
                35,
                "Extend elbows fully and spread the rope ends apart at the bottom."
        );

        Exercise plank = buildExercise(
                "Plank",
                MuscleGroup.ABS,
                Set.of(MuscleGroup.SHOULDERS),
                Equipment.BODYWEIGHT,
                ExerciseCategory.DURATION,
                MechanicType.ISOLATION,
                3,
                60,
                "Hold a rigid straight line from head to heels while bracing abs and glutes."
        );

        List<Exercise> initialSave = exerciseRepository.saveAll(Arrays.asList(
                barbellBench, inclineDbPress, cableFly,
                pullUp, closeGripLatPulldown, barbellRow,
                overheadPress, lateralRaise,
                barbellSquat, romanianDeadlift, legPress,
                barbellCurl, tricepPushdown, plank
        ));

        // Link fast-swap / crowded-gym alternatives
        barbellBench.getAlternativeExercises().add(inclineDbPress);
        pullUp.getAlternativeExercises().add(closeGripLatPulldown);
        barbellSquat.getAlternativeExercises().add(legPress);

        exerciseRepository.saveAll(Arrays.asList(barbellBench, pullUp, barbellSquat));
        System.out.println("Seeded " + initialSave.size() + " built-in exercises into FitStud catalog.");
    }

    private Exercise buildExercise(
            String name,
            MuscleGroup primaryMuscle,
            Set<MuscleGroup> secondaryMuscles,
            Equipment equipment,
            ExerciseCategory category,
            MechanicType mechanicType,
            int priorityTier,
            int estSecondsPerSet,
            String instructions
    ) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setPrimaryMuscle(primaryMuscle);
        exercise.setSecondaryMuscles(new HashSet<>(secondaryMuscles));
        exercise.setEquipment(equipment);
        exercise.setCategory(category);
        exercise.setCustom(false);
        exercise.setMechanicType(mechanicType);
        exercise.setPriorityTier(priorityTier);
        exercise.setEstimatedSecondsPerSet(estSecondsPerSet);
        exercise.setInstructions(instructions);
        return exercise;
    }
}