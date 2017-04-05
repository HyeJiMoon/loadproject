package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class LoadMain extends JFrame implements ActionListener{
	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load ,bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	FileReader reader=null;
	BufferedReader buffr=null;
	
	//윈도우창이 열리면 이미 접속을 확보해놓자 . 
	DBManager manager=DBManager.getInstance();  //둘다 최초에 올려놔져야해 접속!!!!!!!!!!!!! 가능가능
	Connection con;
	PreparedStatement pstmt=null;  //객체당하나
	
	public LoadMain() {
		p_north=new JPanel();
		t_path=new JTextField(20);
		bt_open=new JButton("파일열기");
		bt_load=new JButton("로드하기");
		bt_excel=new JButton("엑셀로드");
		bt_del=new JButton("삭제하기");
	
		table =new JTable();
		scroll = new JScrollPane(table);
		chooser=new JFileChooser("C:/animal");
		
		p_north.add(t_path);
		p_north.add(bt_open);
		p_north.add(bt_load);
		p_north.add(bt_excel);
		p_north.add(bt_del);
		
		add(p_north, BorderLayout.NORTH);
		add(scroll);
	
		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);
		
		//윈도우와 리스너와 연결  //윈도우리스너????
		this.addWindowListener(new WindowAdapter() {
		
			public void windowClosing(WindowEvent e) {
			//1.데이터베이스 자원 해제
				manager.disConnect(con);
				
			//2.프로세스 종료	
				System.exit(0);
			
			}
			
		});
		
		setVisible(true);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE); //나중에 뺴는 이유는 데이터베이스 연동할때 
	
		init();
	}
	
	//DB연동 Connection 얻어다 놓기
	private void init() {
		con=manager.getConnection();
		
	}
	//파일 탐색기 띄우기
	public void open(){ 
		int result=chooser.showOpenDialog(this); //this윈도우  . 1단계 반환값 result 긍정을 눌렀는지 부정을 눌렀는ㄷ지
		//열기를 누르면 목적파일에 스트림을 생성하자  
		if(result==JFileChooser.APPROVE_OPTION){
			//유저가 선택한 파일 
			File file=chooser.getSelectedFile(); //chooser가 보유한 메서드인 getSelectedFile 그걸 v파일로 받은것
	
			
			//스트림생성! (파일을 끌어들이기 위한)
			try {
				reader = new FileReader(file); //문자기반이자 입력스트림
				buffr=new BufferedReader(reader); //버퍼채로 끌어들이자 
				
				//빨대만 꽂아있는채로 타이밍상 로드하기를 눌러야 한줄씩 읽어들이는게 더나음 따라서 로드하기를먼저 구현 
				t_path.setText(file.getAbsolutePath());
			
//				String data;
//				while(true){
//					data=buffr.readLine();//readline 한줄씩 읽어들이기
//					if(data==null)break;
//					System.out.println(data);                               --> 빨대만 꼭
//				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		}
	}
	
	//CSV --> Oracle로 데이터 이전(migration)하기
	public void load(){
		//버퍼스트림을 이용하여 CSV의 데이터를 1줄씩 읽어들여 insert 시키자 
		//레코드가 없을 때 까지! 
		//while 문으로 돌리면 너무 빠르므로, 네트워크가 감당할 수 없기 때문에 일부러 지연시키면서!
		String data;
		StringBuffer sb=new StringBuffer();
		
		
		
		try {
			while(true){
				data=buffr.readLine();
				if(data==null)break;
				
				String[] value=data.split(","); // , 를 기준으로 String 배열로 반환받음
				//value[0] //=seq라인
				//seq줄을 제외하고 insert 하겠다
				if(!value[0].equals("seq")){
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					pstmt=con.prepareStatement(sb.toString());
					
					int result=pstmt.executeUpdate();
					//기존에 누적된 StringBuffer의 데이터를 모두 지우기
					sb.delete(0, sb.length()); 
				}else{
					System.out.println("난 1줄이므로 제외");
				}	
			}
			JOptionPane.showMessageDialog(this, "migration 완료");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) { //pstmt=con.prepareStatement(sb.toString()); 여기서 온 try/catch 코드 지저분해지니까 ㅣㅁㅌ으로 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
			if(pstmt!=null){
				try {
					pstmt.close();
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	//엑셀파일 읽어서 db에 migration 하기! 
	//javaSE 엑셀제어 라이브러리 있다? X 
	//open Source 공개소프트웨어 
	//copyright <---> copyleft (아파치 단체) 
	//POI 라이브러리 ! http://apache.org 
	/*
	 * HSSFWorkbook : 엑셀파일
	 * HSSFSheet : sheet 
	 * HSSFRow : row 
	 * HSSFCell : cell 
	 * 
	 * */
	
	public void loadExcel(){
		int result=chooser.showOpenDialog(this);
		
		if(result==JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			FileInputStream fis=null;
			
			
			
			try {
				fis=new FileInputStream(file);
				
				HSSFWorkbook book=null;
				book=new HSSFWorkbook(fis); // 스트림을 잡아먹어라 
				
				HSSFSheet sheet=null;
				sheet=book.getSheet("sheet1"); 
				
				int total=sheet.getLastRowNum();  // 길어지니까
				DataFormatter df=new DataFormatter(); //숫자와 문자 공존인데 자료형에 국한되지않음! 
				
				for(int a=1;a<=total;a++){
					HSSFRow row=sheet.getRow(a);
					int columnCount=row.getLastCellNum(); //컬럼의 갯수가 몇개니?
					
					for(int i=0;i<columnCount;i++){
						HSSFCell cell=row.getCell(i);
						//if(cell.getCellType()==){
						//System.out.println(cell.getNumericCellValue()); 
						//하나의 레코드에는 숫자와 문자가 공존하기 때문에 조건을 걸어서 따져보지ㅏ그런데 없어질 getCellType 따라서 자료형에 국한되지않고 모두 String 처리하자
						String value=df.formatCellValue(cell);
						System.out.print(value); //한줄뽑은거 
					}
					System.out.println("");//줄바꾼거
				}
				//HSSFRow row=sheet.getRow(0); //한줄가져오기
				//HSSFCell cell=row.getCell(0);
						
			}
			catch (FileNotFoundException e) {		
				e.printStackTrace(); //여기까진 그냥 똑같은데 엑셀은 일반파일이 아니므로 POI이용! 파일먼저- > sheet -> row > cell
			} catch (IOException e) {//book=new HSSFWorkbook(fis); 의 우려! 아파치에서 
				e.printStackTrace();
			}
		}
	}
	//선택한 레코드 삭제 
	public void delete(){
		
		
		
	}

	public void actionPerformed(ActionEvent e) {
		Object obj=e.getSource();

		if(obj==bt_open){
			open();
		}else if(obj==bt_load){
			load();
		}else if(obj==bt_excel){
			loadExcel();
		}else if(obj==bt_del){
			delete();
		}
	}
	
	public static void main(String[] args) {
		new LoadMain();
	}

}
