import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

class ChatSub extends JFrame implements ActionListener, Runnable, WindowListener {
	private Container con;
	private CardLayout cl = new CardLayout();
	private JPanel first_pane = new JPanel(new BorderLayout(5, 5));
	private JPanel second_pane = new JPanel(new BorderLayout(5, 5));

	private JLabel nickname_lb = new JLabel(); // 닉네임 표시 레이블
	private JTextArea view_ta = new JTextArea();
	private JScrollPane view_jsp = new JScrollPane(view_ta);
	private JTextField talk_tf = new JTextField();
	private JButton send_bt = new JButton("전송");
	private JLabel inwon_lb = new JLabel("명");
	private JTextField inwon_tf = new JTextField("0", 3);
	private Vector inwon_vc = new Vector();
	private JList inwon_li = new JList(inwon_vc);
	private JScrollPane inwon_jsp = new JScrollPane(inwon_li);
	private JButton end_bt = new JButton("나가기");

	private Socket soc;
	private PrintWriter out;
	private BufferedReader in;
	private Thread currentThread;
	private String nick;

	public ChatSub() {
		super("EBSChat");
		
		// 닉네임 입력받기
		while (true) {
			nick = JOptionPane.showInputDialog("닉네임을 입력하세요:");
			if (nick == null) {
				// 취소 버튼을 누르면 프로그램 종료
				System.exit(0);
			} else if (nick.trim().length() == 0) {
				// 닉네임을 입력하지 않으면 오류 메시지 출력
				JOptionPane.showMessageDialog(null, "닉네임을 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
			} else {
				break;
			}
		}
		nickname_lb.setText(" *** " + nick + " *** "); // 닉네임 표시
		
		this.init();
		this.start();
		this.setSize(450, 600);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frm = this.getSize();
		int xpos = (int) (screen.getWidth() / 2 - frm.getWidth() / 2);
		int ypos = (int) (screen.getHeight() / 2 - frm.getHeight() / 2);
		this.setLocation(xpos, ypos);
		this.setVisible(true);
		
		try {
			soc = new Socket("localhost", 9999);
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())));
			in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			currentThread = new Thread(this);
			currentThread.start();
			out.println(nick);
			out.flush();
			talk_tf.requestFocus();
		} catch (IOException ee) {
			System.err.println("접속에러!");
		}
			}

	public void init() {
		con = this.getContentPane();
		con.setLayout(cl);
		JPanel jp4 = new JPanel(new BorderLayout(5, 5));
		jp4.add("North", nickname_lb);  // 닉네임 표시
		jp4.add("Center", view_jsp);
		JPanel jp6 = new JPanel(new BorderLayout());
		jp6.add("Center", talk_tf); // 메세지 입력
		jp6.add("East", send_bt);
		//jp6.setBorder(new TitledBorder("TALK DATA"));
		jp4.add("South", jp6);
		second_pane.add("Center", jp4);
		JPanel jp7 = new JPanel(new BorderLayout(5, 5));
		JPanel jp8 = new JPanel(new BorderLayout());
		JPanel jp9 = new JPanel(new GridBagLayout());
		inwon_tf.setHorizontalAlignment(SwingConstants.CENTER);
		inwon_tf.setBorder(new BevelBorder(BevelBorder.LOWERED));
		inwon_tf.setEditable(false);
		jp9.add(inwon_tf);
		//jp9.add(inwon_lb);
		//jp9.setBorder(new TitledBorder("총인원"));
		jp8.add("North", jp9);
		inwon_vc.add("== Room Member ==");
		jp8.add("Center", inwon_jsp);
		jp8.setBorder(new TitledBorder("접속자"));
		jp7.add("Center", jp8);
		JPanel jp10 = new JPanel(new GridLayout(3, 1));
		JPanel jp11 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		jp11.add(end_bt);
		jp10.add(jp11);
		jp10.setBorder(new TitledBorder("OPTION"));
		jp7.add("South", jp10);
		second_pane.add("East", jp7);
		con.add("chat", second_pane);
		view_ta.setForeground(Color.RED);
		view_ta.setBackground(Color.white);
		view_ta.setDisabledTextColor(Color.RED);
		view_ta.setFont(new Font("Sans-Serif", Font.BOLD, 15));
		
		//파일전송
		JButton file_bt = new JButton("파일");
		file_bt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					//파일을 서버에 전송할 코드 필요
				}
			}
		});

		JPanel jp111 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		jp111.add(file_bt);
		jp111.add(end_bt);
		jp10.add(jp111);
	}

	public void start() {
		end_bt.addActionListener(this);
		talk_tf.addActionListener(this);
		send_bt.addActionListener(this);
		this.addWindowListener(this);
		end_bt.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == talk_tf || e.getSource() == send_bt) {
			String str = talk_tf.getText();
			if (str == null || str.trim().length() == 0)
				return;
			out.println(str);
			out.flush();
			talk_tf.setText("");
			talk_tf.requestFocus();
		} else if (e.getSource() == end_bt) {
			out.println("/q");
			out.flush();
			currentThread.interrupt();
			currentThread = null;
			soc = null;
			out = null;
			in = null;
			nickname_lb.setText(" ");
			inwon_vc.clear();
			inwon_vc.add("== Room Member ==");
			inwon_li.setListData(inwon_vc);
			inwon_tf.setText("0");
			view_ta.setText("");
		}
	}

	public void run() { 
		view_ta.setEnabled(false);
		view_ta.setText("### 대화방에 입장 하셨습니다. ###\n");
		while (true) {
			try {
				String str = in.readLine();
				if (str.charAt(0) == '/') {
					if (str.charAt(1) == 'q') {
						String name = str.substring(2).trim();
						view_ta.append("%%% " + name + "님께서 퇴장하셨습니다.%%%\n");
						for (int i = 0; i < inwon_vc.size(); i++) {
							String imsi = (String) inwon_vc.elementAt(i);
							if (imsi.equals(name)) {
								int pos = inwon_li.getSelectedIndex();
								inwon_vc.removeElementAt(i);
								inwon_li.setListData(inwon_vc);
								inwon_li.setSelectedIndex(pos);
								break;
							}
						}
						int xx = Integer.parseInt(inwon_tf.getText());
						inwon_tf.setText(String.valueOf(--xx));
					} else if (str.charAt(1) == 'p') {
						int pos = inwon_li.getSelectedIndex();
						String user = str.substring(2).trim();
						inwon_vc.add(user);
						inwon_li.setListData(inwon_vc);
						inwon_li.setSelectedIndex(pos);
						view_ta.append("*** " + user + "님께서 입장하셨습니다.***\n");
						int xx = Integer.parseInt(inwon_tf.getText().trim());
						inwon_tf.setText(String.valueOf(++xx));
					} else if (str.charAt(1) == 'o') {
						String user = str.substring(2).trim();
						inwon_vc.add(user);
						inwon_li.setListData(inwon_vc);
						int xx = Integer.parseInt(inwon_tf.getText().trim());
						inwon_tf.setText(String.valueOf(++xx));
					}
				} else {
					view_ta.append(str + " ");
					view_ta.setCaretPosition(view_ta.getText().trim().length() - str.trim().length() + 1);
				}
			} catch (IOException ee) {
				System.err.println("read error = " + ee.toString());
			}
		}
	}

	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {
		if (out != null) {
			out.println("/q");
			out.flush();
			currentThread.interrupt();
		}
		System.exit(0);
	}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
}

public class Chatting {
	public static void main(String[] args) {
		ChatSub cs = new ChatSub();
	}
}