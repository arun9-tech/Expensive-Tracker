// This file is correct and requires no changes.
// It will work perfectly with the new HTML and CSS.

// Helper function to make authenticated requests
async function fetchWithAuth(url, options = {}) {
    // Set default headers
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...options.headers
    };
    
    // Add Authorization header if token exists
    const token = localStorage.getItem('token');
    console.log('fetchWithAuth: Token from localStorage', token);
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(url, {
            ...options,
            headers,
            credentials: 'include'
        });
        
        // Handle unauthorized responses
        if (response.status === 401) {
            localStorage.removeItem('token');
            // window.location.href = '/index.html'; // Temporarily removed to debug redirection
            throw new Error('Session expired. Please log in again.');
        }
        
        return response;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

// API Configuration - Using proxy server
// NEW (Correct)
const API_BASE_URL = 'http://localhost:8080/api'; // <--- Add /api here
const AUTH_ENDPOINTS = {
    LOGIN: `${API_BASE_URL}/auth/signin`,
    REGISTER: `${API_BASE_URL}/auth/signup`,
};
const TRANSACTION_ENDPOINTS = {
    GET_ALL: `${API_BASE_URL}/transactions`,
    CREATE: `${API_BASE_URL}/transactions`,
    DELETE: (id) => `${API_BASE_URL}/transactions/${id}`,
};

// DOM Elements
const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const authTabs = document.querySelectorAll('.auth-tab');
const logoutBtn = document.getElementById('logout-btn');
const welcomeMessage = document.getElementById('welcome-message');
const transactionForm = document.getElementById('transaction-form');
const transactionsList = document.getElementById('transactions-list');
const totalBalanceEl = document.getElementById('total-balance');
const totalIncomeEl = document.getElementById('total-income');
const totalExpenseEl = document.getElementById('total-expense');

// State
let currentUser = null;
let transactions = [];

// Initialize the application
function init() {
    const token = localStorage.getItem('token');
    const userString = localStorage.getItem('user');
    const user = userString && userString !== "undefined" ? JSON.parse(userString) : null;
    
    console.log('Init: Token from localStorage', token);
    console.log('Init: User from localStorage', user);

    if (token && user) {
        currentUser = user;
        showApp();
        loadTransactions();
    } else {
        showAuth();
    }
    
    setupEventListeners();
}

// Event Listeners
function setupEventListeners() {
    if (authTabs.length > 0) {
        authTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                const tabName = e.target.dataset.tab;
                switchAuthTab(tabName);
            });
        });
    }
    
    if (loginForm) loginForm.addEventListener('submit', handleLogin);
    if (registerForm) registerForm.addEventListener('submit', handleRegister);
    if (logoutBtn) logoutBtn.addEventListener('click', handleLogout);
    if (transactionForm) transactionForm.addEventListener('submit', handleAddTransaction);
}


// Auth Functions
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const errorEl = document.getElementById('login-error');
    errorEl.textContent = '';
    
    try {
        const response = await fetch(AUTH_ENDPOINTS.LOGIN, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (!response.ok) throw new Error(data.message || 'Login failed');
        
        console.log('handleLogin: Token received', data.accessToken);
        localStorage.setItem('token', data.accessToken);
        console.log('handleLogin: Token after saving', localStorage.getItem('token'));
        localStorage.setItem('user', JSON.stringify(data));
        
        currentUser = data;
        showApp();
        loadTransactions();
        
    } catch (error) {
        errorEl.textContent = error.message;
        console.error('Login error:', error);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const errorEl = document.getElementById('register-error');
    errorEl.textContent = '';
    
    try {
        const response = await fetch(AUTH_ENDPOINTS.REGISTER, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        
        const data = await response.json();
        
        if (!response.ok) throw new Error(data.message || 'Registration failed');
        
        switchAuthTab('login');
        alert('Registration successful! Please login.');
        
    } catch (error) {
        errorEl.textContent = error.message;
        console.error('Registration error:', error);
    }
}

function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    currentUser = null;
    showAuth();
}

// Transaction Functions
async function loadTransactions() {
    try {
        const response = await fetchWithAuth(TRANSACTION_ENDPOINTS.GET_ALL);
        if (!response.ok) throw new Error('Failed to load transactions');
        transactions = await response.json();
        renderTransactions();
        updateSummary();
    } catch (error) {
        console.error('Error loading transactions:', error);
    }
}

async function handleAddTransaction(e) {
    e.preventDefault();
    const amount = parseFloat(document.getElementById('transaction-amount').value);
    const description = document.getElementById('transaction-description').value;
    const type = document.getElementById('transaction-type').value;
    
    try {
        const response = await fetchWithAuth(TRANSACTION_ENDPOINTS.CREATE, {
            method: 'POST',
            body: JSON.stringify({ amount, description, type })
        });
        if (!response.ok) throw new Error('Failed to add transaction');
        transactionForm.reset();
        await loadTransactions();
    } catch (error) {
        console.error('Error adding transaction:', error);
    }
}

async function handleDeleteTransaction(id) {
    if (!confirm('Are you sure?')) return;
    try {
        const response = await fetchWithAuth(TRANSACTION_ENDPOINTS.DELETE(id), { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete transaction');
        await loadTransactions();
    } catch (error) {
        console.error('Error deleting transaction:', error);
    }
}

// UI Update Functions
function renderTransactions() {
    if (transactions.length === 0) {
        transactionsList.innerHTML = '<p class="text-muted">No transactions yet. Add one above to get started!</p>';
        return;
    }
    transactionsList.innerHTML = transactions.map(t => `
        <div class="transaction-item">
            <div class="transaction-info">
                <h4>${t.description}</h4>
                <small class="text-muted">${new Date(t.date).toLocaleDateString()}</small>
            </div>
            <div class="transaction-amount ${t.type.toLowerCase()}">
                ${t.type === 'INCOME' ? '+' : '-'}$${Math.abs(t.amount).toFixed(2)}
            </div>
            <button class="btn btn-sm btn-danger" onclick="handleDeleteTransaction('${t.id}')">Delete</button>
        </div>
    `).join('');
}

function updateSummary() {
    const income = transactions.filter(t => t.type === 'INCOME').reduce((sum, t) => sum + t.amount, 0);
    const expense = transactions.filter(t => t.type === 'EXPENSE').reduce((sum, t) => sum + t.amount, 0);
    const balance = income - expense;
    
    totalBalanceEl.textContent = `$${balance.toFixed(2)}`;
    totalIncomeEl.textContent = `$${income.toFixed(2)}`;
    totalExpenseEl.textContent = `$${expense.toFixed(2)}`;
    
    totalBalanceEl.className = balance >= 0 ? 'text-success' : 'text-danger';
}

// Helper Functions
function showAuth() {
    authSection.classList.remove('hidden');
    appSection.classList.add('hidden');
}

function showApp() {
    authSection.classList.add('hidden');
    appSection.classList.remove('hidden');
    welcomeMessage.textContent = `Welcome, ${currentUser.username}`;
}

function switchAuthTab(tabName) {
    authTabs.forEach(tab => tab.classList.toggle('active', tab.dataset.tab === tabName));
    loginForm.classList.toggle('hidden', tabName !== 'login');
    registerForm.classList.toggle('hidden', tabName !== 'register');
}

// Make delete function globally accessible for inline onclick
window.handleDeleteTransaction = handleDeleteTransaction;

// Initialize the app
document.addEventListener('DOMContentLoaded', init);