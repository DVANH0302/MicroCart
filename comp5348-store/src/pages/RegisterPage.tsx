import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { RegisterRequest } from '../api/types';

const emptyForm: RegisterRequest = {
  username: '',
  password: '',
  email: '',
  firstName: '',
  lastName: '',
  bankAccountId: '',
};

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState<RegisterRequest>(emptyForm);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const updateField = <K extends keyof RegisterRequest>(
    key: K,
    value: RegisterRequest[K],
  ) => {
    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (Object.values(form).some((value) => !value)) {
      setError('Complete all fields to create your account.');
      return;
    }

    if (form.password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }

    setLoading(true);
    try {
      await register(form);
      navigate('/login', { state: { registered: true }, replace: true });
    } catch (apiError) {
      if (apiError && typeof apiError === 'object' && 'response' in apiError) {
        const message =
          (apiError as any).response?.data?.message ??
          (apiError as any).response?.data ??
          'Registration failed.';
        setError(String(message));
      } else {
        setError('Registration failed.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card narrow">
      <header className="card-header">
        <div>
          <h1>Create an account</h1>
          <p className="muted">
            Register a user in the store service. All fields are required.
          </p>
        </div>
      </header>
      {error ? <div className="alert error">{error}</div> : null}
      <form className="stack" onSubmit={handleSubmit}>
        <label className="form-field">
          <span>Username</span>
          <input
            type="text"
            value={form.username}
            onChange={(event) => updateField('username', event.currentTarget.value)}
            autoComplete="username"
          />
        </label>
        <label className="form-field">
          <span>Password</span>
          <input
            type="password"
            value={form.password}
            onChange={(event) => updateField('password', event.currentTarget.value)}
            autoComplete="new-password"
          />
        </label>
        <label className="form-field">
          <span>Email</span>
          <input
            type="email"
            value={form.email}
            onChange={(event) => updateField('email', event.currentTarget.value)}
            autoComplete="email"
          />
        </label>
        <label className="form-field">
          <span>First name</span>
          <input
            type="text"
            value={form.firstName}
            onChange={(event) =>
              updateField('firstName', event.currentTarget.value)
            }
          />
        </label>
        <label className="form-field">
          <span>Last name</span>
          <input
            type="text"
            value={form.lastName}
            onChange={(event) =>
              updateField('lastName', event.currentTarget.value)
            }
          />
        </label>
        <label className="form-field">
          <span>Bank account ID</span>
          <input
            type="text"
            value={form.bankAccountId}
            onChange={(event) =>
              updateField('bankAccountId', event.currentTarget.value)
            }
            placeholder="e.g. CUST_001"
          />
        </label>
        <button type="submit" className="btn primary" disabled={loading}>
          {loading ? 'Creating account…' : 'Register'}
        </button>
      </form>
    </section>
  );
};

export default RegisterPage;
