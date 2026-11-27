
import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from 'react';

import {
  LoginRequest,
  RegisterRequest,
  UserProfile,
} from '@/types/auth';

import {
  loginUser,
  registerUser,
  getCurrentUser,
} from '@/api/auth.api';

import {
  getToken,
  saveToken,
  removeToken,
} from '@/api/token-storage';

interface AuthContextType {
  user: UserProfile | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  signIn: (data: LoginRequest) => Promise<void>;
  signUp: (data: RegisterRequest) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(
  undefined
);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore an existing session when the application starts.
  useEffect(() => {
    async function restoreSession() {
      try {
        const token = await getToken();

        if (!token) {
          return;
        }

        const profile = await getCurrentUser();
        setUser(profile);
      } catch (error) {
        await removeToken();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    }

    restoreSession();
  }, []);

  async function signIn(data: LoginRequest): Promise<void> {
    const response = await loginUser(data);

    await saveToken(response.accessToken);

    try {
      const profile = await getCurrentUser();
      setUser(profile);
    } catch (error) {
      await removeToken();
      throw error;
    }
  }

  async function signUp(data: RegisterRequest): Promise<void> {
    await registerUser(data);
  }

  async function signOut(): Promise<void> {
    await removeToken();
    setUser(null);
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: user !== null,
        signIn,
        signUp,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}
