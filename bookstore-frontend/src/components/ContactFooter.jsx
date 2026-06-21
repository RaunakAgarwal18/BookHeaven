import React, { useState, useRef, useEffect } from 'react';
import apiClient from '../api/axios';
import { Mail, Send, Paperclip, CheckCircle, XCircle } from 'lucide-react';
import useAuthStore from '../store/useAuthStore';
import './ContactFooter.css';

const ContactFooter = () => {
  const user = useAuthStore((state) => state.user);

  const [formData, setFormData] = useState({
    email: user?.email || '',
    subject: '',
    description: '',
    screenshotBase64: '',
    filename: ''
  });
  const [status, setStatus] = useState('idle'); // idle, loading, success, error
  const [errorMessage, setErrorMessage] = useState('');
  const fileInputRef = useRef(null);

  useEffect(() => {
    setFormData((prev) => ({ ...prev, email: user?.email || '' }));
  }, [user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      setErrorMessage("File size must be under 2MB");
      setStatus('error');
      return;
    }
    
    setStatus('idle');
    setErrorMessage('');

    const reader = new FileReader();
    reader.onloadend = () => {
      setFormData(prev => ({
        ...prev,
        screenshotBase64: reader.result,
        filename: file.name
      }));
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email || !formData.subject || !formData.description) {
      setErrorMessage("Please fill out all required fields.");
      setStatus('error');
      return;
    }

    setStatus('loading');
    try {
      await apiClient.post('/user/contact', formData);
      setStatus('success');
      setFormData({
        email: '',
        subject: '',
        description: '',
        screenshotBase64: '',
        filename: ''
      });
      if (fileInputRef.current) fileInputRef.current.value = '';
      
      setTimeout(() => setStatus('idle'), 5000);
    } catch (error) {
      setStatus('error');
      setErrorMessage("Failed to send message. Please try again later.");
    }
  };

  return (
    <footer className="contact-footer">
      <div className="contact-footer-container">
        <div className="contact-info-section">
          <h2 className="contact-title"><Mail size={24} style={{ marginRight: '10px' }} /> Get in Touch</h2>
          <p className="contact-subtitle">Have questions, feedback, or need support? We'd love to hear from you!</p>
          <div className="contact-details">
            <p><strong>Email:</strong> support@bookheaven.com</p>
            <p><strong>Phone:</strong> +1 (555) 123-4567</p>
          </div>
        </div>

        <div className="contact-form-section">
          <form onSubmit={handleSubmit} className="contact-form">
            <div className="form-row">
              <input 
                type="email" 
                name="email" 
                placeholder="Your Email Address *" 
                value={formData.email}
                onChange={handleInputChange}
                required
                className="contact-input"
              />
              <input 
                type="text" 
                name="subject" 
                placeholder="Subject *" 
                value={formData.subject}
                onChange={handleInputChange}
                required
                className="contact-input"
              />
            </div>
            <textarea 
              name="description" 
              placeholder="How can we help you? *" 
              value={formData.description}
              onChange={handleInputChange}
              required
              className="contact-textarea"
              rows={4}
            />
            
            <div className="form-actions">
              <div className="file-upload-wrapper">
                <input 
                  type="file" 
                  accept="image/*" 
                  onChange={handleFileChange}
                  ref={fileInputRef}
                  id="screenshot-upload"
                  className="hidden-file-input"
                />
                <label htmlFor="screenshot-upload" className="file-upload-label">
                  <Paperclip size={18} />
                  <span className="file-name">{formData.filename ? formData.filename : "Attach Screenshot (Max 2MB)"}</span>
                </label>
              </div>
              
              <button type="submit" disabled={status === 'loading'} className="submit-btn">
                {status === 'loading' ? 'Sending...' : <><Send size={18} /> Send Message</>}
              </button>
            </div>

            {status === 'success' && (
              <div className="status-message success">
                <CheckCircle size={18} /> Message sent successfully! We'll get back to you soon.
              </div>
            )}
            
            {status === 'error' && (
              <div className="status-message error">
                <XCircle size={18} /> {errorMessage}
              </div>
            )}
          </form>
        </div>
      </div>
      <div className="contact-footer-bottom">
        <p>&copy; {new Date().getFullYear()} BookHeaven. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default ContactFooter;
