import { create } from 'zustand';
import apiClient from '../api/axios';

const useWishlistStore = create((set, get) => ({
  wishlistIds: new Set(),
  loading: false,
  error: null,

  fetchWishlistIds: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.get('/user/wishlist');
      set({ wishlistIds: new Set(response.data.map(String)), loading: false });
    } catch (error) {
      console.error('Failed to fetch wishlist', error);
      set({ error: error.response?.data?.message || 'Failed to fetch wishlist', loading: false });
    }
  },

  toggleWishlist: async (bookId) => {
    const currentIds = get().wishlistIds; bookId = String(bookId);
    const isWishlisted = currentIds.has(bookId);
    
    // Optimistic update
    const newIds = new Set(currentIds);
    if (isWishlisted) {
      newIds.delete(bookId);
    } else {
      newIds.add(bookId);
    }
    set({ wishlistIds: newIds });

    try {
      if (isWishlisted) {
        await apiClient.delete(`/user/wishlist/${bookId}`);
      } else {
        await apiClient.post(`/user/wishlist/${bookId}`);
      }
    } catch (error) {
      // Revert on failure
      set({ wishlistIds: currentIds, error: 'Failed to update wishlist' });
      console.error('Failed to update wishlist', error);
    }
  },
  
  clearWishlist: () => {
    set({ wishlistIds: new Set() });
  }
}));

export default useWishlistStore;
