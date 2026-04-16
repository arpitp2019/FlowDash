/**
 * FlowDash Authentication Service
 * Handles user sessions and authorization state.
 * For production, replace this with a backend (e.g. Firebase).
 */

const AUTH_KEY = 'flowdash_user_session';
const USERS_KEY = 'flowdash_registered_users';

const AuthService = {
  // Check if user is logged in
  isAuthenticated() {
    return !!localStorage.getItem(AUTH_KEY);
  },

  // Get current logged in user details
  getCurrentUser() {
    const session = localStorage.getItem(AUTH_KEY);
    return session ? JSON.parse(session) : null;
  },

  // Register a new user
  signUp(username, email, password) {
    const users = JSON.parse(localStorage.getItem(USERS_KEY) || '[]');
    
    // Check if user already exists
    if (users.find(u => u.email === email)) {
      throw new Error('An account with this email already exists.');
    }

    const newUser = { username, email, password, id: Date.now().toString() };
    users.push(newUser);
    localStorage.setItem(USERS_KEY, JSON.stringify(users));

    // Auto login after signup
    this.login(email, password);
    return newUser;
  },

  // Log in a user
  login(email, password) {
    const users = JSON.parse(localStorage.getItem(USERS_KEY) || '[]');
    const user = users.find(u => u.email === email && u.password === password);

    if (!user) {
      throw new Error('Invalid email or password.');
    }

    // Store session (exclude password)
    const sessionData = { 
      username: user.username, 
      email: user.email, 
      id: user.id,
      loginAt: new Date().toISOString()
    };
    localStorage.setItem(AUTH_KEY, JSON.stringify(sessionData));
    return sessionData;
  },

  // Log in a user via Google
  loginWithGoogle(payload) {
    const users = JSON.parse(localStorage.getItem(USERS_KEY) || '[]');
    let user = users.find(u => u.email === payload.email);

    if (!user) {
      // Auto-signup for Google users
      user = { 
        username: payload.name, 
        email: payload.email, 
        picture: payload.picture,
        id: 'google_' + payload.sub,
        source: 'google'
      };
      users.push(user);
      localStorage.setItem(USERS_KEY, JSON.stringify(users));
    }

    const sessionData = { 
      username: user.username, 
      email: user.email, 
      picture: user.picture,
      id: user.id,
      loginAt: new Date().toISOString()
    };
    localStorage.setItem(AUTH_KEY, JSON.stringify(sessionData));
    return sessionData;
  },

  // Log out
  logout() {
    localStorage.removeItem(AUTH_KEY);
    window.location.href = 'login.html';
  },

  // Security check for protected pages
  protect() {
    if (!this.isAuthenticated()) {
      // Don't redirect if we're already on login page
      if (!window.location.pathname.includes('login.html')) {
        window.location.href = 'login.html';
      }
    }
  }
};

// Automatic protection check on load
if (typeof window !== 'undefined') {
  AuthService.protect();
}
