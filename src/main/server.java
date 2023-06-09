package main;

import java.io.*;
import java.util.*;
import java.util.List;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


//서버 프레임//////////////////////////////////////////////////////
public class server extends JFrame implements ActionListener {
	JPanel basePane, panel_main, panel_textarea, panel_btn;
	JScrollPane scrollpane;
	JTextArea textarea;
	JLabel serverstatus_label;
	JButton serverstart_btn, serverclose_btn;

	boolean gamestart, setEnd = false;
	public static boolean timebreak = false;
	LinkedHashMap<String, DataOutputStream> clientOutput = new LinkedHashMap<String, DataOutputStream>();
	LinkedHashMap<String, Integer> clientScore = new LinkedHashMap<String, Integer>();

	ServerSocket ss;
	Socket s;
	int port = 8800;
	int readyPlayer;
	int score, currentTime = 0;
	public static int drawer = 0;
	public static final int MAX_CLIENT = 4;
	public static int gameNum = 0;
	public static int setNum = 0;
	//public static boolean set0 = false;
	String sentence;

	public void init() {
		setTitle("SERVER");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 400);
		setLocationRelativeTo(null);

		basePane = new JPanel();
		basePane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(basePane);
		basePane.setLayout(new GridLayout(0, 1, 10, 0));

		panel_main = new JPanel();
		basePane.add(panel_main);
		panel_main.setLayout(new BoxLayout(panel_main, BoxLayout.Y_AXIS));

		panel_btn = new JPanel();
		panel_btn.setPreferredSize(new Dimension(10, 43));
		panel_btn.setAutoscrolls(true);
		panel_main.add(panel_btn);
		panel_btn.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		serverstart_btn = new JButton(" 서버 시작 ");
		serverstart_btn.setHorizontalTextPosition(SwingConstants.CENTER);
		serverstart_btn.setPreferredSize(new Dimension(120, 40));
		serverstart_btn.setFocusPainted(false);
		serverstart_btn.setFont(new Font("나눔바른고딕", Font.BOLD, 16));
		serverstart_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverstart_btn.setForeground(Color.WHITE);
		serverstart_btn.setBackground(Color.DARK_GRAY);
		serverstart_btn.setBorder(null);
		panel_btn.add(serverstart_btn);
		serverstart_btn.addActionListener(this);

		serverclose_btn = new JButton(" 서버 종료 ");
		serverclose_btn.setHorizontalTextPosition(SwingConstants.CENTER);
		serverclose_btn.setPreferredSize(new Dimension(120, 40));
		serverclose_btn.setFocusPainted(false);
		serverclose_btn.setFont(new Font("나눔바른고딕", Font.BOLD, 16));
		serverclose_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverclose_btn.setForeground(Color.WHITE);
		serverclose_btn.setBackground(Color.DARK_GRAY);
		serverclose_btn.setBorder(null);
		panel_btn.add(serverclose_btn);
		serverclose_btn.addActionListener(this);
		serverclose_btn.setEnabled(false);

		serverstatus_label = new JLabel("[ Server Waiting .. ]");
		serverstatus_label.setAlignmentX(Component.CENTER_ALIGNMENT);
		serverstatus_label.setPreferredSize(new Dimension(96, 50));
		panel_main.add(serverstatus_label);
		serverstatus_label.setHorizontalTextPosition(SwingConstants.CENTER);
		serverstatus_label.setHorizontalAlignment(SwingConstants.CENTER);
		serverstatus_label.setFont(new Font("나눔바른고딕", Font.PLAIN, 20));

		panel_textarea = new JPanel();
		panel_main.add(panel_textarea);
		panel_textarea.setLayout(new BorderLayout(0, 0));

		scrollpane = new JScrollPane();
		scrollpane.setBorder(new LineBorder(Color.DARK_GRAY));
		panel_textarea.add(scrollpane);

		textarea = new JTextArea();
		textarea.setLineWrap(true);
		textarea.setEditable(false);
		scrollpane.setViewportView(textarea);

		basePane.setBackground(new Color(247, 243, 222));
		panel_main.setBackground(new Color(247, 243, 222));
		panel_textarea.setBackground(new Color(247, 243, 222));
		panel_btn.setBackground(new Color(247, 243, 222));
	}
	// '서버 시작 & 종료' 버튼///////////////////////////////////////////
	public void actionPerformed(ActionEvent e) { 
		if (e.getSource() == serverstart_btn) {
			new Thread() {
				public void run() {
					try {

						Collections.synchronizedMap(clientOutput);
						ss = new ServerSocket(port);
						serverstatus_label.setText("[ Server Started ]");
						textarea.append("[ 서버가 시작되었습니다 ]" + "\n");
						serverstart_btn.setEnabled(false);
						serverclose_btn.setEnabled(true);
						clientAccept();
					} catch (IOException ie) {
					}
				}
			}.start();

		} else if (e.getSource() == serverclose_btn) {
			int select = JOptionPane.showConfirmDialog(null, "서버를 정말 종료하시겠습니까?", "JAVA CatchMind Server",
					JOptionPane.OK_CANCEL_OPTION);
			try {
				if (select == JOptionPane.YES_OPTION) {
					ss.close();
					serverstatus_label.setText("[ Server Closed ]");
					textarea.append("[ 서버가 종료되었습니다 ]" + "\n");
					serverstart_btn.setEnabled(true);
					serverclose_btn.setEnabled(false);
				}
			} catch (IOException io) {
			}
		}
	}
	
	//클라이언트 신호 받아들이는 매소드///////////////////////////////////////////
	public void clientAccept() {
		try {
			while (true) {
				s = ss.accept();
				if ((clientOutput.size()) + 1 > MAX_CLIENT || gamestart == true) {
					s.close();

				} else {

					Thread cm = new clientManager(s);
					cm.start();
				}
			}

		} catch (IOException io) {
		}

	}

	public void serverMsg(String str) {
		Iterator<String> it = clientOutput.keySet().iterator();
		while (it.hasNext()) {
			try {
				DataOutputStream dos = clientOutput.get(it.next());
				dos.writeUTF(str);
				dos.flush();
			} catch (IOException io) {
			}
		}
	}

	public class clientManager extends Thread {
		Socket soc;
		DataInputStream dis;
		DataOutputStream dos;
		//Timer[] tmArr = new Timer[15];
	    Timer tm = new Timer();

		public clientManager(Socket soc) {
			
			this.soc = soc;
			try {
				dis = new DataInputStream(soc.getInputStream());
				dos = new DataOutputStream(soc.getOutputStream());
			} catch (IOException io) {
			}
		}

		public void run() {
			String nickname = "";
			try {
				//닉네임 중복확인////////////////////
				nickname = dis.readUTF();
				if (!clientOutput.containsKey(nickname)) {
					clientOutput.put(nickname, dos);
					clientScore.put(nickname, score);
				} else if (clientOutput.containsKey(nickname)) {
					JOptionPane.showMessageDialog(null, "중복된 닉네임이 있습니다. ", "ERROR", JOptionPane.ERROR_MESSAGE);
					s.close();
				}
				serverMsg("=========" + nickname + "님 입장!==============\n =======현재 플레이어 수 : " + clientOutput.size()
						+ "/4" + "=======");
				Iterator<String> it1 = clientOutput.keySet().iterator();

				textarea.append("현재 접속자 수 : " + clientOutput.size() + "/4\n");
				scrollpane.getVerticalScrollBar().setValue(scrollpane.getVerticalScrollBar().getMaximum());

				clientSet();

				while (dis != null) {
					command(dis.readUTF());
				}

			} catch (IOException io) { // 이거 다시생각해 보기 원래 코드는 클라이언트 나가면 레디 다시하도록 게임 종료함
				clientOutput.remove(nickname);
				clientScore.remove(nickname);
				Close();
				serverMsg("=======" + "현재 플레이어 수 : " + clientOutput.size() + "/4" + "=======\n");
				Iterator<String> it1 = clientOutput.keySet().iterator();

				textarea.append("현재 접속자 수 : " + clientOutput.size() + "/4\n");
				scrollpane.getVerticalScrollBar().setValue(scrollpane.getVerticalScrollBar().getMaximum());

				clientSet();
			}

		}
		//종료 메소드/////////////////////////
		public void Close() {
			try {
				if (dos != null)
					dos.close();
				if (dis != null)
					dis.close();
				if (s != null)
					s.close();
			} catch (IOException io) {
			}
		}
		public void clientSet() {
			String[] nicknames = new String[clientOutput.size()];
			int[] scores = new int[clientScore.size()];
			int idx = 0;
			for (Map.Entry<String, Integer> mapEntry : clientScore.entrySet()) {
				nicknames[idx] = mapEntry.getKey();
				scores[idx++] = mapEntry.getValue();
			}
			for (int i = 0; i < clientScore.size(); i++)
				serverMsg("_CList" + nicknames[i] + " " + scores[i] + "#" + i);
		}
		
		public void gameStart() {
			// for(int i =0; i<15; i++) tmArr[i] = new Timer();
			
			ArrayList<String> drawerList = new ArrayList<String>();
			Iterator<String> it = clientOutput.keySet().iterator();

			while (it.hasNext())
				drawerList.add(it.next());
			if (drawer > clientOutput.size() - 1) {
				drawer = 0;
			}
			serverMsg("_Drwer" + drawerList.get(drawer++));
			
			Quiz quiz = new Quiz();
			quiz.start(); // 문제 출제
			if(gameNum==0&&setNum==0) {
				
				tm.start();
			}
//			if(gameNum>0) {
//				tm.cancel();
//			}
//			tm = new Timer();
//			tm.run();
//			if (gameNum != 0)
//				tmArr[gameNum - 1].cancel();
//			tmArr[gameNum] = new Timer();
//			tmArr[gameNum].run();

			gamestart = true;
			serverMsg("_Round" + (gameNum + 1));
			serverMsg("_Start"); // 명령어 : 게임 시작
			timebreak = false;
		}
		
		//커맨드 메소드////////////////////////////////////
		public void command(String str) {
			String cmd = str.substring(0, 6);

			if (cmd.contentEquals("_Chat ")) {
				check(str.substring(6).trim());
				serverMsg(str.substring(6));
			} else if (cmd.equals("_Ready")) { // 명령어 : 클라이언트 준비

				if (setEnd) {
					readyPlayer = 0;
					gameNum = 0;
					setEnd = false;
					Iterator<String> it1 = clientScore.keySet().iterator();
					while (it1.hasNext())
						clientScore.put(it1.next(), 0);

					clientSet();
				}
				readyPlayer++;
				if (readyPlayer >= 2 && readyPlayer == clientOutput.size()) {
					serverMsg("====" + "모든 참여자들이 준비되었습니다." + "====");
					for (int i = 3; i > 0; i--) {
						try {
							serverMsg("======" + i + "초 후 게임을 시작합니다 " + "======");
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}
					}
					if (gameNum == 0)
						gameStart();

				}
			} else if (cmd.equals("_Mouse")) { // 명령어 : 마우스 좌표 수신
				serverMsg(str);

			} else if (cmd.equals("_Color")) { // 명령어 : 컬러 설정
				serverMsg(str);
			} else if (cmd.equals("_Erase")) { // 명령어 : 지우기
				serverMsg(str);
			} else if (cmd.equals("_ErAll")) { // 명령어 : 모두 지우기
				serverMsg(str);
			} else if (cmd.equals("_GmEnd")) { // 명령어 : 게임 종료 (시간 초과나 이탈자 발생으로 게임이 종료되는 경우)

			}

			else if (cmd.equals("regame")) {
				currentTime = 0;
				serverMsg("_Timer03 : 00");
				timebreak = true;
				if (gameNum++ < 14) {

					gameStart();
					serverMsg("======" + "다음 게임이 곧 시작됩니다." + "======\n");
					
				} else {
					
					setEnd = true;
					setNum++;
					
					serverMsg("======" + "세트가 종료되었습니다 !" + "======\n");

					List<Map.Entry<String, Integer>> list = new LinkedList<>(clientScore.entrySet());

					Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
						public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
							int comparision = (o1.getValue() - o2.getValue()) * -1;
							return comparision == 0 ? o1.getKey().compareTo(o2.getKey()) : comparision;
						}
					});

					Map<String, Integer> sortedMap = new LinkedHashMap<>();
					for (Iterator<Map.Entry<String, Integer>> iter = list.iterator(); iter.hasNext();) {
						Map.Entry<String, Integer> entry = iter.next();
						sortedMap.put(entry.getKey(), entry.getValue());
					}
					String[] nicks = new String[4];
					int[] scores = new int[4];
					int idx = 0;
					for (Map.Entry<String, Integer> mapEntry : sortedMap.entrySet()) {
						nicks[idx] = mapEntry.getKey();
						scores[idx++] = mapEntry.getValue();
					}
					String result = "";
					for (idx = 0; idx < 4; idx++) {
						if (nicks[idx] == null)
							break;
						result += (idx + 1) + "등 : " + nicks[idx] + " (" + scores[idx] + ")\n";
					}
					serverMsg("_StEnd" + result);
					gamestart = false;
//					clientAccept();
//					clientSet();
//					gamestart = true;
				}
			}
		}
		
		// 정답 체크 메소드////////////////////////////////////
		public void check(String str) { 
			String nickname = str.substring(0, str.indexOf(" ")); // 정답을 말한 클라이언트의 닉네임
			String ans = str.substring(str.lastIndexOf(" ") + 1); // 정답 내용

			if (ans.equals(sentence)) {
				serverMsg("======" + nickname + " 님이 맞혔습니다!" + "===========\n =======정답은 [" + sentence
						+ "]!!!!===========");
				clientScore.put(nickname, clientScore.get(nickname) + 1); // 정답자 점수 추가
				clientSet();
				currentTime = 0;
				serverMsg("_Timer03 : 00");
				timebreak = true;
				gamestart = false;
				serverMsg("_GmEnd" + "0");
			}
		}
	}
	
	//문제내는 클래스////////////////////////////////////////
	class Quiz extends Thread {
		int x = 0;
		BufferedReader br;

		public void run() {
			Random r = new Random();
			int num = r.nextInt(100);
			try {
				FileReader fr = new FileReader("text\\catchMindWord.txt");
				br = new BufferedReader(fr);
				for (int x = 0; x <= num; x++) {
					sentence = br.readLine();
				}
				serverMsg("_RExam" + sentence);
			} catch (IOException ie) {
			}
		}
	}
	
	//타이머 클래스////////////////////////////////
	class Timer extends Thread{
		public void run() {
			try {
				
				while (true) {
					
//					else{
						sleep(1000);
						if(!timebreak) currentTime++;
						
						if(currentTime ==0) serverMsg("_Timer03 : 00");
						else serverMsg("_Timer" + toTime(currentTime));
						if (currentTime > 179) {
							gamestart = false;
							currentTime = 0;
							serverMsg("=========정답은 [" + sentence + "]!!!!=============\n");
							serverMsg("_GmEnd" + "1" + sentence);
						}
//					}
				}
					
			} catch (Exception e) {}
		}

		String toTime(int time) {
			int m,s;
			if(time % 60 == 0) {	m = time/60 - 1;	s = 60;	} 
			else {	m = time / 60;	s = time - 60 * m;}
			
			return String.format("%02d : %02d", 2 - m, 60 - s);
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					server sv = new server();
					sv.init();
					sv.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}

