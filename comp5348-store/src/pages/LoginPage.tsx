import { useState } from 'react';
import type { FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

type LocationState = {
  from?: string;
  registered?: boolean;
};

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const state = (location.state as LocationState) ?? {};

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    if (!username || !password) {
      setError('Enter your username and password.');
      return;
    }
    setLoading(true);
    try {
      await login(username, password);
      const redirectTo = state.from ?? '/catalog';
      navigate(redirectTo, { replace: true });
    } catch (authError) {
      if (authError && typeof authError === 'object' && 'response' in authError) {
        const message =
          (authError as any).response?.data?.message ??
          (authError as any).response?.data ??
          'Login failed. Check your credentials.';
        setError(String(message));
      } else {
        setError('Login failed. Check your credentials.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card narrow">
      <header className="card-header">
        <div>
          <h1>Log in</h1>
          <p className="muted">
            Authenticate with the store service to place orders and manage them.
          </p>
        </div>
      </header>
      {state.registered ? (
        <div className="alert success">
          Account created successfully. You can now sign in.
        </div>
      ) : null}
      {error ? <div className="alert error">{error}</div> : null}
      <form className="stack" onSubmit={handleSubmit}>
        <label className="form-field">
          <span>Username</span>
          <input
            type="text"
            value={username}
            autoComplete="username"
            onChange={(event) => setUsername(event.currentTarget.value)}
            placeholder="customer"
          />
        </label>
        <label className="form-field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            autoComplete="current-password"
            onChange={(event) => setPassword(event.currentTarget.value)}
            placeholder="COMP5348"
          />
        </label>
        <button type="submit" className="btn primary" disabled={loading}>
          {loading ? 'Signing in…' : 'Log in'}
        </button>
      </form>
      <p className="muted">
        Tip: the seed data ships with the user <code>customer</code>/
        <code>COMP5348</code>.
      </p>
    </section>
  );
};

export default LoginPage;
