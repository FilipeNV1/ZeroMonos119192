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
  try {
    const res = await fetch("http://localhost:8080/api/bookings", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify(booking)
    });
    if (!res.ok) {
      resultDiv.textContent = "Erro ao criar marcação. Verifica os dados!";
      resultDiv.style.color = "red";
      return;
    }
    if (res.status === 429) {
      resultDiv.textContent = "Limite de marcações atingido para este município/data!";
      resultDiv.style.color = "red";
      return;
    }
    const result = await res.json();
    resultDiv.textContent = "Booking created! Token: " + result.token;
    resultDiv.style.color = "#007bff";
    document.getElementById("bookingForm").reset();
  } catch (err) {
    resultDiv.textContent = "Erro de ligação ao servidor!";
    resultDiv.style.color = "red";
  }
});