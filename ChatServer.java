import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class ChatServer {

    // جميع الجلسات المفتوحة
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    // الرسائل المخزنة (احتفظ بآخر 50 رسالة)
    private static final ConcurrentLinkedQueue<String> chatHistory = new ConcurrentLinkedQueue<>();

    // جلسات المستخدمين الذين فتحوا الشات بكتابة "Love"
    private static final Set<Session> unlockedSessions = ConcurrentHashMap.newKeySet();

    private static final int MAX_HISTORY = 50;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        session.getBasicRemote().sendText("🔒 مرحباً! اكتب 'Love' لفتح الدردشة.");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        message = message.trim();

        // إذا لم يفتح المستخدم الدردشة بكتابة "Love"
        if (!unlockedSessions.contains(session)) {
            if (message.equalsIgnoreCase("Love")) {
                unlockedSessions.add(session);
                session.getBasicRemote().sendText("✅ تم فتح الدردشة، يمكنك الآن رؤية الرسائل وإرسالها.");
                // إرسال تاريخ الرسائل السابقة للمستخدم
                synchronized (chatHistory) {
                    for (String msg : chatHistory) {
                        session.getBasicRemote().sendText(msg);
                    }
                }
            } else {
                session.getBasicRemote().sendText("❌ لا يمكنك رؤية الرسائل حتى تكتب 'Love'");
            }
            return;
        }

        // إضافة الرسالة لتاريخ الشات مع الحد الأقصى
        synchronized (chatHistory) {
            if (chatHistory.size() >= MAX_HISTORY) {
                chatHistory.poll();
            }
            chatHistory.add(message);
        }

        // إرسال الرسالة لكل الجلسات المفتوحة فقط (التي كتبت "Love")
        for (Session s : unlockedSessions) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        unlockedSessions.remove(session);
    }
}
