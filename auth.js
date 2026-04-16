/**
 * FlowDash Authentication Service
 * Handles user sessions and authorization state.
 * For production, replace this with a backend (e.g. Firebase).
 */

const AUTH_KEY = 'flowdash_user_session';
const USERS_KEY = 'flowdash_registered_users';

// --- SHARED CONFIG ---
const CLIENT_ID = '216540498622-2ccjhuedqhpkelaea8nv2suaicps07ov.apps.googleusercontent.com'; 
const API_KEY = 'AIzaSyDD8NYtI5kgrTEfaqX3Uiq6ovUBiXp83Dc';
const DISCOVERY_DOC = 'https://www.googleapis.com/discovery/v1/apis/drive/v3/rest';
const SCOPES = 'https://www.googleapis.com/auth/drive.appdata';

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
    localStorage.removeItem('flowdash_drive_token');
    window.location.href = 'login.html';
  },

  // --- DRIVE SERVICE ---
  Drive: {
    gapiInited: false,
    tokenClient: null,
    driveFileId: null,

    // Initialize GAPI and GIS
    async init() {
      if (this.gapiInited) return;
      
      return new Promise((resolve) => {
        // Load GAPI
        const script = document.createElement('script');
        script.src = "https://apis.google.com/js/api.js";
        script.onload = () => {
          gapi.load('client', async () => {
            await gapi.client.init({ apiKey: API_KEY, discoveryDocs: [DISCOVERY_DOC] });
            this.gapiInited = true;
            console.log("Drive GAPI Ready");
            
            // Load GIS
            const gisScript = document.createElement('script');
            gisScript.src = "https://accounts.google.com/gsi/client";
            gisScript.onload = () => {
              this.tokenClient = google.accounts.oauth2.initTokenClient({
                client_id: CLIENT_ID, scope: SCOPES, callback: (resp) => this.onTokenResponse(resp)
              });
              console.log("Drive GIS Ready");
              
              // Auto-reconnect if token exists
              const savedToken = localStorage.getItem('flowdash_drive_token');
              if (savedToken) {
                gapi.client.setToken({ access_token: savedToken });
                this.findOrCreateSaveFile().then(resolve);
              } else {
                resolve();
              }
            };
            document.head.appendChild(gisScript);
          });
        };
        document.head.appendChild(script);
      });
    },

    async authorize() {
      if (!this.tokenClient) await this.init();
      this.tokenClient.requestAccessToken({ prompt: '' });
    },

    onTokenResponse(resp) {
      if (resp.error) return console.error("Drive Auth Error:", resp.error);
      localStorage.setItem('flowdash_drive_token', resp.access_token);
      this.findOrCreateSaveFile();
    },

    async findOrCreateSaveFile() {
      try {
        let res = await gapi.client.drive.files.list({
          spaces: 'appDataFolder', q: "name='flowdash_suite_sync.json'", fields: 'files(id)'
        });
        if (res.result.files && res.result.files.length > 0) {
          this.driveFileId = res.result.files[0].id;
        } else {
          let metadata = { name: 'flowdash_suite_sync.json', parents: ['appDataFolder'] };
          let file = new Blob([JSON.stringify({ goals:[], habits:[], vault:[] })], {type: 'application/json'});
          let formData = new FormData();
          formData.append('metadata', new Blob([JSON.stringify(metadata)], {type: 'application/json'}));
          formData.append('file', file);
          let fetchRes = await fetch('https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart', {
            method: 'POST',
            headers: new Headers({'Authorization': 'Bearer ' + gapi.client.getToken().access_token}),
            body: formData
          });
          let json = await fetchRes.json();
          this.driveFileId = json.id;
        }
      } catch (e) { console.error("Drive File Init Error", e); }
    },

    async sync(key, data) {
      if (!this.driveFileId) return data; // Return local if not synced
      
      try {
        // 1. Get current full state
        let res = await gapi.client.drive.files.get({ fileId: this.driveFileId, alt: 'media' });
        let fullState = res.body ? JSON.parse(res.body) : { goals:[], habits:[], vault:[] };
        
        // 2. Merge local data
        fullState[key] = data;
        
        // 3. Push back to Drive
        await fetch(`https://www.googleapis.com/upload/drive/v3/files/${this.driveFileId}?uploadType=media`, {
          method: 'PATCH',
          headers: new Headers({
            'Authorization': 'Bearer ' + gapi.client.getToken().access_token,
            'Content-Type': 'application/json'
          }),
          body: JSON.stringify(fullState)
        });
        
        return fullState[key];
      } catch (e) {
        console.error("Drive Sync Error", e);
        return data;
      }
    }
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
