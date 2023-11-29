import java.io.*;
import java.net.*;
import java.util.*;

class ChatServer extends Thread {
	private ServerSocket ss;
	private Vector vc = new Vector();

	public ChatServer() {
		try {
			ss = new ServerSocket(9999);
			this.start();
			System.out.println("서버를 시작!..클라이언트 접속 대기중...");
		} catch (IOException ee) {
			System.err.println("이미 사용중입니다.");
			System.exit(1);
		}
	}

	public void run() {
		try {
			while (true) {
				Socket s = ss.accept();
				System.out.println(s.getInetAddress() + "에서 접속하였습니다.");
				ChatService cs = new ChatService(s);
				cs.start();
				vc.add(cs);
			}
		} catch (IOException ee) {
			System.err.println("서버가 종료되었습니다.");
		}
	}

	class ChatService extends Thread {
		private Socket s;
		private BufferedReader in;
		private PrintWriter out;
		private String name;

		public ChatService(Socket s) {
			this.s = s;
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())));
			} catch (IOException ie) {
				System.err.println("스트림 생성 에러!");
			}
		}

		public void run() {
			try {
				name = in.readLine();
				broadcast("/p" + name);
				String str = null;
				while ((str = in.readLine()) != null) {
					if (str.equals("/q")) {
						broadcast("/q" + name);
						vc.remove(this);
						break;
					}
					broadcast(name + ">" + str);
				}
			} catch (IOException ie) {
				System.err.println("상대방과의 연결이 끊어졌습니다.");
			} finally {
				try {
					if (s != null)
						s.close();
					System.out.println(s.getInetAddress() + "에서 접속을 종료하였습니다.");
				} catch (IOException ie) {
				}
			}
		}

		public void broadcast(String message) {
			synchronized (vc) {
				for (int i = 0; i < vc.size(); i++) {
					ChatService cs = (ChatService) vc.elementAt(i);
					cs.send(message);
				}
			}
		}

		public void send(String message) {
			out.println(message);
			out.flush();
		}
	}
}

public class ChattingServer {
	public static void main(String[] args) {
		new ChatServer();
	}
}

