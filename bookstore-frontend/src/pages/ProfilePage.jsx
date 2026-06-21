import { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { User, Mail, Phone, MapPin, Edit2, X, Save, Loader2, KeyRound, Eye, EyeOff, CheckCircle2, Camera, Trash2 } from 'lucide-react';
import apiClient from '../api/axios';
import useAuthStore from '../store/useAuthStore';

const ProfilePage = () => {
  const { isAuthenticated, user: authUser } = useAuthStore();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [uploadingPic, setUploadingPic] = useState(false);
  const fileInputRef = useRef(null);

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    razorpayAccountId: '',
  });

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
      return;
    }
    fetchProfile();
  }, [isAuthenticated]);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/user/me');
      setProfile(res.data);
      // Populate form with existing data
      setForm({
        firstName: res.data.firstName || '',
        lastName: res.data.lastName || '',
        phoneNumber: res.data.phoneNumber || '',
        razorpayAccountId: res.data.razorpayAccountId || '',
      });
    } catch (err) {
      setError('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    setSaveError(null);
    setSaveSuccess(false);
    setIsSaving(true);
    try {
      const res = await apiClient.put(`/user/${profile.id}`, form);
      setProfile(res.data);
      setIsEditing(false);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      setSaveError(err.response?.data?.message || 'Failed to save changes');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    // Reset form to current profile data
    setForm({
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || '',
      phoneNumber: profile?.phoneNumber || '',
      razorpayAccountId: profile?.razorpayAccountId || '',
    });
    setSaveError(null);
    setIsEditing(false);
  };

  const handlePictureUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setSaveError('Please select an image file.');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      setSaveError('Image must be under 2MB.');
      return;
    }
    setUploadingPic(true);
    setSaveError(null);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await apiClient.post('/user/me/profile-picture', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setProfile(res.data);
      useAuthStore.getState().updateUser({ profilePicture: res.data.profilePicture });
    } catch (err) {
      setSaveError(err.response?.data?.message || 'Failed to upload picture.');
    } finally {
      setUploadingPic(false);
      e.target.value = '';
    }
  };

  if (loading) {
    return (
      <div className="container mt-8" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <Loader2 className="animate-spin" size={32} style={{ color: 'var(--primary-color)' }} />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mt-8 text-center">
        <p style={{ color: 'var(--danger-color)' }}>{error}</p>
      </div>
    );
  }

  const inputStyle = {
    width: '100%',
    padding: '0.6rem 0.9rem',
    borderRadius: 'var(--radius)',
    border: '1px solid var(--border-color)',
    backgroundColor: 'var(--bg-color)',
    color: 'var(--text-color)',
    fontSize: '1rem',
    outline: 'none',
    boxSizing: 'border-box',
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '0.35rem',
    color: 'var(--text-muted)',
    fontSize: '0.85rem',
    fontWeight: '500',
  };

  const infoRow = (icon, label, value) => (
    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', marginBottom: '1.25rem' }}>
      <div style={{ color: 'var(--primary-color)', marginTop: '2px', flexShrink: 0 }}>{icon}</div>
      <div>
        <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '0.2rem' }}>{label}</div>
        <div style={{ fontWeight: '500' }}>{value || <span style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>Not set</span>}</div>
      </div>
    </div>
  );

  return (
    <div className="container mt-8 mb-8" style={{ maxWidth: '800px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem' }}>My Profile</h1>
        {!isEditing ? (
          <button
            onClick={() => setIsEditing(true)}
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.2rem' }}
          >
            <Edit2 size={16} /> Edit Profile
          </button>
        ) : (
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button
              onClick={handleCancel}
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.2rem', backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-color)' }}
            >
              <X size={16} /> Cancel
            </button>
            <button
              onClick={handleSave}
              disabled={isSaving}
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1.2rem' }}
            >
              {isSaving ? <Loader2 className="animate-spin" size={16} /> : <Save size={16} />}
              Save Changes
            </button>
          </div>
        )}
      </div>

      {saveSuccess && (
        <div style={{ marginBottom: '1.5rem', padding: '0.75rem 1rem', backgroundColor: 'rgba(34,197,94,0.1)', color: '#22c55e', borderRadius: 'var(--radius)', border: '1px solid rgba(34,197,94,0.3)' }}>
          ✓ Profile updated successfully!
        </div>
      )}

      {saveError && (
        <div style={{ marginBottom: '1.5rem', padding: '0.75rem 1rem', backgroundColor: 'rgba(239,68,68,0.1)', color: 'var(--danger-color)', borderRadius: 'var(--radius)', border: '1px solid rgba(239,68,68,0.3)' }}>
          {saveError}
        </div>
      )}

      {/* Profile Picture */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2rem' }}>
        <div
          onClick={() => fileInputRef.current?.click()}
          style={{
            position: 'relative',
            width: '100px',
            height: '100px',
            borderRadius: '50%',
            overflow: 'hidden',
            cursor: 'pointer',
            border: '3px solid var(--primary-color)',
            boxShadow: '0 0 20px rgba(59,130,246,0.3)',
          }}
        >
          {profile?.profilePicture ? (
            <img
              src={profile.profilePicture}
              alt="Profile"
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          ) : (
            <div style={{
              width: '100%', height: '100%',
              background: 'linear-gradient(135deg, var(--primary-color), #8b5cf6)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: '#fff', fontWeight: '700', fontSize: '2.5rem',
            }}>
              {profile?.username?.[0]?.toUpperCase() || '?'}
            </div>
          )}
          {/* Camera overlay */}
          <div style={{
            position: 'absolute', bottom: 0, left: 0, right: 0,
            height: '32px',
            backgroundColor: 'rgba(0,0,0,0.6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            {uploadingPic ? (
              <Loader2 className="animate-spin" size={14} style={{ color: '#fff' }} />
            ) : (
              <Camera size={14} style={{ color: '#fff' }} />
            )}
          </div>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handlePictureUpload}
          style={{ display: 'none' }}
        />
        <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.5rem' }}>
          Click to change photo
        </p>
      </div>

      {/* Account Info Card — always read-only */}
      <div className="card glass-panel" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.2rem', marginBottom: '1.5rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          Account Information
        </h2>
        {infoRow(<User size={18} />, 'Username', profile?.username)}
        {infoRow(<Mail size={18} />, 'Email', profile?.email)}
        <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '-0.5rem' }}>Username and email cannot be changed.</p>
      </div>

      {/* Personal Info Card */}
      <div className="card glass-panel" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.2rem', marginBottom: '1.5rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
          Personal Information
        </h2>

        {isEditing ? (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div>
              <label style={labelStyle}>First Name</label>
              <input name="firstName" value={form.firstName} onChange={handleChange} style={inputStyle} placeholder="First name" />
            </div>
            <div>
              <label style={labelStyle}>Last Name</label>
              <input name="lastName" value={form.lastName} onChange={handleChange} style={inputStyle} placeholder="Last name" />
            </div>
            <div style={profile?.role === 'SELLER' ? {} : { gridColumn: '1 / -1' }}>
              <label style={labelStyle}>Phone Number</label>
              <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} style={inputStyle} placeholder="e.g. +91 98765 43210" />
            </div>
            {profile?.role === 'SELLER' && (
              <div>
                <label style={labelStyle}>Razorpay Account ID</label>
                <input name="razorpayAccountId" value={form.razorpayAccountId} onChange={handleChange} style={inputStyle} placeholder="e.g. acc_XYZ123" />
              </div>
            )}
          </div>
        ) : (
          <>
            {infoRow(<User size={18} />, 'First Name', profile?.firstName)}
            {infoRow(<User size={18} />, 'Last Name', profile?.lastName)}
            {infoRow(<Phone size={18} />, 'Phone Number', profile?.phoneNumber)}
            {profile?.role === 'SELLER' && (
              infoRow(<KeyRound size={18} />, 'Razorpay Account ID', profile?.razorpayAccountId)
            )}
          </>
        )}
      </div>

      {/* Address Card */}
      <AddressSection addresses={profile?.addresses || []} fetchProfile={fetchProfile} />

      {/* Change Password Card */}
      <ChangePasswordCard />
    </div>
  );
};

/* ── Password field helper — defined OUTSIDE ChangePasswordCard so React
     never treats it as a new component type on re-render (fixes focus loss) */
const PwField = ({ id, label, name, value, showVisible, onToggleShow, onChange }) => {
  const inputStyle = {
    width: '100%',
    padding: '0.6rem 2.5rem 0.6rem 0.9rem',
    borderRadius: 'var(--radius)',
    border: '1px solid var(--border-color)',
    backgroundColor: 'var(--bg-color)',
    color: 'var(--text-color)',
    fontSize: '1rem',
    outline: 'none',
    boxSizing: 'border-box',
  };
  const labelStyle = {
    display: 'block',
    marginBottom: '0.35rem',
    color: 'var(--text-muted)',
    fontSize: '0.85rem',
    fontWeight: '500',
  };
  return (
    <div>
      <label style={labelStyle}>{label}</label>
      <div style={{ position: 'relative' }}>
        <input
          id={id}
          type={showVisible ? 'text' : 'password'}
          name={name}
          value={value}
          onChange={onChange}
          style={inputStyle}
          placeholder="••••••••"
          autoComplete="off"
        />
        <button
          type="button"
          onClick={onToggleShow}
          style={{
            position: 'absolute',
            right: '0.75rem',
            top: '50%',
            transform: 'translateY(-50%)',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--text-muted)',
            padding: 0,
            display: 'flex',
          }}
          tabIndex={-1}
        >
          {showVisible ? <EyeOff size={16} /> : <Eye size={16} />}
        </button>
      </div>
    </div>
  );
};

/* ── Standalone Change Password card ─────────────────────────────── */
const ChangePasswordCard = () => {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [pwForm, setPwForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
  const [showPw, setShowPw] = useState({ old: false, new: false, confirm: false });
  const [pwLoading, setPwLoading] = useState(false);
  const [pwError, setPwError] = useState(null);
  const [pwWrongOld, setPwWrongOld] = useState(false);
  const [pwSuccess, setPwSuccess] = useState(false);

  const handlePwChange = (e) => {
    const { name, value } = e.target;
    setPwForm((prev) => ({ ...prev, [name]: value }));
    setPwError(null);
    setPwWrongOld(false);
  };

  const handleToggle = () => {
    setOpen((v) => !v);
    setPwForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
    setPwError(null);
    setPwWrongOld(false);
    setPwSuccess(false);
  };

  const handleSubmit = async () => {
    if (!pwForm.oldPassword || !pwForm.newPassword || !pwForm.confirmPassword) {
      setPwError('All fields are required.');
      return;
    }
    if (pwForm.newPassword.length < 8) {
      setPwError('New password must be at least 8 characters.');
      return;
    }
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      setPwError('New password and confirm password do not match.');
      return;
    }
    setPwLoading(true);
    setPwError(null);
    setPwWrongOld(false);
    try {
      await apiClient.post('/user/me/change-password', pwForm);
      setPwSuccess(true);
      setPwForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => {
        setPwSuccess(false);
        setOpen(false);
      }, 3000);
    } catch (err) {
      const status = err.response?.status;
      const msg = err.response?.data?.message || 'Failed to change password.';
      if (status === 400 && msg.toLowerCase().includes('incorrect')) {
        setPwWrongOld(true);
      } else {
        setPwError(msg);
      }
    } finally {
      setPwLoading(false);
    }
  };


  return (
    <div className="card glass-panel" style={{ marginTop: '1.5rem' }}>
      {/* Header row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.2rem', marginBottom: '0.25rem' }}>Password</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: 0 }}>
            Keep your account secure with a strong password.
          </p>
        </div>
        <button
          id="change-password-toggle"
          onClick={handleToggle}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.6rem 1.2rem',
            backgroundColor: open ? 'transparent' : undefined,
            border: open ? '1px solid var(--border-color)' : undefined,
            color: open ? 'var(--text-color)' : undefined,
          }}
        >
          {open ? <><X size={16} /> Cancel</> : <><KeyRound size={16} /> Change Password</>}
        </button>
      </div>

      {/* Expandable form */}
      {open && (
        <div style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border-color)' }}>
          {/* Success banner */}
          {pwSuccess && (
            <div style={{
              marginBottom: '1rem',
              padding: '0.75rem 1rem',
              backgroundColor: 'rgba(34,197,94,0.1)',
              color: '#22c55e',
              borderRadius: 'var(--radius)',
              border: '1px solid rgba(34,197,94,0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
            }}>
              <CheckCircle2 size={16} /> Password changed successfully!
            </div>
          )}

          {/* Wrong old password guidance */}
          {pwWrongOld && (
            <div style={{
              marginBottom: '1rem',
              padding: '0.85rem 1rem',
              backgroundColor: 'rgba(239,68,68,0.08)',
              color: 'var(--danger-color)',
              borderRadius: 'var(--radius)',
              border: '1px solid rgba(239,68,68,0.3)',
              fontSize: '0.9rem',
              lineHeight: '1.5',
            }}>
              <strong>Incorrect current password.</strong>
              <br />
              If you've forgotten it, please{' '}
              <Link
                to="/login"
                style={{ color: 'var(--primary-color)', textDecoration: 'underline', fontWeight: '600' }}
                onClick={() => navigate('/login')}
              >
                go to the login page
              </Link>{' '}
              and use <strong>Forgot Password</strong> to reset it via email.
            </div>
          )}

          {/* Generic error */}
          {pwError && (
            <div style={{
              marginBottom: '1rem',
              padding: '0.75rem 1rem',
              backgroundColor: 'rgba(239,68,68,0.1)',
              color: 'var(--danger-color)',
              borderRadius: 'var(--radius)',
              border: '1px solid rgba(239,68,68,0.3)',
            }}>
              {pwError}
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <PwField
              id="old-password"
              label="Current Password"
              name="oldPassword"
              value={pwForm.oldPassword}
              showVisible={showPw.old}
              onToggleShow={() => setShowPw((p) => ({ ...p, old: !p.old }))}
              onChange={handlePwChange}
            />
            <PwField
              id="new-password"
              label="New Password"
              name="newPassword"
              value={pwForm.newPassword}
              showVisible={showPw.new}
              onToggleShow={() => setShowPw((p) => ({ ...p, new: !p.new }))}
              onChange={handlePwChange}
            />
            <PwField
              id="confirm-password"
              label="Confirm New Password"
              name="confirmPassword"
              value={pwForm.confirmPassword}
              showVisible={showPw.confirm}
              onToggleShow={() => setShowPw((p) => ({ ...p, confirm: !p.confirm }))}
              onChange={handlePwChange}
            />

            {/* Live mismatch hint */}
            {pwForm.confirmPassword && pwForm.newPassword !== pwForm.confirmPassword && (
              <p style={{ color: 'var(--danger-color)', fontSize: '0.82rem', margin: '-0.5rem 0 0' }}>
                Passwords do not match.
              </p>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
              <button
                id="submit-change-password"
                onClick={handleSubmit}
                disabled={pwLoading}
                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.65rem 1.5rem' }}
              >
                {pwLoading ? <Loader2 className="animate-spin" size={16} /> : <KeyRound size={16} />}
                {pwLoading ? 'Updating…' : 'Update Password'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

/* ── Standalone Address Section ─────────────────────────────── */
const AddressSection = ({ addresses, fetchProfile }) => {
  const [editingId, setEditingId] = useState(null); // 'new' or ID
  const [form, setForm] = useState({ street: '', city: '', state: '', zipCode: '', country: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleEdit = (addr) => {
    setEditingId(addr.id);
    setForm({ street: addr.street, city: addr.city, state: addr.state, zipCode: addr.zipCode, country: addr.country });
    setError(null);
  };

  const handleAddNew = () => {
    setEditingId('new');
    setForm({ street: '', city: '', state: '', zipCode: '', country: '' });
    setError(null);
  };

  const handleCancel = () => {
    setEditingId(null);
    setForm({ street: '', city: '', state: '', zipCode: '', country: '' });
    setError(null);
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setLoading(true);
    setError(null);
    try {
      if (editingId === 'new') {
        await apiClient.post('/user/address', form);
      } else {
        await apiClient.put(`/user/address/${editingId}`, form);
      }
      setEditingId(null);
      fetchProfile();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save address');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this address?')) return;
    try {
      await apiClient.delete(`/user/address/${id}`);
      fetchProfile();
    } catch (err) {
      alert('Failed to delete address');
    }
  };

  const inputStyle = {
    width: '100%', padding: '0.6rem 0.9rem', borderRadius: 'var(--radius)',
    border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-color)',
    color: 'var(--text-color)', fontSize: '1rem', outline: 'none', boxSizing: 'border-box'
  };
  const labelStyle = { display: 'block', marginBottom: '0.35rem', color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: '500' };

  return (
    <div className="card glass-panel" style={{ marginBottom: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
        <h2 style={{ fontSize: '1.2rem', margin: 0 }}>Addresses</h2>
        {editingId === null && (
          <button onClick={handleAddNew} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
            + Add New
          </button>
        )}
      </div>

      {error && <div style={{ color: 'var(--danger-color)', marginBottom: '1rem' }}>{error}</div>}

      {editingId !== null ? (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <div style={{ gridColumn: '1 / -1' }}>
            <label style={labelStyle}>Street</label>
            <input name="street" value={form.street} onChange={handleChange} style={inputStyle} placeholder="Street address" />
          </div>
          <div><label style={labelStyle}>City</label><input name="city" value={form.city} onChange={handleChange} style={inputStyle} placeholder="City" /></div>
          <div><label style={labelStyle}>State</label><input name="state" value={form.state} onChange={handleChange} style={inputStyle} placeholder="State" /></div>
          <div><label style={labelStyle}>ZIP Code</label><input name="zipCode" value={form.zipCode} onChange={handleChange} style={inputStyle} placeholder="ZIP / Postal code" /></div>
          <div><label style={labelStyle}>Country</label><input name="country" value={form.country} onChange={handleChange} style={inputStyle} placeholder="Country" /></div>
          
          <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button onClick={handleCancel} style={{ padding: '0.5rem 1rem', background: 'transparent', color: 'var(--text-color)', border: '1px solid var(--border-color)' }}>Cancel</button>
            <button onClick={handleSave} disabled={loading} style={{ padding: '0.5rem 1rem' }}>{loading ? 'Saving...' : 'Save Address'}</button>
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {(!addresses || addresses.length === 0) ? (
            <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--text-muted)' }}>
              <MapPin size={32} style={{ marginBottom: '0.75rem', opacity: 0.4 }} />
              <p>No addresses saved yet.</p>
            </div>
          ) : (
            addresses.map(addr => (
              <div key={addr.id} style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: '500', marginBottom: '0.25rem' }}>{addr.street}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    {addr.city}, {addr.state} {addr.zipCode}, {addr.country}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button onClick={() => handleEdit(addr)} style={{ background: 'transparent', color: 'var(--text-color)', border: '1px solid var(--border-color)', padding: '0.4rem' }}><Edit2 size={16} /></button>
                  <button onClick={() => handleDelete(addr.id)} style={{ background: 'transparent', color: 'var(--danger-color)', border: '1px solid rgba(239,68,68,0.3)', padding: '0.4rem' }}><Trash2 size={16} /></button>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default ProfilePage;

