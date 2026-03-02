// ClientHandler.java（Server 端一人一線）
package com.example.androidsocketpractice;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler extends Thread {
    private final Socket sock;
    private final CopyOnWriteArrayList<ClientHandler> peers;
    private BufferedReader in;
    private PrintWriter out;
    private final ServerSocketThread parent;
    private String username;

    public ClientHandler(Socket s, CopyOnWriteArrayList<ClientHandler> peers, ServerSocketThread parent) {
        this.sock = s;
        this.peers = peers;
        this.parent = parent;
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) { /* 交由 run() 中處理 */ }
    }

    public void send(String msg) { if (out != null) out.println(msg); }

    public void closeQuiet() {
        try { if (sock != null && !sock.isClosed()) sock.close(); } catch (IOException ignore) {}
        try { if (in != null) in.close(); } catch (IOException ignore) {}
        try { if (out != null) out.close(); } catch (Exception ignore) {}
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                // 嘗試從「加入訊息」抓出名稱：<name> 已加入聊天室。
                if (username == null && line.endsWith(" 已加入聊天室。")) {
                    int idx = line.indexOf(" 已加入聊天室。");
                    if (idx > 0) username = line.substring(0, idx);
                }
                // 回報給 ServerThread，讓 UI 可以顯示在 Server 畫面
                if (parent != null) parent.onClientMessageFromHandler(line);

                // 再廣播給所有 client（含自己）
                for (ClientHandler p: peers) p.send(line);
            }
        } catch (IOException ignored) {
        } finally {
            if (username != null) {
                String bye = username + " 已離開聊天室。";
                if (parent != null) parent.onClientMessageFromHandler(bye);  // Server UI
                for (ClientHandler p : peers) p.send(bye);                   // 廣播給其他 client
            }
            closeQuiet();
            peers.remove(this);
        }
    }
}
