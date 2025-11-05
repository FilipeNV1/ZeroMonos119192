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

document.addEventListener("DOMContentLoaded", async () => {
  const select = document.getElementById("municipality");
  const response = await fetch("https://json.geoapi.pt/municipios");
  const data = await response.json();
  data.forEach(m => {
    const opt = document.createElement("option");
    opt.value = m;
    opt.textContent = m;
    select.appendChild(opt);
  });
  const dateInput = document.getElementById("date");
  const now = new Date();
  now.setDate(now.getDate() + 1);
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  dateInput.min = now.toISOString().slice(0,16);
});

document.getElementById("bookingForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const booking = {
    description: document.getElementById("description").value,
    municipality: document.getElementById("municipality").value,
    date: document.getElementById("date").value
  };

  const resultDiv = document.getElementById("result");
  resultDiv.textContent = "Submitting...";
  resultDiv.style.background = "#dbeafe";
  resultDiv.style.color = "#1e40af";

  try {
    const res = await fetch("http://localhost:8080/api/bookings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(booking)
    });

    if (!res.ok) {
      resultDiv.textContent = "❌ Error creating booking!";
      resultDiv.style.background = "#fee2e2";
      resultDiv.style.color = "#991b1b";
      return;
    }

    const saved = await res.json();
    resultDiv.innerHTML = `
      <div style="background:#d1fae5;color:#065f46;padding:20px;border-radius:12px;">
        <h3 style="margin:0 0 10px 0;">✅ Booking Created Successfully!</h3>
        <p style="margin:5px 0;"><strong>Your Token:</strong> <code style="background:#fff;padding:4px 8px;border-radius:4px;font-size:18px;">${saved.token}</code></p>
        <p style="margin:5px 0;font-size:14px;">Save this token to track your booking!</p>
      </div>
    `;
    document.getElementById("bookingForm").reset();
  } catch (err) {
    resultDiv.textContent = "❌ Connection error!";
    resultDiv.style.background = "#fee2e2";
    resultDiv.style.color = "#991b1b";
  }
});

document.getElementById("searchForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const token = document.getElementById("token").value.trim();
  const resultDiv = document.getElementById("bookingResult");
  resultDiv.innerHTML = "<p style='text-align:center;color:#718096;'>Searching...</p>";

  try {
    const res = await fetch(`http://localhost:8080/api/bookings/${token}`);
    if (!res.ok) {
      resultDiv.innerHTML = `<div style="color:#ef4444;text-align:center;padding:20px;">❌ Booking not found!</div>`;
      return;
    }
    const booking = await res.json();
    
    const statusColors = {
      'RECEIVED': '#3b82f6',
      'ASSIGNED': '#f59e0b',
      'IN_PROGRESS': '#8b5cf6',
      'COMPLETED': '#10b981',
      'CANCELLED': '#ef4444'
    };
    
    resultDiv.innerHTML = `
      <div class="booking-details">
        <h3>📦 Booking Details</h3>
        <div class="detail-row">
          <span class="detail-label">Description:</span>
          <span class="detail-value">${booking.description}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Municipality:</span>
          <span class="detail-value">${booking.municipality}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Date:</span>
          <span class="detail-value">${booking.date.replace('T', ' ').substring(0,16)}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Status:</span>
          <span class="status-badge" style="background:${statusColors[booking.status]};color:#fff;" id="status">${booking.status}</span>
        </div>
        <button id="cancelBtn" class="cancel-btn" ${booking.status === 'CANCELLED' || booking.status === 'COMPLETED' ? 'disabled' : ''}>
          ❌ Cancel Booking
        </button>
      </div>
    `;
    
    if (booking.status !== 'CANCELLED' && booking.status !== 'COMPLETED') {
      document.getElementById("cancelBtn").onclick = async () => {
        const confirmDiv = document.createElement('div');
        confirmDiv.innerHTML = `
          <div style="background:#fef3c7;color:#92400e;padding:15px;border-radius:8px;margin-top:10px;">
            <p style="margin:0 0 10px 0;">⚠️ Are you sure you want to cancel this booking?</p>
            <button id="confirmCancel" style="background:#ef4444;color:#fff;border:none;padding:8px 16px;border-radius:6px;cursor:pointer;margin-right:10px;">Yes, Cancel</button>
            <button id="cancelCancel" style="background:#cbd5e0;color:#2d3748;border:none;padding:8px 16px;border-radius:6px;cursor:pointer;">No, Keep It</button>
          </div>
        `;
        resultDiv.appendChild(confirmDiv);
        
        document.getElementById("confirmCancel").onclick = async () => {
          const cancelRes = await fetch(`http://localhost:8080/api/bookings/${token}/cancel`, { method: "PUT" });
          if (!cancelRes.ok) {
            resultDiv.innerHTML += `<div style="background:#fee2e2;color:#991b1b;padding:15px;border-radius:8px;margin-top:10px;">❌ Could not cancel booking.</div>`;
            return;
          }
          document.getElementById("searchForm").dispatchEvent(new Event('submit'));
        };
        
        document.getElementById("cancelCancel").onclick = () => {
          confirmDiv.remove();
        };
      };
    }
    
    const historyRes = await fetch(`http://localhost:8080/api/bookings/${token}/history`);
    if (historyRes.ok) {
      const history = await historyRes.json();
      let historyHtml = `<div class="status-history"><h3>📊 Status History</h3>`;
      history.forEach(h => {
        historyHtml += `<div class="history-item">
          <span style="font-weight:600;color:${statusColors[h.status]}">${h.status}</span>
          <span style="color:#718096;font-size:14px;">${h.timestamp.replace('T',' ').substring(0,16)}</span>
        </div>`;
      });
      historyHtml += "</div>";
      document.getElementById("historyResult").innerHTML = historyHtml;
    }
  } catch (err) {
    resultDiv.innerHTML = `<div style="color:#ef4444;text-align:center;padding:20px;">❌ Connection error!</div>`;
  }
});