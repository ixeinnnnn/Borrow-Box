<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Barangay Resources Borrowing System</title>
<link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Mono:wght@400;500&family=DM+Sans:ital,wght@0,300;0,400;0,500;1,400&display=swap" rel="stylesheet">
<style>
  :root {
    --bg: #0d1117;
    --surface: #161b22;
    --surface2: #1c2333;
    --border: #30363d;
    --accent: #2ea44f;
    --accent2: #58a6ff;
    --accent3: #f78166;
    --text: #e6edf3;
    --muted: #8b949e;
    --code-bg: #161b22;
  }

  * { margin: 0; padding: 0; box-sizing: border-box; }

  body {
    background: var(--bg);
    color: var(--text);
    font-family: 'DM Sans', sans-serif;
    font-size: 15px;
    line-height: 1.7;
  }

  /* Layout */
  .wrapper {
    max-width: 900px;
    margin: 0 auto;
    padding: 0 24px 80px;
  }

  /* Hero */
  .hero {
    padding: 60px 0 48px;
    border-bottom: 1px solid var(--border);
    margin-bottom: 48px;
  }

  .hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(46,164,79,0.12);
    border: 1px solid rgba(46,164,79,0.3);
    color: var(--accent);
    font-family: 'DM Mono', monospace;
    font-size: 11px;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    padding: 4px 12px;
    border-radius: 20px;
    margin-bottom: 20px;
  }

  .hero-badge::before { content: '●'; font-size: 8px; }

  h1 {
    font-family: 'Syne', sans-serif;
    font-size: clamp(28px, 5vw, 44px);
    font-weight: 800;
    line-height: 1.1;
    letter-spacing: -0.02em;
    margin-bottom: 16px;
    background: linear-gradient(135deg, #e6edf3 0%, #8b949e 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .hero-desc {
    font-size: 16px;
    color: var(--muted);
    max-width: 560px;
    line-height: 1.6;
    margin-bottom: 28px;
  }

  .badge-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .badge {
    font-family: 'DM Mono', monospace;
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 4px;
    border: 1px solid var(--border);
    color: var(--muted);
    background: var(--surface);
  }

  .badge.green { border-color: rgba(46,164,79,0.4); color: var(--accent); background: rgba(46,164,79,0.08); }
  .badge.blue  { border-color: rgba(88,166,255,0.4); color: var(--accent2); background: rgba(88,166,255,0.08); }
  .badge.red   { border-color: rgba(247,129,102,0.4); color: var(--accent3); background: rgba(247,129,102,0.08); }

  /* Section headings */
  h2 {
    font-family: 'Syne', sans-serif;
    font-size: 20px;
    font-weight: 700;
    color: var(--text);
    margin: 48px 0 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  h2::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border);
  }

  h3 {
    font-family: 'Syne', sans-serif;
    font-size: 14px;
    font-weight: 600;
    color: var(--accent2);
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin: 28px 0 10px;
  }

  /* Feature cards */
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }

  .feature-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 16px;
    transition: border-color 0.2s;
  }

  .feature-card:hover { border-color: var(--accent2); }

  .feature-card .icon {
    font-size: 22px;
    margin-bottom: 10px;
    display: block;
  }

  .feature-card strong {
    display: block;
    font-family: 'Syne', sans-serif;
    font-size: 13px;
    font-weight: 600;
    color: var(--text);
    margin-bottom: 6px;
  }

  .feature-card p {
    font-size: 12px;
    color: var(--muted);
    line-height: 1.5;
  }

  /* Tech stack table */
  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    margin: 4px 0;
  }

  th {
    text-align: left;
    font-family: 'DM Mono', monospace;
    font-size: 11px;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--muted);
    padding: 8px 12px;
    border-bottom: 1px solid var(--border);
  }

  td {
    padding: 10px 12px;
    border-bottom: 1px solid rgba(48,54,61,0.5);
    vertical-align: top;
  }

  tr:last-child td { border-bottom: none; }

  td:first-child {
    font-family: 'DM Mono', monospace;
    font-size: 12px;
    color: var(--muted);
    white-space: nowrap;
    width: 140px;
  }

  /* Steps */
  .steps { counter-reset: step; display: flex; flex-direction: column; gap: 2px; }

  .step {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 8px;
    transition: border-color 0.2s;
  }

  .step:hover { border-color: var(--accent); }

  .step-num {
    counter-increment: step;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: rgba(46,164,79,0.15);
    border: 1px solid rgba(46,164,79,0.3);
    color: var(--accent);
    font-family: 'DM Mono', monospace;
    font-size: 12px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .step-content strong {
    display: block;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .step-content p, .step-content ul {
    font-size: 13px;
    color: var(--muted);
  }

  .step-content ul { padding-left: 16px; }
  .step-content li { margin-bottom: 2px; }

  /* Credentials */
  .cred-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  @media (max-width: 500px) { .cred-grid { grid-template-columns: 1fr; } }

  .cred-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 16px 20px;
  }

  .cred-card .role {
    font-family: 'Syne', sans-serif;
    font-size: 13px;
    font-weight: 700;
    color: var(--muted);
    text-transform: uppercase;
    letter-spacing: 0.06em;
    margin-bottom: 10px;
  }

  .cred-row { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; }
  .cred-label { color: var(--muted); }
  .cred-value { font-family: 'DM Mono', monospace; font-size: 12px; color: var(--accent); }

  .warning {
    margin-top: 12px;
    padding: 10px 14px;
    background: rgba(247,129,102,0.08);
    border: 1px solid rgba(247,129,102,0.25);
    border-radius: 6px;
    font-size: 12px;
    color: var(--accent3);
  }

  /* Code blocks */
  pre {
    background: var(--code-bg);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 16px 20px;
    overflow-x: auto;
    font-family: 'DM Mono', monospace;
    font-size: 12px;
    line-height: 1.6;
    color: #c9d1d9;
    margin: 12px 0;
  }

  code {
    font-family: 'DM Mono', monospace;
    font-size: 12px;
    background: var(--surface2);
    padding: 2px 6px;
    border-radius: 4px;
    color: var(--accent2);
  }

  pre code { background: none; padding: 0; color: inherit; }

  /* Troubleshoot */
  .trouble-grid { display: flex; flex-direction: column; gap: 2px; }

  .trouble-item {
    display: flex;
    align-items: baseline;
    gap: 16px;
    padding: 12px 16px;
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 6px;
    font-size: 13px;
  }

  .trouble-issue {
    font-family: 'DM Mono', monospace;
    font-size: 12px;
    color: var(--accent3);
    min-width: 200px;
  }

  .trouble-fix { color: var(--muted); }

  /* Roadmap */
  .roadmap { display: flex; flex-direction: column; gap: 4px; }

  .roadmap-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 6px;
    font-size: 13px;
    border: 1px solid transparent;
    transition: border-color 0.2s;
  }

  .roadmap-item:hover { border-color: var(--border); background: var(--surface); }

  .check { width: 16px; height: 16px; border: 1.5px solid var(--border); border-radius: 3px; flex-shrink: 0; }

  /* Footer */
  .footer {
    margin-top: 64px;
    padding-top: 24px;
    border-top: 1px solid var(--border);
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 12px;
    color: var(--muted);
    font-family: 'DM Mono', monospace;
  }

  /* Links */
  a { color: var(--accent2); text-decoration: none; }
  a:hover { text-decoration: underline; }

  p { margin-bottom: 8px; }
</style>
</head>
<body>
<div class="wrapper">

  <!-- Hero -->
  <div class="hero">
    <div class="hero-badge">Android · Firebase · Java</div>
    <h1>Barangay Resources<br>Borrowing System</h1>
    <p class="hero-desc">A community resource management app for Philippine barangays. Enables residents to borrow equipment and officials to manage inventory — all from their phones.</p>
    <div class="badge-row">
      <span class="badge green">v1.0.0</span>
      <span class="badge blue">API 36</span>
      <span class="badge blue">Java 11+</span>
      <span class="badge">MIT License</span>
      <span class="badge">April 2026</span>
    </div>
  </div>

  <!-- Features -->
  <h2>✨ Features</h2>
  <div class="feature-grid">
    <div class="feature-card">
      <span class="icon">🔐</span>
      <strong>Authentication</strong>
      <p>Email/password login, forgot password, admin & user flows, session management</p>
    </div>
    <div class="feature-card">
      <span class="icon">📦</span>
      <strong>Resource Browsing</strong>
      <p>Browse available items by category, check availability, submit borrow requests</p>
    </div>
    <div class="feature-card">
      <span class="icon">📋</span>
      <strong>Borrowing Tracker</strong>
      <p>View active borrows, return history, and overdue items per user</p>
    </div>
    <div class="feature-card">
      <span class="icon">🛠️</span>
      <strong>Admin Dashboard</strong>
      <p>Manage users and items, view system stats and reports</p>
    </div>
    <div class="feature-card">
      <span class="icon">👤</span>
      <strong>User Profiles</strong>
      <p>Personal info, borrow stats, contact details</p>
    </div>
    <div class="feature-card">
      <span class="icon">🔒</span>
      <strong>Role-based Access</strong>
      <p>Separate admin and resident roles with scoped permissions</p>
    </div>
  </div>

  <!-- Tech Stack -->
  <h2>🧱 Tech Stack</h2>
  <div style="background:var(--surface);border:1px solid var(--border);border-radius:8px;overflow:hidden;">
    <table>
      <thead><tr><th>Layer</th><th>Technology</th></tr></thead>
      <tbody>
        <tr><td>Language</td><td>Java</td></tr>
        <tr><td>UI</td><td>XML + Material Design, CardView, RecyclerView, Poppins font</td></tr>
        <tr><td>Auth</td><td>Firebase Authentication (Email/Password)</td></tr>
        <tr><td>Database</td><td>Firebase Realtime Database</td></tr>
        <tr><td>Analytics</td><td>Firebase Analytics</td></tr>
        <tr><td>Architecture</td><td>MVC pattern, separate Activities per screen</td></tr>
        <tr><td>Min SDK</td><td>API 36</td></tr>
      </tbody>
    </table>
  </div>

  <!-- Project Structure -->
  <h2>📁 Project Structure</h2>
  <pre>app/src/main/
├── java/com/example/barrowing_system/
│   ├── MainActivity.java               # Entry point & routing
│   ├── LoginActivity.java              # User login
│   ├── AdminLoginActivity.java         # Admin login
│   ├── RegisterActivity.java           # Registration
│   ├── ForgotPasswordActivity.java     # Password reset
│   ├── UserDashboardActivity.java      # User dashboard
│   ├── AdminDashboardActivity.java     # Admin dashboard
│   ├── User.java                       # User model
│   └── SessionManager.java             # Session handling
└── res/
    ├── layout/                         # XML layouts
    ├── drawable/                       # Icons & graphics
    ├── values/                         # Strings, colors, styles
    └── font/                           # Poppins font files</pre>

  <!-- Getting Started -->
  <h2>🚀 Getting Started</h2>

  <h3>Prerequisites</h3>
  <p style="color:var(--muted);font-size:13px;margin-bottom:16px;">Android Studio (latest) · Java 11+ · Android SDK API 36 · Firebase project</p>

  <div class="steps">
    <div class="step">
      <div class="step-num">1</div>
      <div class="step-content">
        <strong>Open the Project</strong>
        <ul>
          <li>Launch Android Studio</li>
          <li>Select <em>Open an existing project</em></li>
          <li>Navigate to the project folder and wait for Gradle sync</li>
        </ul>
      </div>
    </div>
    <div class="step">
      <div class="step-num">2</div>
      <div class="step-content">
        <strong>Set Up Firebase</strong>
        <ul>
          <li>Go to <a href="https://console.firebase.google.com/" target="_blank">Firebase Console</a> and create a project</li>
          <li>Register an Android app with package name <code>com.example.barrowing_system</code></li>
          <li>Download <code>google-services.json</code> and place it in <code>app/</code></li>
          <li>Enable <strong>Authentication → Email/Password</strong></li>
          <li>Enable <strong>Realtime Database</strong> (test mode for dev)</li>
        </ul>
      </div>
    </div>
    <div class="step">
      <div class="step-num">3</div>
      <div class="step-content">
        <strong>Run the App</strong>
        <p>Connect a device or start an emulator, then click <strong>Run</strong> in Android Studio.</p>
      </div>
    </div>
  </div>

  <!-- Credentials -->
  <h2>🔑 Default Credentials</h2>
  <div class="cred-grid">
    <div class="cred-card">
      <div class="role">Admin</div>
      <div class="cred-row"><span class="cred-label">Email</span><span class="cred-value">admin@barangay.com</span></div>
      <div class="cred-row"><span class="cred-label">Password</span><span class="cred-value">admin123</span></div>
    </div>
    <div class="cred-card">
      <div class="role">User</div>
      <div class="cred-row"><span class="cred-label">Email</span><span class="cred-value">Register via app</span></div>
      <div class="cred-row"><span class="cred-label">Password</span><span class="cred-value">Min. 6 characters</span></div>
    </div>
  </div>
  <div class="warning">⚠️ Change the default admin credentials before deploying to production.</div>

  <!-- Database Schema -->
  <h2>🗄️ Database Schema</h2>

  <h3>users/{userId}</h3>
  <pre>{
  "uid": "firebase_uid",
  "fullName": "Juan Dela Cruz",
  "email": "juan@example.com",
  "role": "user",            // "user" | "admin"
  "phoneNumber": "+639171234567",
  "address": "123 Rizal St",
  "createdAt": 1640995200000,
  "isActive": true,
  "totalBorrowings": 5,
  "activeBorrowings": 2
}</pre>

  <h3>items/{itemId}</h3>
  <pre>{
  "name": "Projector",
  "category": "Electronics",
  "status": "available",     // "available" | "borrowed" | "maintenance"
  "borrowedBy": "userId",
  "borrowedAt": 1640995200000,
  "returnBy": 1641081600000
}</pre>

  <h3>borrowings/{userId}/{borrowingId}</h3>
  <pre>{
  "itemId": "itemId",
  "borrowedAt": 1640995200000,
  "returnBy": 1641081600000,
  "returnedAt": null,
  "status": "active"         // "active" | "returned" | "overdue"
}</pre>

  <!-- Troubleshooting -->
  <h2>🐛 Troubleshooting</h2>
  <div class="trouble-grid">
    <div class="trouble-item"><span class="trouble-issue">Gradle sync fails</span><span class="trouble-fix">Check internet connection and confirm <code>google-services.json</code> is in <code>app/</code></span></div>
    <div class="trouble-item"><span class="trouble-issue">Login not working</span><span class="trouble-fix">Verify Email/Password is enabled in Firebase Authentication</span></div>
    <div class="trouble-item"><span class="trouble-issue">Data not loading</span><span class="trouble-fix">Check Firebase Realtime Database security rules</span></div>
    <div class="trouble-item"><span class="trouble-issue">Write permission denied</span><span class="trouble-fix">Review database rules in Firebase Console</span></div>
    <div class="trouble-item"><span class="trouble-issue">Session lost</span><span class="trouble-fix">Verify SharedPreferences are accessible on the device</span></div>
    <div class="trouble-item"><span class="trouble-issue">Missing dependencies</span><span class="trouble-fix">Verify Firebase SDK versions in <code>build.gradle.kts</code> and re-sync</span></div>
  </div>

  <!-- Roadmap -->
  <h2>🗺️ Roadmap</h2>
  <div class="roadmap">
    <div class="roadmap-item"><div class="check"></div>Google Sign-In integration</div>
    <div class="roadmap-item"><div class="check"></div>Push notifications for due-date reminders</div>
    <div class="roadmap-item"><div class="check"></div>Offline mode with local SQLite cache</div>
    <div class="roadmap-item"><div class="check"></div>QR / barcode scanning for items</div>
    <div class="roadmap-item"><div class="check"></div>Filipino language support</div>
    <div class="roadmap-item"><div class="check"></div>Dark mode</div>
    <div class="roadmap-item"><div class="check"></div>Export reports to PDF / Excel</div>
    <div class="roadmap-item"><div class="check"></div>iOS version (Flutter or React Native)</div>
    <div class="roadmap-item"><div class="check"></div>Material Design 3 UI refresh</div>
  </div>

  <!-- Contributing -->
  <h2>🤝 Contributing</h2>
  <p style="color:var(--muted);font-size:13px;margin-bottom:12px;">Fork the repo, create a feature branch, commit your changes, and open a pull request.</p>
  <pre>git checkout -b feature/your-feature-name
git commit -m "feat: describe your change"
git push origin feature/your-feature-name</pre>
  <p style="font-size:13px;color:var(--muted);">Follow Java naming conventions, keep methods under 30 lines, and add Javadoc comments to all public methods.</p>

 

  <!-- Footer -->
  <div class="footer">
    <span>Barangay Resources Borrowing System</span>
    <span>v1.0.0 · April 2026 · MIT License</span>
  </div>

</div>
</body>
</html>
