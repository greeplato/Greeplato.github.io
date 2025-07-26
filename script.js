// اتصال WebSocket
const socket = new WebSocket("ws://localhost:8080/chat");

// مرجع للعناصر
const form = document.querySelector("form");
const input = document.querySelector("input[type='text']");
const chatBox = document.getElementById("chatBox");

// فلتر لمنع رؤية الرسائل إلا بعد كلمة "Love"
let unlocked = false;

// عرض الرسائل في الصفحة
function addMessage(message, type = "unlocked") {
  const msg = document.createElement("div");
  msg.classList.add("message", type);
  msg.textContent = message;
  chatBox.appendChild(msg);
  chatBox.scrollTop = chatBox.scrollHeight;
}

// عند استلام رسالة من الخادم
socket.onmessage = (event) => {
  if (unlocked) {
    addMessage(event.data, "unlocked");
  } else {
    addMessage("🔒 الرسائل مخفية. اكتب كلمة Love لرؤيتها", "locked");
  }
};

// عند إرسال رسالة
form.addEventListener("submit", (e) => {
  e.preventDefault();
  const text = input.value.trim();
  if (!text) return;

  // التحقق من كلمة الفتح
  if (!unlocked && text.toLowerCase() === "love") {
    unlocked = true;
    addMessage("✅ تم فتح الرسائل. استمتع بالشات!", "system");
    input.value = "";
    return;
  }

  // إرسال الرسالة للخادم
  socket.send(text);
  addMessage("أنت: " + text, "unlocked");
  input.value = "";
});

// عند فتح الاتصال بالخادم
socket.onopen = () => {
  console.log("🔌 WebSocket connection established.");
};

// عند فشل الاتصال
socket.onerror = (error) => {
  console.error("WebSocket Error:", error);
  addMessage("❌ حدث خطأ في الاتصال بالخادم.", "system");
};

// عند إغلاق الاتصال
socket.onclose = () => {
  addMessage("🚫 تم قطع الاتصال بالخادم.", "system");
};
//التعديل الجديد
document.addEventListener("DOMContentLoaded", () => {
  const gate = document.getElementById("gate");
  const gateInput = document.getElementById("gateInput");
  const chatUI = document.getElementById("chatUI");

  gateInput.addEventListener("keyup", function (e) {
    if (e.key === "Enter" && gateInput.value.trim().toLowerCase() === "love") {
      gate.style.display = "none";
      chatUI.style.display = "block";
    }
  });
});
