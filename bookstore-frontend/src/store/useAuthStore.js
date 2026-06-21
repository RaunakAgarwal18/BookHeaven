import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (userData) => set({
        user: userData,
        token: null,
        isAuthenticated: true,
      }),
      logout: async () => {
        try {
          const { default: apiClient } = await import('../api/axios');
          await apiClient.post('/user/auth/logout');
        } catch (error) {
          console.error("Failed to logout from backend:", error);
        } finally {
          set({
            user: null,
            token: null,
            isAuthenticated: false,
          });
        }
      },
      updateUser: (userData) => set((state) => ({
        user: { ...state.user, ...userData },
      })),
    }),
    {
      name: 'auth-storage', // unique name
    }
  )
);

export default useAuthStore;
