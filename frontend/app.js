/*
  ARENA FINDER — CORE APPLICATION LOGIC
  Handles Auth, Search, Bookings, AI Chat, and offline Sandbox Simulation
*/

const API_BASE_URL = 'http://localhost:8080';

// Global Reactive State
const state = {
    currentUser: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
    arenas: [],
    myBookings: [],
    activeSection: 'home-section',
    activeBookingArena: null,
    selectedSlot: null,
    isDemoMode: false,
    aiChatHistory: [
        {
            role: 'assistant',
            text: '👋 Hey! I am your **Arena Finder AI Assistant**. I can recommend the best venues, check slot prices, or help you find sports activities nearby. What are you looking to play today?'
        }
    ]
};

// Static Mock Data for Sandbox Demo Mode
const MOCK_ARENAS = [
    {
        id: 1,
        name: 'Champions Turf',
        address: 'Lanka, Varanasi',
        city: 'Varanasi',
        latitude: 25.3176,
        longitude: 82.9739,
        sport: 'FOOTBALL',
        openTime: '06:00',
        closeTime: '22:00',
        pricePerHour: 500.0,
        ownerId: 1,
        imageUrl: 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&q=80&w=800'
    },
    {
        id: 2,
        name: 'City Badminton Hall',
        address: 'Sigra, Varanasi',
        city: 'Varanasi',
        latitude: 25.3200,
        longitude: 82.9800,
        sport: 'BADMINTON',
        openTime: '07:00',
        closeTime: '21:00',
        pricePerHour: 300.0,
        ownerId: 1,
        imageUrl: 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&q=80&w=800'
    },
    {
        id: 3,
        name: 'Sports Arena Mumbai',
        address: 'Andheri, Mumbai',
        city: 'Mumbai',
        latitude: 19.1136,
        longitude: 72.8697,
        sport: 'BOTH',
        openTime: '05:00',
        closeTime: '23:00',
        pricePerHour: 800.0,
        ownerId: 2,
        imageUrl: 'https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&q=80&w=800'
    }
];

const MOCK_SLOTS = [
    '07:00 - 08:00', '08:00 - 09:00', '09:00 - 10:00', 
    '10:00 - 11:00', '14:00 - 15:00', '16:00 - 17:00', 
    '17:00 - 18:00', '18:00 - 19:00', '19:00 - 20:00',
    '20:00 - 21:00', '21:00 - 22:00'
];

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
    initNavigation();
    initAuth();
    initSearch();
    initBooking();
    initAiAssistant();
    
    // Check if backend is available, switch to Demo Mode if offline
    await checkBackendAvailability();
    loadArenas();
});

// Check if microservices are online
async function checkBackendAvailability() {
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 2000);
        
        // Try hitting api-gateway health or just fetch arenas
        const response = await fetch(`${API_BASE_URL}/arenas`, { 
            signal: controller.signal 
        });
        clearTimeout(timeoutId);
        
        state.isDemoMode = false;
        logDebug('Connected to backend API cluster.');
    } catch (e) {
        state.isDemoMode = true;
        logDebug('Backend offline. Operating in Sandbox Demo Mode.');
        showToast('Running in local Demo Sandbox Mode.', 'info');
    }
}

// Navigation Controls
function initNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const target = link.getAttribute('data-target');
            switchSection(target);
        });
    });
}

function switchSection(sectionId) {
    if (sectionId === 'bookings-section' && !state.currentUser) {
        showToast('Please login to view bookings.', 'warning');
        openModal('login-modal');
        return;
    }

    state.activeSection = sectionId;
    
    document.querySelectorAll('.app-section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('data-target') === sectionId) {
            link.classList.add('active');
        }
    });

    const activeSec = document.getElementById(sectionId);
    if (activeSec) {
        activeSec.classList.add('active');
    }

    if (sectionId === 'bookings-section') {
        loadBookings();
    }
}

// Authentication Logic
function initAuth() {
    const loginModal = document.getElementById('login-modal');
    const registerModal = document.getElementById('register-modal');

    // Open/Close buttons
    document.getElementById('btn-login-open').addEventListener('click', () => openModal('login-modal'));
    document.getElementById('btn-register-open').addEventListener('click', () => openModal('register-modal'));
    document.getElementById('btn-close-login-modal').addEventListener('click', () => closeModal('login-modal'));
    document.getElementById('btn-close-register-modal').addEventListener('click', () => closeModal('register-modal'));
    
    // Switch links
    document.getElementById('link-to-register').addEventListener('click', (e) => {
        e.preventDefault();
        closeModal('login-modal');
        openModal('register-modal');
    });
    document.getElementById('link-to-login').addEventListener('click', (e) => {
        e.preventDefault();
        closeModal('register-modal');
        openModal('login-modal');
    });

    // Form Submissions
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        if (state.isDemoMode) {
            // Mock Login
            const name = email.split('@')[0];
            const mockUser = {
                id: 99,
                name: name.charAt(0).toUpperCase() + name.slice(1),
                email: email,
                role: 'ROLE_USER'
            };
            handleAuthSuccess(mockUser, 'mock-jwt-token');
            closeModal('login-modal');
            showToast(`Welcome back, ${mockUser.name}!`, 'success');
        } else {
            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (!response.ok) throw new Error('Invalid email or password');

                const data = await response.json();
                handleAuthSuccess(data.user, data.token);
                closeModal('login-modal');
                showToast(`Logged in successfully!`, 'success');
            } catch (err) {
                showToast(err.message, 'danger');
            }
        }
    });

    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('register-name').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;
        const role = document.getElementById('register-role').value;

        if (state.isDemoMode) {
            const mockUser = { id: 100, name, email, role };
            handleAuthSuccess(mockUser, 'mock-jwt-token');
            closeModal('register-modal');
            showToast(`Registered successfully in sandbox!`, 'success');
        } else {
            try {
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name, email, password, role })
                });

                if (!response.ok) throw new Error('Registration failed. Email might exist.');

                const data = await response.json();
                handleAuthSuccess(data.user, data.token);
                closeModal('register-modal');
                showToast(`Welcome, ${name}! Your account is ready.`, 'success');
            } catch (err) {
                showToast(err.message, 'danger');
            }
        }
    });

    // Render profile badge if logged in
    updateAuthHeader();
}

function handleAuthSuccess(user, token) {
    state.currentUser = user;
    state.token = token;
    localStorage.setItem('user', JSON.stringify(user));
    localStorage.setItem('token', token);
    updateAuthHeader();
    if (state.activeSection === 'bookings-section') {
        loadBookings();
    }
}

function handleLogout() {
    state.currentUser = null;
    state.token = null;
    state.myBookings = [];
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    updateAuthHeader();
    switchSection('home-section');
    showToast('Logged out successfully.', 'info');
}

function updateAuthHeader() {
    const container = document.getElementById('auth-header-container');
    if (state.currentUser) {
        const initials = state.currentUser.name ? state.currentUser.name.charAt(0).toUpperCase() : 'U';
        container.innerHTML = `
            <div class="user-profile-badge">
                <div class="avatar">${initials}</div>
                <div class="user-info-text">
                    <span class="user-name">${state.currentUser.name}</span>
                    <span class="user-role">${state.currentUser.role === 'ROLE_OWNER' ? 'Arena Owner' : 'Player'}</span>
                </div>
                <button class="btn-logout" id="btn-logout" title="Log Out">
                    <i class="fa-solid fa-power-off"></i>
                </button>
            </div>
        `;
        document.getElementById('btn-logout').addEventListener('click', handleLogout);
    } else {
        container.innerHTML = `
            <button class="btn btn-outline" id="btn-login-open"><i class="fa-solid fa-arrow-right-to-bracket"></i> Login</button>
            <button class="btn btn-primary" id="btn-register-open"><i class="fa-solid fa-user-plus"></i> Sign Up</button>
        `;
        document.getElementById('btn-login-open').addEventListener('click', () => openModal('login-modal'));
        document.getElementById('btn-register-open').addEventListener('click', () => openModal('register-modal'));
    }
}

// Arenas Searching & Listing
function initSearch() {
    // Range Slider
    const range = document.getElementById('search-radius');
    const radVal = document.getElementById('radius-value');
    range.addEventListener('input', (e) => {
        radVal.innerText = `${e.target.value} Km`;
    });

    // Search trigger
    document.getElementById('btn-search').addEventListener('click', () => {
        loadArenas();
    });

    // Chips filter triggers
    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            
            const filter = chip.getAttribute('data-filter');
            filterArenasBySport(filter);
        });
    });
}

async function loadArenas() {
    const city = document.getElementById('search-city').value.trim();
    const sport = document.getElementById('search-sport').value;
    const loader = document.getElementById('arenas-loader');
    const container = document.getElementById('arenas-container');

    if (loader) loader.style.display = 'flex';
    
    // Clear old cards
    document.querySelectorAll('.arena-card').forEach(c => c.remove());

    if (state.isDemoMode) {
        setTimeout(() => {
            if (loader) loader.style.display = 'none';
            state.arenas = MOCK_ARENAS;
            
            let filtered = state.arenas;
            if (city) {
                filtered = filtered.filter(a => a.city.toLowerCase() === city.toLowerCase());
            }
            if (sport) {
                filtered = filtered.filter(a => a.sport === sport || a.sport === 'BOTH');
            }
            renderArenas(filtered);
        }, 600);
    } else {
        try {
            let url = `${API_BASE_URL}/arenas`;
            const params = [];
            if (sport) params.push(`sport=${sport}`);
            if (city) params.push(`city=${city}`);
            
            if (params.length > 0) {
                url += `?${params.join('&')}`;
            }

            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to fetch arenas');
            const data = await response.json();
            
            if (loader) loader.style.display = 'none';
            state.arenas = data;
            renderArenas(data);
        } catch (err) {
            if (loader) loader.style.display = 'none';
            showToast('API fetch error. Switching to sandbox mode.', 'warning');
            state.isDemoMode = true;
            loadArenas();
        }
    }
}

function renderArenas(arenas) {
    const container = document.getElementById('arenas-container');
    const badge = document.getElementById('arenas-count-badge');
    badge.innerText = arenas.length;

    if (arenas.length === 0) {
        container.innerHTML += `
            <div class="loader-container arena-card-empty-state">
                <i class="fa-solid fa-map-location" style="font-size: 3rem; color: var(--border-glass-focus);"></i>
                <p>No arenas found matching your search. Try changing the filter.</p>
            </div>
        `;
        return;
    }

    // Remove any old empty state
    const oldEmpty = container.querySelector('.arena-card-empty-state');
    if (oldEmpty) oldEmpty.remove();

    arenas.forEach(arena => {
        const image = arena.imageUrl || getSportImagePlaceholder(arena.sport);
        const card = document.createElement('div');
        card.className = 'arena-card animated';
        card.innerHTML = `
            <div class="arena-card-img" style="background-image: url('${image}')">
                <span class="sport-badge ${arena.sport.toLowerCase()}">${arena.sport}</span>
            </div>
            <div class="arena-card-body">
                <h3 class="arena-card-title">${arena.name}</h3>
                <p class="arena-address"><i class="fa-solid fa-location-dot"></i> ${arena.address}</p>
                <div class="arena-meta">
                    <div class="arena-price">
                        <span>Rate / hr</span>
                        <span>${arena.pricePerHour} INR</span>
                    </div>
                    <button class="btn btn-primary btn-book" data-id="${arena.id}">
                        <i class="fa-solid fa-calendar-days"></i> Book Slot
                    </button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });

    // Add click listeners to book buttons
    container.querySelectorAll('.btn-book').forEach(btn => {
        btn.addEventListener('click', () => {
            const arenaId = parseInt(btn.getAttribute('data-id'));
            const selectedArena = state.arenas.find(a => a.id === arenaId);
            openBookingModal(selectedArena);
        });
    });
}

function filterArenasBySport(sport) {
    let filtered = state.arenas;
    if (sport !== 'all') {
        filtered = state.arenas.filter(a => a.sport === sport || a.sport === 'BOTH');
    }
    document.querySelectorAll('.arena-card').forEach(c => c.remove());
    renderArenas(filtered);
}

function getSportImagePlaceholder(sport) {
    if (sport === 'FOOTBALL') {
        return 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?auto=format&fit=crop&q=80&w=800';
    } else if (sport === 'BADMINTON') {
        return 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&q=80&w=800';
    } else {
        return 'https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&q=80&w=800';
    }
}

// Booking Management
function initBooking() {
    document.getElementById('btn-close-booking-modal').addEventListener('click', () => closeModal('booking-modal'));
    
    // Set default date to today
    const dateInput = document.getElementById('booking-date');
    const today = new Date().toISOString().split('T')[0];
    dateInput.setAttribute('min', today);
    dateInput.value = today;

    // Recalculate cost when duration changes
    document.getElementById('booking-duration').addEventListener('change', (e) => {
        updateBookingSummary();
    });

    // Form submit
    document.getElementById('booking-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (!state.currentUser) {
            showToast('Please login to book a slot.', 'warning');
            closeModal('booking-modal');
            openModal('login-modal');
            return;
        }

        const date = document.getElementById('booking-date').value;
        const duration = parseInt(document.getElementById('booking-duration').value);

        if (!state.selectedSlot) {
            showToast('Please select an active time slot!', 'warning');
            return;
        }

        const totalCost = state.activeBookingArena.pricePerHour * duration;

        if (state.isDemoMode) {
            // Simulated Booking
            const newBooking = {
                id: Math.floor(Math.random() * 9000) + 1000,
                arenaId: state.activeBookingArena.id,
                arenaName: state.activeBookingArena.name,
                userId: state.currentUser.id,
                bookingDate: date,
                startTime: state.selectedSlot.split(' - ')[0],
                endTime: calculateEndTime(state.selectedSlot.split(' - ')[0], duration),
                totalPrice: totalCost,
                status: 'CONFIRMED'
            };

            const savedBookings = JSON.parse(localStorage.getItem('my_bookings') || '[]');
            savedBookings.unshift(newBooking);
            localStorage.setItem('my_bookings', JSON.stringify(savedBookings));
            
            closeModal('booking-modal');
            showToast('Session Booked Successfully! Check My Bookings.', 'success');
            
            // Pop micro notification toast
            showFloatingNotification(`📅 New booking confirmed at ${state.activeBookingArena.name}!`);
        } else {
            try {
                const response = await fetch(`${API_BASE_URL}/bookings`, {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'application/json',
                        'X-User-Id': state.currentUser.id
                    },
                    body: JSON.stringify({
                        arenaId: state.activeBookingArena.id,
                        bookingDate: date,
                        startTime: state.selectedSlot.split(' - ')[0],
                        endTime: calculateEndTime(state.selectedSlot.split(' - ')[0], duration)
                    })
                });

                if (!response.ok) throw new Error('Slot already booked or invalid request');

                closeModal('booking-modal');
                showToast('Booking success! See you at the arena!', 'success');
                showFloatingNotification(`📅 Session confirmed at ${state.activeBookingArena.name}!`);
            } catch (err) {
                showToast(err.message, 'danger');
            }
        }
    });
}

function calculateEndTime(startTime, durationHours) {
    const [h, m] = startTime.split(':').map(Number);
    const endH = (h + durationHours) % 24;
    return `${endH.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`;
}

function openBookingModal(arena) {
    state.activeBookingArena = arena;
    state.selectedSlot = null;
    
    document.getElementById('modal-arena-name').innerText = arena.name;
    document.getElementById('modal-arena-sport').innerText = arena.sport;
    document.getElementById('modal-arena-address').innerText = arena.address;
    document.getElementById('modal-arena-price').innerText = arena.pricePerHour;
    
    // Reset duration to 1 hour
    document.getElementById('booking-duration').value = "1";

    // Dynamic slot injection
    const container = document.getElementById('slots-container');
    container.innerHTML = '';
    
    MOCK_SLOTS.forEach(slot => {
        const btn = document.createElement('button');
        btn.className = 'slot-btn';
        btn.type = 'button';
        btn.innerText = slot;
        btn.addEventListener('click', () => {
            container.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            state.selectedSlot = slot;
            updateBookingSummary();
        });
        container.appendChild(btn);
    });

    updateBookingSummary();
    openModal('booking-modal');
}

function updateBookingSummary() {
    if (!state.activeBookingArena) return;

    const rate = state.activeBookingArena.pricePerHour;
    const duration = parseInt(document.getElementById('booking-duration').value);
    const total = rate * duration;

    document.getElementById('summary-base-rate').innerText = rate;
    document.getElementById('summary-duration').innerText = duration;
    document.getElementById('summary-total-price').innerText = total;
}

// Load Booking History
async function loadBookings() {
    const tbody = document.getElementById('bookings-tbody');
    tbody.innerHTML = '';

    if (!state.currentUser) {
        tbody.innerHTML = `
            <tr class="empty-state-row">
                <td colspan="6">
                    <div class="empty-state">
                        <i class="fa-solid fa-lock"></i>
                        <p>Please login to view your booking history.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    if (state.isDemoMode) {
        const local = JSON.parse(localStorage.getItem('my_bookings') || '[]');
        state.myBookings = local;
        renderBookingRows(local);
    } else {
        try {
            const response = await fetch(`${API_BASE_URL}/bookings/my`, {
                headers: { 'X-User-Id': state.currentUser.id }
            });
            if (!response.ok) throw new Error('Failed to load bookings');
            const data = await response.json();
            state.myBookings = data;
            renderBookingRows(data);
        } catch (err) {
            showToast('Failed to connect to API, showing local bookings.', 'warning');
            state.myBookings = JSON.parse(localStorage.getItem('my_bookings') || '[]');
            renderBookingRows(state.myBookings);
        }
    }
}

function renderBookingRows(bookings) {
    const tbody = document.getElementById('bookings-tbody');
    if (bookings.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-state-row">
                <td colspan="6">
                    <div class="empty-state">
                        <i class="fa-solid fa-calendar-minus"></i>
                        <p>You have no active bookings. Book your first slot now!</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    bookings.forEach(bk => {
        const row = document.createElement('tr');
        const total = bk.totalPrice || (bk.totalCost || 'N/A');
        
        let actionsHtml = `<button class="btn btn-danger btn-xs btn-cancel" data-id="${bk.id}"><i class="fa-solid fa-xmark"></i> Cancel</button>`;
        if (bk.status === 'CANCELLED') {
            actionsHtml = `<span style="color: var(--text-muted)">No Actions</span>`;
        }

        row.innerHTML = `
            <td>#${bk.id}</td>
            <td><strong>${bk.arenaName || 'Premium Arena'}</strong></td>
            <td>${bk.bookingDate} (${bk.startTime} - ${bk.endTime})</td>
            <td><strong>${total} INR</strong></td>
            <td><span class="status-tag ${bk.status.toLowerCase()}">${bk.status}</span></td>
            <td>${actionsHtml}</td>
        `;
        tbody.appendChild(row);
    });

    // Add listeners to cancel buttons
    tbody.querySelectorAll('.btn-cancel').forEach(btn => {
        btn.addEventListener('click', () => {
            const bookingId = parseInt(btn.getAttribute('data-id'));
            cancelBooking(bookingId);
        });
    });
}

async function cancelBooking(id) {
    if (confirm('Are you sure you want to cancel this booking session?')) {
        if (state.isDemoMode) {
            const savedBookings = JSON.parse(localStorage.getItem('my_bookings') || '[]');
            const index = savedBookings.findIndex(b => b.id === id);
            if (index !== -1) {
                savedBookings[index].status = 'CANCELLED';
                localStorage.setItem('my_bookings', JSON.stringify(savedBookings));
                showToast('Booking cancelled successfully', 'info');
                loadBookings();
            }
        } else {
            try {
                const response = await fetch(`${API_BASE_URL}/bookings/${id}/cancel`, {
                    method: 'PUT',
                    headers: { 'X-User-Id': state.currentUser.id }
                });

                if (!response.ok) throw new Error('Cannot cancel booking');

                showToast('Booking cancelled.', 'info');
                loadBookings();
            } catch (err) {
                showToast(err.message, 'danger');
            }
        }
    }
}

// AI Assistant Chat Interface
function initAiAssistant() {
    document.getElementById('btn-chat-send').addEventListener('click', () => {
        sendAiMessage();
    });

    document.getElementById('chat-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendAiMessage();
        }
    });

    // Suggestions quick buttons
    document.querySelectorAll('.suggestion-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const query = btn.getAttribute('data-query');
            document.getElementById('chat-input').value = query;
            sendAiMessage();
        });
    });
}

async function sendAiMessage() {
    const input = document.getElementById('chat-input');
    const msg = input.value.trim();
    if (!msg) return;

    input.value = '';

    // Append user message bubble
    appendChatBubble('user', msg);

    // Show AI typing placeholder
    const typingId = appendChatBubble('assistant', '🤖 *Thinking...*');

    if (state.isDemoMode) {
        // Local Smart NLP response simulator
        setTimeout(() => {
            const reply = generateLocalNlpReply(msg.toLowerCase());
            removeChatBubble(typingId);
            appendChatBubble('assistant', reply);
        }, 1000);
    } else {
        try {
            const response = await fetch(`${API_BASE_URL}/ai/chat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: msg })
            });

            if (!response.ok) throw new Error();

            const data = await response.json();
            removeChatBubble(typingId);
            appendChatBubble('assistant', data.response);
        } catch (e) {
            // Graceful fallback to Local Smart NLP if API throws
            setTimeout(() => {
                const reply = generateLocalNlpReply(msg.toLowerCase());
                removeChatBubble(typingId);
                appendChatBubble('assistant', reply);
            }, 1000);
        }
    }
}

function appendChatBubble(role, text) {
    const container = document.getElementById('chat-messages-container');
    const id = 'bubble_' + Date.now() + Math.random().toString(36).substr(2, 5);
    
    const formattedText = parseMarkdown(text);
    const bubbleHtml = document.createElement('div');
    bubbleHtml.className = `message ${role} animated`;
    bubbleHtml.id = id;
    bubbleHtml.innerHTML = `
        <div class="bubble">
            ${formattedText}
        </div>
        <span class="message-time">Just now</span>
    `;
    container.appendChild(bubbleHtml);
    container.scrollTop = container.scrollHeight;
    
    return id;
}

function removeChatBubble(id) {
    const bubble = document.getElementById(id);
    if (bubble) bubble.remove();
}

function parseMarkdown(text) {
    // Basic Markdown Parser for bolding and emojis
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/\n/g, '<br>');
}

function generateLocalNlpReply(msg) {
    if (msg.includes('football') || msg.includes('soccer') || msg.includes('turf')) {
        return "⚽ Based on our catalog, I highly recommend **Champions Turf** in Varanasi (Lanka). It is a top-tier artificial grass turf open from 06:00 AM to 10:00 PM at just **500 INR/hr**. I can help you book it right now!";
    } else if (msg.includes('badminton') || msg.includes('court') || msg.includes('shuttle')) {
        return "🏸 For Badminton, the **City Badminton Hall** in Varanasi (Sigra) is excellent! It has professional indoor wooden courts, open 07:00 AM to 09:00 PM for **300 INR/hr**. Would you like to check available slots?";
    } else if (msg.includes('mumbai') || msg.includes('andheri') || msg.includes('both')) {
        return "🌆 If you're in Mumbai, you must check out **Sports Arena Mumbai** located in Andheri. It supports both Football and Badminton with premium amenities, open from 05:00 AM to 11:00 PM for **800 INR/hr**!";
    } else if (msg.includes('hello') || msg.includes('hi') || msg.includes('hey')) {
        return "👋 Hey there! I am your Arena Finder AI Assistant. Ask me to find the best arenas for Football or Badminton in Varanasi or Mumbai, or ask about pricing and timings!";
    } else {
        return "🤖 I am here to help you find the perfect sports arena! We have fantastic options like:\n\n" +
            "- **Champions Turf** (Football, Varanasi) — 500 INR/hr\n" +
            "- **City Badminton Hall** (Badminton, Varanasi) — 300 INR/hr\n" +
            "- **Sports Arena Mumbai** (Football & Badminton, Mumbai) — 800 INR/hr\n\n" +
            "Tell me which sport or city you are interested in, and I will find the best match!";
    }
}

// Global Modal Helpers
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
}

// Global Interactive Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('notification-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'fa-circle-info';
    if (type === 'success') iconClass = 'fa-circle-check';
    if (type === 'danger') iconClass = 'fa-triangle-exclamation';
    if (type === 'warning') iconClass = 'fa-circle-exclamation';

    toast.innerHTML = `
        <i class="fa-solid ${iconClass}"></i>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    // Auto-remove after 4 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(15px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Popup micro notifications at bottom for live simulation feel
function showFloatingNotification(text) {
    const container = document.getElementById('notification-container');
    const toast = document.createElement('div');
    toast.className = 'toast info';
    toast.style.background = 'rgba(7, 7, 12, 0.95)';
    toast.style.border = '1px solid var(--secondary)';
    toast.innerHTML = `
        <i class="fa-solid fa-circle-nodes" style="color: var(--secondary); animation: spin 4s infinite linear;"></i>
        <span>${text}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

// Custom Debug logging helper
function logDebug(msg) {
    console.log(`%c[ARENA FINDER] ${msg}`, 'color: #9d4edd; font-weight: bold;');
}
