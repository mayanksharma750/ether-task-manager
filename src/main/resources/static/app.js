const state = {
    token: localStorage.getItem("etharaToken"),
    user: JSON.parse(localStorage.getItem("etharaUser") || "null")
};

const authPanel = document.getElementById("authPanel");
const dashboardPanel = document.getElementById("dashboardPanel");
const message = document.getElementById("message");

document.getElementById("loginForm").addEventListener("submit", handleLogin);
document.getElementById("signupForm").addEventListener("submit", handleSignup);
document.getElementById("projectForm").addEventListener("submit", handleCreateProject);
document.getElementById("taskForm").addEventListener("submit", handleCreateTask);
document.getElementById("logoutButton").addEventListener("click", logout);
document.getElementById("refreshButton").addEventListener("click", loadWorkspace);

renderAuthState();

if (state.token) {
    loadWorkspace();
}

async function handleLogin(event) {
    event.preventDefault();

    const response = await api("/api/auth/login", {
        method: "POST",
        body: {
            email: value("loginEmail"),
            password: value("loginPassword")
        },
        skipAuth: true
    });

    saveSession(response);
    showMessage("You are back in. Good to see you.");
    await loadWorkspace();
}

async function handleSignup(event) {
    event.preventDefault();

    const response = await api("/api/auth/signup", {
        method: "POST",
        body: {
            name: value("signupName"),
            email: value("signupEmail"),
            password: value("signupPassword"),
            role: value("signupRole")
        },
        skipAuth: true
    });

    saveSession(response);
    showMessage("Account created. Welcome to the workspace.");
    await loadWorkspace();
}

async function handleCreateProject(event) {
    event.preventDefault();

    await api("/api/projects", {
        method: "POST",
        body: {
            name: value("projectName"),
            description: value("projectDescription")
        }
    });

    event.target.reset();
    showMessage("Project created. Nice, a fresh place for the work to land.");
    await loadProjects();
}

async function handleCreateTask(event) {
    event.preventDefault();

    const assigneeId = value("taskAssigneeId");

    await api("/api/tasks", {
        method: "POST",
        body: {
            title: value("taskTitle"),
            description: value("taskDescription"),
            dueDate: value("taskDueDate") || null,
            projectId: Number(value("taskProjectId")),
            assigneeId: assigneeId ? Number(assigneeId) : null
        }
    });

    event.target.reset();
    showMessage("Task created. One more clear next step.");
    await loadDashboard();
}

async function loadWorkspace() {
    renderAuthState();

    if (!state.token) {
        return;
    }

    await Promise.allSettled([
        loadDashboard(),
        loadProjects()
    ]);
}

async function loadDashboard() {
    const dashboard = await api("/api/dashboard");

    document.getElementById("totalTasks").textContent = dashboard.totalTasks;
    document.getElementById("completedTasks").textContent = dashboard.completedTasks;
    document.getElementById("overdueTasks").textContent = dashboard.overdueTasks;
}

async function loadProjects() {
    const projectsList = document.getElementById("projectsList");
    const projects = await api("/api/projects");

    if (!projects.length) {
        projectsList.innerHTML = '<p class="empty-state">No projects yet. Create the first one above, then come back here to see it take shape.</p>';
        return;
    }

    projectsList.innerHTML = projects.map(project => {
        const members = project.members?.length
            ? project.members.map(member => `${escapeHtml(member.name)} (#${member.id})`).join(", ")
            : "No assigned members";

        return `
            <article class="list-item">
                <strong>${escapeHtml(project.name)}</strong>
                <small>Project #${project.id} · Owner: ${escapeHtml(project.ownerEmail)} · Members: ${members}</small>
                <p class="muted project-description">${escapeHtml(project.description || "No description added yet.")}</p>
            </article>
        `;
    }).join("");
}

async function api(path, options = {}) {
    const headers = {
        "Content-Type": "application/json"
    };

    if (state.token && !options.skipAuth) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, {
        method: options.method || "GET",
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        const error = data?.message || `Request failed with status ${response.status}`;
        showMessage(error, true);
        throw new Error(error);
    }

    return data;
}

function saveSession(authResponse) {
    state.token = authResponse.token;
    state.user = {
        id: authResponse.userId,
        name: authResponse.name,
        email: authResponse.email,
        role: authResponse.role
    };

    localStorage.setItem("etharaToken", state.token);
    localStorage.setItem("etharaUser", JSON.stringify(state.user));
    renderAuthState();
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem("etharaToken");
    localStorage.removeItem("etharaUser");
    renderAuthState();
    showMessage("Signed out. See you next time.");
}

function renderAuthState() {
    const loggedIn = Boolean(state.token);

    authPanel.classList.toggle("hidden", loggedIn);
    dashboardPanel.classList.toggle("hidden", !loggedIn);

    if (state.user) {
        document.getElementById("userSummary").textContent =
            `${state.user.name} is signed in as ${state.user.role.toLowerCase()} (${state.user.email})`;
    }
}

function value(id) {
    return document.getElementById(id).value.trim();
}

function showMessage(text, isError = false) {
    message.textContent = text;
    message.classList.toggle("error", isError);
    message.classList.add("visible");

    window.clearTimeout(showMessage.timer);
    showMessage.timer = window.setTimeout(() => {
        message.classList.remove("visible");
    }, 3200);
}

function escapeHtml(text) {
    return String(text)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
