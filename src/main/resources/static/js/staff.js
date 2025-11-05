function toggleRoleMenu() {
    document.getElementById('roleMenu').classList.toggle('active');
}

window.onclick = function(event) {
    if (!event.target.matches('.role-icon')) {
        const dropdowns = document.getElementsByClassName("role-menu");
        for (let i = 0; i < dropdowns.length; i++) {
            dropdowns[i].classList.remove('active');
        }
    }
}

function showTab(tabName) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById(tabName).classList.add('active');
}

let allBookings = [];
let allEmployees = [];

document.addEventListener("DOMContentLoaded", async () => {
    const select = document.getElementById("municipality");
    const empMunicipality = document.getElementById("empMunicipality");
    const response = await fetch("https://json.geoapi.pt/municipios");
    const data = await response.json();
    data.forEach(m => {
        const opt = document.createElement("option");
        opt.value = m;
        opt.textContent = m;
        select.appendChild(opt);
        
        const empOpt = document.createElement("option");
        empOpt.value = m;
        empOpt.textContent = m;
        empMunicipality.appendChild(empOpt);
    });
});

const statusColors = {
    'RECEIVED': '#3b82f6',
    'ASSIGNED': '#f59e0b',
    'IN_PROGRESS': '#8b5cf6',
    'COMPLETED': '#10b981',
    'CANCELLED': '#ef4444'
};

function renderTable(bookings) {
    const resultDiv = document.getElementById("staffResult");
    
    if (bookings.length === 0) {
        resultDiv.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <h3>No bookings found</h3>
                <p>Try adjusting your filters or load all bookings</p>
            </div>
        `;
        return;
    }

    let tableHtml = `
        <table>
            <thead>
                <tr>
                    <th>Token</th>
                    <th>Description</th>
                    <th>Municipality</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
    `;

    bookings.forEach(booking => {
        const canAssign = booking.status === 'RECEIVED';
        tableHtml += `
            <tr>
                <td><code>${booking.token}</code></td>
                <td>${booking.description}</td>
                <td>${booking.municipality}</td>
                <td>${booking.date.replace('T',' ').substring(0,16)}</td>
                <td>
                    <select class="status-select" id="status-${booking.token}" ${booking.status==="CANCELLED" || booking.status==="COMPLETED"?"disabled":""}>
                        <option value="RECEIVED" ${booking.status==="RECEIVED"?"selected":""}>RECEIVED</option>
                        <option value="ASSIGNED" ${booking.status==="ASSIGNED"?"selected":""}>ASSIGNED</option>
                        <option value="IN_PROGRESS" ${booking.status==="IN_PROGRESS"?"selected":""}>IN_PROGRESS</option>
                        <option value="COMPLETED" ${booking.status==="COMPLETED"?"selected":""}>COMPLETED</option>
                        <option value="CANCELLED" ${booking.status==="CANCELLED"?"selected":""}>CANCELLED</option>
                    </select>
                </td>
                <td style="display: flex; gap: 8px;">
                    <button class="update-btn" id="btn-${booking.token}" ${booking.status==="CANCELLED" || booking.status==="COMPLETED"?"disabled":""}>
                        ✅ Update
                    </button>
                    ${canAssign ? `<button class="assign-btn" onclick="showAssignModal('${booking.token}')">👤 Assign</button>` : ''}
                </td>
            </tr>
        `;
    });

    tableHtml += `</tbody></table>`;
    resultDiv.innerHTML = tableHtml;

    bookings.forEach(booking => {
        document.getElementById(`btn-${booking.token}`).onclick = async () => {
            const newStatus = document.getElementById(`status-${booking.token}`).value;
            const staffMsg = document.getElementById("staffMsg");
            
            const res = await fetch(`http://localhost:8080/api/bookings/${booking.token}/status`, {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({status: newStatus})
            });
            
            if (res.ok) {
                staffMsg.textContent = "✅ Status updated successfully!";
                staffMsg.style.background = "#d1fae5";
                staffMsg.style.color = "#065f46";
                if (newStatus === "CANCELLED" || newStatus === "COMPLETED") {
                    document.getElementById(`btn-${booking.token}`).disabled = true;
                    document.getElementById(`status-${booking.token}`).disabled = true;
                }
                setTimeout(() => staffMsg.textContent = "", 3000);
            } else {
                staffMsg.textContent = "❌ Error updating status.";
                staffMsg.style.background = "#fee2e2";
                staffMsg.style.color = "#991b1b";
                setTimeout(() => staffMsg.textContent = "", 3000);
            }
        };
    });
}

document.getElementById("loadBtn").onclick = async () => {
    const municipality = document.getElementById("municipality").value;
    const status = document.getElementById("statusFilter").value;
    const resultDiv = document.getElementById("staffResult");
    
    resultDiv.innerHTML = "<div class='empty-state'><p>Loading bookings...</p></div>";
    
    try {
        const res = await fetch(`http://localhost:8080/api/bookings`);
        if (!res.ok) {
            resultDiv.innerHTML = "<div class='empty-state' style='color:#ef4444'>❌ Error loading bookings.</div>";
            return;
        }
        
        allBookings = await res.json();
        let filteredBookings = allBookings;

        if (municipality) {
            filteredBookings = filteredBookings.filter(b => b.municipality === municipality);
        }
        
        if (status) {
            filteredBookings = filteredBookings.filter(b => b.status === status);
        }

        renderTable(filteredBookings);
    } catch (err) {
        resultDiv.innerHTML = "<div class='empty-state' style='color:#ef4444'>❌ Connection error.</div>";
    }
};

document.getElementById("addEmployeeBtn").onclick = () => {
    const form = document.getElementById("addEmployeeForm");
    form.style.display = form.style.display === "none" ? "block" : "none";
};

document.getElementById("employeeForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    
    const employee = {
        name: document.getElementById("empName").value,
        email: document.getElementById("empEmail").value,
        municipality: document.getElementById("empMunicipality").value,
        role: document.getElementById("empRole").value
    };

    const employeesResult = document.getElementById("employeesResult");
    employeesResult.innerHTML = "<p style='text-align:center;color:#10b981;'>✅ Creating employee...</p>";

    try {
        const res = await fetch("http://localhost:8080/api/employees", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(employee)
        });

        if (res.ok) {
            const created = await res.json();
            employeesResult.innerHTML = `
                <div style="background:#d1fae5;color:#065f46;padding:20px;border-radius:12px;text-align:center;margin-bottom:20px;">
                    <h3 style="margin:0;">✅ Employee Created Successfully!</h3>
                    <p style="margin:10px 0 0 0;">Name: ${created.name} | Role: ${created.role}</p>
                </div>
            `;
            document.getElementById("employeeForm").reset();
            document.getElementById("addEmployeeForm").style.display = "none";
            setTimeout(() => {
                document.getElementById("loadEmployeesBtn").click();
            }, 2000);
        } else {
            employeesResult.innerHTML = `
                <div style="background:#fee2e2;color:#991b1b;padding:20px;border-radius:12px;text-align:center;">
                    ❌ Error creating employee
                </div>
            `;
        }
    } catch (err) {
        employeesResult.innerHTML = `
            <div style="background:#fee2e2;color:#991b1b;padding:20px;border-radius:12px;text-align:center;">
                ❌ Connection error
            </div>
        `;
    }
});

document.getElementById("loadEmployeesBtn").onclick = async () => {
    const resultDiv = document.getElementById("employeesResult");
    resultDiv.innerHTML = "<p style='text-align:center'>Loading employees...</p>";
    
    try {
        const res = await fetch("http://localhost:8080/api/employees");
        if (!res.ok) {
            resultDiv.innerHTML = "<p style='color:red;text-align:center'>Error loading employees</p>";
            return;
        }
        
        allEmployees = await res.json();
        
        if (allEmployees.length === 0) {
            resultDiv.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">👥</div>
                    <h3>No employees found</h3>
                    <p>Click "Add Employee" to create your first employee</p>
                </div>
            `;
            return;
        }
        
        let html = `<table>
            <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Municipality</th><th>Role</th></tr></thead>
            <tbody>`;
        
        allEmployees.forEach(emp => {
            html += `<tr>
                <td><strong>${emp.id}</strong></td>
                <td>${emp.name}</td>
                <td>${emp.email}</td>
                <td>${emp.municipality}</td>
                <td><span style="background: #3b82f6; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;">${emp.role}</span></td>
            </tr>`;
        });
        
        html += `</tbody></table>`;
        resultDiv.innerHTML = html;
    } catch (err) {
        resultDiv.innerHTML = "<p style='color:red;text-align:center'>Error loading employees</p>";
    }
};

function showAssignModal(token) {
    if (allEmployees.length === 0) {
        const staffMsg = document.getElementById("staffMsg");
        staffMsg.textContent = "⚠️ Please load employees first!";
        staffMsg.style.background = "#fef3c7";
        staffMsg.style.color = "#92400e";
        setTimeout(() => staffMsg.textContent = "", 3000);
        return;
    }
    
    const modal = document.getElementById("assignModal");
    const employeeList = document.getElementById("employeeList");
    
    employeeList.innerHTML = "";
    allEmployees.forEach(emp => {
        const empCard = document.createElement("div");
        empCard.className = "employee-card";
        empCard.innerHTML = `
            <div>
                <strong>${emp.name}</strong><br>
                <small>${emp.role} - ${emp.municipality}</small>
            </div>
            <button class="select-employee-btn" onclick="assignToEmployee('${token}', ${emp.id})">Select</button>
        `;
        employeeList.appendChild(empCard);
    });
    
    modal.style.display = "flex";
}

function closeAssignModal() {
    document.getElementById("assignModal").style.display = "none";
}

async function assignToEmployee(token, employeeId) {
    closeAssignModal();
    
    const staffMsg = document.getElementById("staffMsg");
    staffMsg.textContent = "Assigning task...";
    staffMsg.style.background = "#dbeafe";
    staffMsg.style.color = "#1e40af";
    
    try {
        const res = await fetch("http://localhost:8080/api/tasks", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({bookingToken: token, employeeId: employeeId})
        });
        
        if (res.ok) {
            staffMsg.textContent = "✅ Task assigned successfully!";
            staffMsg.style.background = "#d1fae5";
            staffMsg.style.color = "#065f46";
            setTimeout(() => {
                staffMsg.textContent = "";
                document.getElementById("loadBtn").click();
            }, 2000);
        } else if (res.status === 409) {
            staffMsg.textContent = "⚠️ This booking already has a task assigned!";
            staffMsg.style.background = "#fef3c7";
            staffMsg.style.color = "#92400e";
            setTimeout(() => staffMsg.textContent = "", 3000);
        } else {
            staffMsg.textContent = "❌ Error assigning task";
            staffMsg.style.background = "#fee2e2";
            staffMsg.style.color = "#991b1b";
            setTimeout(() => staffMsg.textContent = "", 3000);
        }
    } catch (err) {
        staffMsg.textContent = "❌ Connection error";
        staffMsg.style.background = "#fee2e2";
        staffMsg.style.color = "#991b1b";
        setTimeout(() => staffMsg.textContent = "", 3000);
    }
}

document.getElementById("loadTasksBtn").onclick = async () => {
    const resultDiv = document.getElementById("tasksResult");
    resultDiv.innerHTML = "<p style='text-align:center'>Loading tasks...</p>";
    
    try {
        const res = await fetch("http://localhost:8080/api/tasks");
        
        if (!res.ok) {
            resultDiv.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">❌</div>
                    <h3>Error loading tasks</h3>
                    <p>Please try again later</p>
                </div>
            `;
            return;
        }
        
        const tasks = await res.json();
        
        if (tasks.length === 0) {
            resultDiv.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📋</div>
                    <h3>No tasks assigned</h3>
                    <p>Tasks will appear here when bookings are assigned to employees</p>
                </div>
            `;
            return;
        }
        
        let html = `<table>
            <thead><tr><th>Task ID</th><th>Booking</th><th>Employee</th><th>Status</th><th>Assigned</th><th>Actions</th></tr></thead>
            <tbody>`;
        
        tasks.forEach(task => {
            html += `<tr>
                <td><strong>${task.id}</strong></td>
                <td><code>${task.booking.token}</code><br><small>${task.booking.description}</small></td>
                <td>${task.assignedEmployee.name}<br><small>${task.assignedEmployee.role}</small></td>
                <td><span style="background: ${task.status === 'COMPLETED' ? '#10b981' : '#f59e0b'}; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;">${task.status}</span></td>
                <td>${task.assignedAt.replace('T',' ').substring(0,16)}</td>
                <td>
                    ${task.status !== 'COMPLETED' ? `<button class="update-btn" onclick="showCompleteModal(${task.id})">✅ Complete</button>` : `<span style="color:#10b981;">✅ Completed</span>`}
                </td>
            </tr>`;
        });
        
        html += `</tbody></table>`;
        resultDiv.innerHTML = html;
    } catch (err) {
        resultDiv.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">❌</div>
                <h3>Connection Error</h3>
                <p>Could not connect to the server</p>
            </div>
        `;
    }
};

function showCompleteModal(taskId) {
    const modal = document.getElementById("completeModal");
    document.getElementById("taskIdToComplete").value = taskId;
    document.getElementById("completionNotes").value = "";
    modal.style.display = "flex";
}

function closeCompleteModal() {
    document.getElementById("completeModal").style.display = "none";
}

document.getElementById("completeTaskForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    
    const taskId = document.getElementById("taskIdToComplete").value;
    const notes = document.getElementById("completionNotes").value;
    
    closeCompleteModal();
    
    const tasksResult = document.getElementById("tasksResult");
    const originalContent = tasksResult.innerHTML;
    tasksResult.innerHTML = "<p style='text-align:center;color:#10b981;'>Completing task...</p>";
    
    try {
        const res = await fetch(`http://localhost:8080/api/tasks/${taskId}/complete`, {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({notes: notes || ""})
        });
        
        if (res.ok) {
            tasksResult.innerHTML = `
                <div style="background:#d1fae5;color:#065f46;padding:20px;border-radius:12px;text-align:center;margin-bottom:20px;">
                    <h3 style="margin:0;">✅ Task Completed!</h3>
                </div>
            `;
            setTimeout(() => {
                document.getElementById("loadTasksBtn").click();
            }, 2000);
        } else {
            tasksResult.innerHTML = originalContent;
            const staffMsg = document.getElementById("staffMsg");
            staffMsg.textContent = "❌ Error completing task";
            staffMsg.style.background = "#fee2e2";
            staffMsg.style.color = "#991b1b";
            setTimeout(() => staffMsg.textContent = "", 3000);
        }
    } catch (err) {
        tasksResult.innerHTML = originalContent;
        const staffMsg = document.getElementById("staffMsg");
        staffMsg.textContent = "❌ Connection error";
        staffMsg.style.background = "#fee2e2";
        staffMsg.style.color = "#991b1b";
        setTimeout(() => staffMsg.textContent = "", 3000);
    }
});

document.getElementById("municipality").addEventListener("change", () => {
    if (allBookings.length > 0) {
        document.getElementById("loadBtn").click();
    }
});

document.getElementById("statusFilter").addEventListener("change", () => {
    if (allBookings.length > 0) {
        document.getElementById("loadBtn").click();
    }
});