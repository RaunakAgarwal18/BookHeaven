import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import useAuthStore from '../store/useAuthStore';
import apiClient from '../api/axios';

const SetPasswordModal = () => {
    const { user, updateUser } = useAuthStore();
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    // Only show if the user requires password setup
    if (!user || !user.requiresPasswordSetup) {
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        if (newPassword.length < 8) {
            setError('Password must be at least 8 characters long.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        setLoading(true);
        try {
            await apiClient.post('/user/me/set-password', {
                newPassword,
                confirmPassword
            });
            setSuccess(true);
            // Hide the modal by updating user state
            setTimeout(() => {
                updateUser({ requiresPasswordSetup: false });
            }, 1500);
        } catch (err) {
            const errMessage = err.response?.data?.message || err.response?.data || 'Failed to set password. Please try again.';
            setError(typeof errMessage === 'string' ? errMessage : 'Failed to set password.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <AnimatePresence>
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                style={{
                    position: 'fixed',
                    top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    backdropFilter: 'blur(10px)',
                    zIndex: 99999,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    padding: '1rem'
                }}
            >
                <motion.div
                    initial={{ scale: 0.9, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    style={{
                        backgroundColor: '#1E1E2E',
                        padding: '2.5rem',
                        borderRadius: '16px',
                        width: '100%',
                        maxWidth: '450px',
                        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
                        border: '1px solid rgba(255, 255, 255, 0.1)'
                    }}
                >
                    <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                        <div style={{
                            width: '60px', height: '60px', borderRadius: '50%',
                            backgroundColor: 'rgba(59, 130, 246, 0.1)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            margin: '0 auto 1rem auto'
                        }}>
                            <span style={{ fontSize: '1.8rem' }}>🔒</span>
                        </div>
                        <h2 style={{ color: '#fff', fontSize: '1.5rem', marginBottom: '0.5rem', fontWeight: 600 }}>Set Your Password</h2>
                        <p style={{ color: '#9CA3AF', fontSize: '0.9rem', lineHeight: 1.5 }}>
                            Welcome to BookHeaven! Since you signed in with Google, please set a password to secure your account. You can use this to login via Email & Password next time.
                        </p>
                    </div>

                    {success ? (
                        <div style={{ 
                            backgroundColor: 'rgba(16, 185, 129, 0.1)', 
                            color: '#10B981', 
                            padding: '1rem', 
                            borderRadius: '8px',
                            textAlign: 'center',
                            fontWeight: '500'
                        }}>
                            Password set successfully! Continuing...
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                            {error && (
                                <div style={{ color: '#EF4444', backgroundColor: 'rgba(239, 68, 68, 0.1)', padding: '0.75rem', borderRadius: '8px', fontSize: '0.9rem', textAlign: 'center' }}>
                                    {error}
                                </div>
                            )}

                            <div>
                                <label style={{ display: 'block', color: '#E5E7EB', marginBottom: '0.5rem', fontSize: '0.9rem' }}>New Password</label>
                                <input
                                    type="password"
                                    required
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '0.75rem 1rem',
                                        backgroundColor: '#2A2A3C',
                                        border: '1px solid #3F3F5A',
                                        borderRadius: '8px',
                                        color: '#fff',
                                        outline: 'none',
                                        transition: 'all 0.2s',
                                        boxSizing: 'border-box'
                                    }}
                                    placeholder="Enter at least 8 characters"
                                    onFocus={(e) => e.target.style.borderColor = '#3B82F6'}
                                    onBlur={(e) => e.target.style.borderColor = '#3F3F5A'}
                                />
                            </div>

                            <div>
                                <label style={{ display: 'block', color: '#E5E7EB', marginBottom: '0.5rem', fontSize: '0.9rem' }}>Confirm Password</label>
                                <input
                                    type="password"
                                    required
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '0.75rem 1rem',
                                        backgroundColor: '#2A2A3C',
                                        border: '1px solid #3F3F5A',
                                        borderRadius: '8px',
                                        color: '#fff',
                                        outline: 'none',
                                        transition: 'all 0.2s',
                                        boxSizing: 'border-box'
                                    }}
                                    placeholder="Confirm your password"
                                    onFocus={(e) => e.target.style.borderColor = '#3B82F6'}
                                    onBlur={(e) => e.target.style.borderColor = '#3F3F5A'}
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                style={{
                                    width: '100%',
                                    padding: '0.875rem',
                                    backgroundColor: '#3B82F6',
                                    color: '#fff',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '1rem',
                                    fontWeight: 600,
                                    cursor: loading ? 'not-allowed' : 'pointer',
                                    opacity: loading ? 0.7 : 1,
                                    marginTop: '0.5rem',
                                    transition: 'background-color 0.2s'
                                }}
                                onMouseOver={(e) => !loading && (e.target.style.backgroundColor = '#2563EB')}
                                onMouseOut={(e) => !loading && (e.target.style.backgroundColor = '#3B82F6')}
                            >
                                {loading ? 'Saving...' : 'Set Password'}
                            </button>
                        </form>
                    )}
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
};

export default SetPasswordModal;
