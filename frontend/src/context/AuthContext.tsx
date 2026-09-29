import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '../types';
import { authApi } from '../api/authApi';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (name: string, email: string, password: string, phoneNumber?: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    try {
      const stored = localStorage.getItem('cpool_user');
      if (stored) {
        setUser(JSON.parse(stored));
      }
    } catch (e) {
      console.error('Failed to parse stored user from localStorage', e);
      localStorage.removeItem('cpool_user');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    const authRes = await authApi.login(email, password);
    
    // If backend returned userId in AuthResponse
    if (authRes.userId) {
      const loggedUser: User = {
        id: authRes.userId,
        name: authRes.name || email.split('@')[0],
        email: authRes.email || email,
        phoneNumber: authRes.phoneNumber || '',
      };
      setUser(loggedUser);
      localStorage.setItem('cpool_user', JSON.stringify(loggedUser));
    } else {
      // Fallback
      const loggedUser: User = {
        id: 'logged-in-user',
        name: email.split('@')[0],
        email: email,
        phoneNumber: '',
      };
      setUser(loggedUser);
      localStorage.setItem('cpool_user', JSON.stringify(loggedUser));
    }
  };

  const signup = async (name: string, email: string, password: string, phoneNumber?: string) => {
    const createdUser = await authApi.signup(name, email, password, phoneNumber);
    setUser(createdUser);
    localStorage.setItem('cpool_user', JSON.stringify(createdUser));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('cpool_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        signup,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
