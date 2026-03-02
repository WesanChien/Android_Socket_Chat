// ClientSocketThread.java
package com.example.androidsocketpractice;

import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientSocketThread extends Thread {
    public interface ClientCallbacks {
        void onConnected();
        void onMessage(String line);
        void onError(String err);
        void onDisconnected(String reason);
    }

    private final String ip; private final int port; private final String username;
    private final ClientCallbacks cb;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private volatile boolean running = true;
    private volatile boolean connected = false;
    private Socket socket; private BufferedReader in; private PrintWriter out;

    public ClientSocketThread(String ip, int port, String username, ClientCallbacks cb) {
        this.ip = ip; this.port = port; this.username = username; this.cb = cb;
    }

    @Override public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            ui.post(cb::onConnected);
            out.println(username + " 已加入聊天室。");

            String line;
            while (running && (line = in.readLine()) != null) {
                final String msg = line;
                ui.post(() -> cb.onMessage(msg));
            }
            ui.post(() -> cb.onDisconnected("與伺服器連線中斷"));
        } catch (IOException e) {
            ui.post(() -> cb.onError(e.getClass().getSimpleName() + (e.getMessage()!=null? " - "+e.getMessage():"")));
            ui.post(() -> cb.onDisconnected("與伺服器連線中斷"));
        } finally {
            connected = false;
            try { if (socket != null) socket.close(); } catch (IOException ignore) {}
        }
    }

    private final ExecutorService writePool = Executors.newSingleThreadExecutor();

    public void send(String msg) {
        if (!connected || out == null) {
            ui.post(() -> cb.onError("尚未連線完成，請稍候…"));
            return;
        }
        writePool.execute(() -> {
            try {
                out.println(username + ": " + msg);
            } catch (Exception e) {
                ui.post(() -> cb.onError("送出失敗：" + e.getClass().getSimpleName()));
            }
        });
    }

    public void disconnect() {
        running = false;
        // 不再主動送「已離開聊天室。」
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignore) {}
        writePool.shutdownNow();
    }
}
