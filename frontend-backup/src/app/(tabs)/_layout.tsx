
import { Tabs, Redirect } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { ActivityIndicator, View } from 'react-native';

export default function TabLayout() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, backgroundColor: '#101010', justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator color="#B8F36B" size="large" />
      </View>
    );
  }

  if (!user) {
    return <Redirect href="/(auth)/login" />;
  }

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: '#B8F36B',
        tabBarInactiveTintColor: '#858585',
        tabBarStyle: {
          backgroundColor: '#151515',
          borderTopColor: '#292929',
          height: 65,
          paddingBottom: 8,
          paddingTop: 8,
        },
        sceneStyle: {
          backgroundColor: '#101010',
        },
      }}
    >
      <Tabs.Screen name="index" options={{ title: 'Home' }} />
      <Tabs.Screen name="routines" options={{ title: 'Routines' }} />
      <Tabs.Screen name="history" options={{ title: 'History' }} />
      <Tabs.Screen name="profile" options={{ title: 'Profile' }} />
    </Tabs>
  );
}
