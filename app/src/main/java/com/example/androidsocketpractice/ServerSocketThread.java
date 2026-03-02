// ServerSocketThread.java
package com.example.androidsocketpractice;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketThread extends Thread {
    private final int port;
    private final Context appCtx;
    private final ServerEvents listener;

    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public interface ServerEvents {
        void onServerStarted(String ip);
        void onServerMessage(String msg);
    }

    public ServerSocketThread(Context ctx, int port, ServerEvents l) {
        this.appCtx = ctx.getApplicationContext();
        this.port = port;
        this.listener = l;
    }

    @Override public void run() {
        try {
            serverSocket = new ServerSocket(port);
            String ip = getIp(appCtx);
            if (listener != null) listener.onServerStarted(ip);
            Log.i("Server", "Listening on " + ip + ":" + port);

            while (running) {
                try {
                    Socket c = serverSocket.accept();
                    ClientHandler h = new ClientHandler(c, clients, this);
                    clients.add(h);
                    h.start();
                } catch (IOException acceptEx) {
                    if (running) Log.e("Server", "accept error", acceptEx);
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("Server", "start failed", e);
        } finally {
            for (ClientHandler ch: clients) ch.closeQuiet();
            clients.clear();
            if (serverSocket != null && !serverSocket.isClosed()) {
                try { serverSocket.close(); } catch (IOException ignore) {}
            }
        }
    }

    void onClientMessageFromHandler(String msg) {
        if (listener != null) listener.onServerMessage(msg);
    }
    private final ExecutorService sendPool = Executors.newCachedThreadPool();

    public void broadcast(String message) {
        for (ClientHandler ch : clients) {
            sendPool.execute(() -> {
                try { ch.send(message); }
                catch (Exception e) { Log.e("Server", "broadcast error", e); }
            });
        }
    }

    public void stopServer() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignore) {}
        sendPool.shutdownNow();
    }

    private static String getIp(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            int ipInt = wm.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipInt & 0xff), (ipInt >> 8 & 0xff), (ipInt >> 16 & 0xff), (ipInt >> 24 & 0xff));
        } catch (Exception e) { return "0.0.0.0"; }
    }
}
