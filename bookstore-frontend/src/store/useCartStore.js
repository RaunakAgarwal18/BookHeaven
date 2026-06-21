import { create } from 'zustand';
import apiClient from '../api/axios';

const useCartStore = create((set, get) => ({
  cart: null,
  loading: false,
  error: null,

  fetchCart: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.get('/cart');
      set({ cart: response.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
    }
  },

  addToCart: async (listingId, quantity = 1) => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.post('/cart/item', { listingId, quantity });
      set({ cart: response.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
    }
  },

  updateQuantity: async (listingId, quantity) => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.put(`/cart/items/${listingId}`, { quantity });
      set({ cart: response.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
    }
  },

  removeFromCart: async (listingId) => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.delete(`/cart/items/${listingId}`);
      set({ cart: response.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
    }
  },

  applyCoupon: async (couponCode) => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.post(`/cart/coupon/${couponCode}`);
      set({ cart: response.data, loading: false });
      return { success: true };
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
      return { success: false, message: err.response?.data?.message || err.message };
    }
  },

  removeCoupon: async () => {
    set({ loading: true, error: null });
    try {
      const response = await apiClient.delete(`/cart/coupon`);
      set({ cart: response.data, loading: false });
    } catch (err) {
      set({ error: err.response?.data?.message || err.message, loading: false });
    }
  },
  
  clearCartState: () => {
    set({ cart: null, loading: false, error: null });
  }
}));

export default useCartStore;
