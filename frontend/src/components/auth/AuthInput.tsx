import { colors } from '@/theme/colors';
import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  Pressable,
  StyleSheet,
  TextInputProps,
} from 'react-native';

interface AuthInputProps extends TextInputProps {
  label: string;
  secure?: boolean;
}

export default function AuthInput({
  label,
  secure = false,
  ...props
}: AuthInputProps) {
  const [visible, setVisible] = useState(false);

  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>

      <View style={styles.inputContainer}>
        <TextInput
          {...props}
          style={styles.input}
          placeholderTextColor="#777"
          secureTextEntry={secure && !visible}
          autoCapitalize="none"
        />

        {secure && (
          <Pressable onPress={() => setVisible(!visible)}>
            <Text style={styles.toggle}>
              {visible ? 'HIDE' : 'SHOW'}
            </Text>
          </Pressable>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 18,
  },
  label: {
    color: colors.textSecondary,
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 9,
  },
  inputContainer: {
    height: 54,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 15,
  },
  input: {
    flex: 1,
    color: colors.text,
    fontSize: 15,
  },
  toggle: {
    color: colors.primary,
    fontSize: 11,
    fontWeight: '700',
    paddingLeft: 10,
  },
});