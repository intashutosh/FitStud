
import { Redirect } from 'expo-router';
import { ActivityIndicator, View } from 'react-native';
import { useAuth } from '@/context/AuthContext';

export default function Index() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, backgroundColor: '#101010', justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator color="#B8F36B" size="large" />
      </View>
    );
  }

  return user
    ? <Redirect href="/(tabs)" />
    : <Redirect href="/(auth)/login" />;
}
