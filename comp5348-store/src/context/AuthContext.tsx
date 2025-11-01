import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import type { ReactNode } from 'react';
import apiClient, { setAuthToken } from '../api/client';
import type {
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from '../api/types';

type AuthUser = {
  userId: number;
  username: string;
  email: string;
};

type AuthState = {
  user: AuthUser | null;
  token: string | null;
};

type AuthContextValue = {
  user: AuthUser | null;
  token: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  register: (payload: RegisterRequest) => Promise<RegisterResponse>;
};

const storageKey = 'store-auth-state';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const readStoredState = (): AuthState => {
  try {
    const raw = localStorage.getItem(storageKey);
    if (!raw) {
      return { user: null, token: null };
    }
    const parsed = JSON.parse(raw) as AuthState;
    return {
      user: parsed.user ?? null,
      token: parsed.token ?? null,
    };
  } catch {
    return { user: null, token: null };
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<AuthState>(() => readStoredState());

  useEffect(() => {
    setAuthToken(state.token ?? null);
    try {
      localStorage.setItem(storageKey, JSON.stringify(state));
    } catch {
      // ignore storage errors in sandbox environments
    }
  }, [state]);

  const login = async (username: string, password: string) => {
    const { data } = await apiClient.post<LoginResponse>('/api/auth/login', {
      username,
      password,
    });

    setState({
      user: {
        userId: data.userId,
        username: data.username,
        email: data.email,
      },
      token: data.accessToken,
    });
  };

  const logout = () => {
    setState({ user: null, token: null });
  };

  const register = async (
    payload: RegisterRequest,
  ): Promise<RegisterResponse> => {
    const { data } = await apiClient.post<RegisterResponse>(
      '/api/auth/register',
      payload,
    );
    return data;
  };

  const value = useMemo(
    () => ({
      user: state.user,
      token: state.token,
      login,
      logout,
      register,
    }),
    [state.user, state.token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};
