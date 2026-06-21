import axios from 'axios';
import useAuthStore from '../store/useAuthStore';

// Create an Axios instance
const apiClient = axios.create({
  baseURL: 'http://localhost:8090/api', // API Gateway URL
  headers: {
    'Content-Type': 'application/json',
    'ngrok-skip-browser-warning': 'true',
  },
  withCredentials: true, // Send cookies with all requests
});

// Add a response interceptor to handle errors globally, e.g., token expiration
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 429) {
      window.dispatchEvent(new CustomEvent("rate-limit-exceeded", {
        detail: "Too many requests. Please slow down and try again shortly!"
      }));
    }
    if (error.response && error.response.status === 401) {
      // Clear auth state if unauthorized
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
