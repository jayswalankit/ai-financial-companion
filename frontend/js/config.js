/* ---------------- STATE ---------------- */
function resolveDefaultApiBase() {
  if (typeof window !== 'undefined' && window.location && window.location.origin && window.location.origin !== 'null') {
    const origin = window.location.origin;
    const isLocalFrontend =
      origin.includes('localhost:63342') ||
      origin.includes('127.0.0.1:63342') ||
      origin.includes('localhost:5500') ||
      origin.includes('127.0.0.1:5500');

    if (isLocalFrontend) {
      return 'http://localhost:8080';
    }

    return origin;
  }
  return 'http://localhost:8080';
}

const state = {
  apiBase: resolveDefaultApiBase(),
  token: null,
  user: null, // {username, email}
  categories: [],
  preferences: null, // {userMode, notificationMode}
  customModes: [],
  theme: 'light',
  page: 'dashboard',
  pendingSignupEmail: null,
  pendingResetEmail: null,
  expenseSaveInProgress: false,
  recordsFilter: { keyword:'', categoryId:'', startDate:'', endDate:'', page:0, size:10, categoryLock:null, dateFromSearch:false },
  reportsTab: 'weekly', // weekly | monthly | custom
  reportsView: 'bar', // bar | donut | trend | stats | table
  reportsSearch: '',
  reportsCustomRange: { start:'', end:'' },
  charts: {},
};

const PAGE_META = {
  dashboard:   {title:'Dashboard', sub:'Your spending, at a glance'},
  categories:  {title:'Categories', sub:'Organise where your money goes'},
  records:     {title:'Records', sub:'Every expense, searchable'},
  reports:     {title:'Reports', sub:'Weekly, monthly, and custom breakdowns'},
  budget:      {title:'Budget', sub:'Set and track your monthly limit'},
  notifications:{title:'Notifications', sub:'Alerts and summaries'},
  settings:    {title:'Settings', sub:'Profile, modes, and preferences'},
};

const CHART_COLORS = ['#1f7a5c','#c8912f','#b34a30','#3f6b8f','#7a5c9e','#5c8a2f','#9e5c7a','#2f8a8a'];

const budgetSel = { month: new Date().getMonth()+1, year: new Date().getFullYear() };

const MONTH_NAMES = ['January','February','March','April','May','June','July','August','September','October','November','December'];
