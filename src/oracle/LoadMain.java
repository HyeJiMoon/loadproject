//숙제 삭제후 갱신
package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.poi.ddf.EscherColorRef.SysIndexSource;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

import util.file.FileUtil;

public class LoadMain extends JFrame implements ActionListener, TableModelListener,Runnable{//마우스리스너는 어댑터!
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
	PreparedStatement pstmt; //객체당 하나
	Vector<Vector> list;
	Vector columnName;
	MyModel myModel;
	
	String data;
	StringBuffer sb=new StringBuffer();
	
	Thread thread; //엑셀등록시 사용될 쓰레드 
	//왜사용? 데이터량이 너무 많을 경우, 네트워크 상태가 좋지 않을 경우, insert 가 while문 속도를 못따라간다.
	//따라서 안정성을 위해 일부러 시간 지연을 일으켜 insert 시도할 것임
	
	//엑셀 파일에 의해 생성된 쿼리문을 쓰레드가 사용할 수 있는 상태로 저장해놓자 !
	StringBuffer intsertsql=new StringBuffer();
	String seq;
	
	public LoadMain() {
		p_north=new JPanel();
		t_path=new JTextField(20);
		bt_open=new JButton("CSV파일열기");
		bt_load=new JButton("로드하기");
		bt_excel=new JButton("엑셀로드");
		bt_del=new JButton("삭제하기");
	
		table =new JTable(); //JTable은 편집이가능한 기능이있지만 tablemodel 을 사용하면 편집기능까지 tablemodel이 책임져야해
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
		table.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				//내가 선택한 곳의 seq 찝는것 
				JTable t=(JTable) e.getSource();
			
				int row=t.getSelectedRow();
				int col=0; //항상 0 (seq는 첫번째 컬럼이니까!)
			
				seq=(String)t.getValueAt(row, col);
			
			}
			
		});
		
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
			
			//cvs 파일 안열고 다른거 열면 뭐라하자 - 유효성체크 
			//동물..병원 .cvs 마지막 cvs 파일명 확장자가 숨어있고 . 이 실질적인 마지막 . 이니까 lastindex를 잡아서 하자. 
			//근데 자바스크립트lib만든거 처럼 유용하게 쓸 듯하니까 따로 빼자 (확장자만 뽑는 lib 만들어서 호출하자)
			String ext=FileUtil.getExt(file.getName());  //
			
			if(!ext.equals("csv")){
				JOptionPane.showMessageDialog(this, "파일열수없습니다");
				
				return; //더이상의 진행을 막는다
			}
			//t_path.setText(file.getAbsolutePath());
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
			
			//JTable 나오게 처리
			getList();
			table.setModel(new MyModel(list, columnName));
			//테이블모델과 리스너와의 연결 - JTable 은 현재 쓰고 있는 자신의 tablemodel을 반환해준다 ... 타이밍상 mymodel을 결정하고 리스너를 연결해야 가능하따! 
			table.getModel().addTableModelListener(this);
			table.updateUI();
			
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
	
	//1단계 컬럼명 분석 맨첫줄 seq~
	
	
	public void loadExcel(){
		int result=chooser.showOpenDialog(this);
		
		if(result==JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			FileInputStream fis=null;
			//컬럼명에서 써먹을 스프링버퍼
			StringBuffer cols=new StringBuffer();
			StringBuffer data=new StringBuffer();
			
			try {
				fis=new FileInputStream(file);
				
				HSSFWorkbook book=null;
				book=new HSSFWorkbook(fis); // 스트림을 잡아먹어라 
				
				HSSFSheet sheet=null;
				sheet=book.getSheet("sheet1"); 
				
				int total=sheet.getLastRowNum();  // 길어지니까
				DataFormatter df=new DataFormatter(); //숫자와 문자 공존인데 자료형에 국한되지않음! 자료형에 국한되지 않고 모두 Stiring처리할수있다
				
				/*--------------------------------------------
				 * 첫번째 row는 데이터가 아닌 컬럼정보이므로,
				 * 이 정보들을 추출하여 insert into table(***)
				 *--------------------------------------------*/
				//1단계 첫번째 row 얻어오기
				//int firstRow=sheet.getFirstRowNum();
				System.out.println("이 파일의 첫번째 row 번호는"+sheet.getFirstRowNum());
				//sheet.getRow(firstRow);
				HSSFRow firstRow=sheet.getRow(sheet.getFirstRowNum());
				//2단계 Row 를 얻었으니, 컬럼을 분석하자
				//firstRow.getLastCellNum(); //마지막 cell 번호
				cols.delete(0, cols.length());
				for(int i=0;i<firstRow.getLastCellNum();i++){ //for 문으로 
					HSSFCell cell=firstRow.getCell(i);
					if(i<firstRow.getLastCellNum()-1){ //사과 바나나 딸기 0과 1 까지 찍혀야해 마지막에 , 없애려고    //스트링버퍼에 모아서 써주자 
						System.out.print(cell.getStringCellValue()+",");
						cols.append(cell.getStringCellValue()+",");
					}else{ 
						System.out.print(cell.getStringCellValue());
						cols.append(cell.getStringCellValue());
					}
				}
				
				//for 문안에서 반복문쓰기
				for(int a=1;a<=total;a++){
					HSSFRow row=sheet.getRow(a);
					int columnCount=row.getLastCellNum(); //컬럼의 갯수가 몇개니?
			
					//insert 문의 횟수는 total 과 일치 엑셀시트가보유하고 있는 rownum만큼 insert 문으 ㅣ횟수 

					//값을 담은 for문
					data.delete(0, data.length());
					for(int i=0;i<columnCount;i++){
						HSSFCell cell=row.getCell(i);
						//if(cell.getCellType()==){
						//System.out.println(cell.getNumericCellValue()); 
						//하나의 레코드에는 숫자와 문자가 공존하기 때문에 조건을 걸어서 따져보지ㅏ그런데 없어질 getCellType 따라서 자료형에 국한되지않고 모두 String 처리하자
						String value=df.formatCellValue(cell);
				
						//System.out.print(value); //한줄뽑은거 
						if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING){
							value="'"+value+"'";
						}
						
						if(i<columnCount-1){
							data.append(value+",");
						}else{
							data.append(value);
							
						}
					}
		
					intsertsql.append("insert into hospital("+cols.toString()+") values("+data.toString()+");");
					
				}
				//HSSFRow row=sheet.getRow(0); //한줄가져오기
				//HSSFCell cell=row.getCell(0);	
				//모든게끝났으니, 편안하게 쓰레드에게 일시키자
				//runnable인터페이스를 인수로 넣으면 thread의 run 을 수행하는 것이 아니라 
				//runnable 인터페이스를 구현한자의 run() 수행하게 됨.. 따라서 우리꺼 수행! 
				thread=new Thread(this);
				thread.start();
				
			}
			catch (FileNotFoundException e) {		
				e.printStackTrace(); //여기까진 그냥 똑같은데 엑셀은 일반파일이 아니므로 POI이용! 파일먼저- > sheet -> row > cell
			}catch (IOException e) {//book=new HSSFWorkbook(fis); 의 우려! 아파치에서 
				e.printStackTrace();
			}
		}
	}
	//모든 레코드 가져오기  //csv와 연관
	
	public void getList(){
		String sql="select * from hospital order by seq asc";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try {
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			
			//컬럼명도 추출 //ResultSetMetaData이거로 하니까 seq 가 뒤에 가있음 !!! 
			ResultSetMetaData meta=rs.getMetaData();
			int count=meta.getColumnCount();
			columnName=new Vector();
			
			for(int i=0; i<count;i++){
				columnName.add(meta.getColumnName(i+1));
			}
			list = new Vector<Vector>(); //이차원 벡터가 될 예정 -> 새로운 list (LoadMain것) 얘를 MyModel에 
			while(rs.next()){ //커서 한칸 전진
		 
				Vector vec = new Vector(); //레코드 1건 담을 것 -> DTO 는 JTable 지원 X
			
				vec.add(rs.getString("seq"));
				vec.add(rs.getString("name"));
				vec.add(rs.getString("addr"));
				vec.add(rs.getString("regdate"));
				vec.add(rs.getString("status"));
				vec.add(rs.getString("dimension"));
				vec.add(rs.getString("type"));
				
				list.add(vec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
				
					e.printStackTrace();
				}
			}
		}
	}
	//선택한 레코드 삭제 
	public void delete(){
		//ans는 다이얼로그 값 반환
		int ans=JOptionPane.showConfirmDialog(LoadMain.this, seq+"선택한 레코드 삭제?");  //클래스명.this or LoadMain me;
		if(ans==JOptionPane.OK_OPTION){
			String sql="delete from hospital where seq="+seq;
			PreparedStatement pstmt=null;
			try {
				pstmt=con.prepareStatement(sql);
				int result=pstmt.executeUpdate(); //쿼리문의 결과가 result
				if(result!=0){
					JOptionPane.showMessageDialog(this, "삭제완료");
					getList();
					myModel.list=list;
					table.updateUI(); //테이블갱신
					
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(pstmt!=null){
					try {
						pstmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
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
	//테이블 모델의 데이터 값에 변경이 발생하면, 그 찰나를 감지하는 리스너 
	@Override
	public void tableChanged(TableModelEvent e) {
		PreparedStatement pstmt=null;
		//System.out.println("나바꿨어?");
		//엔터누르면 값변경 + DB에 올라가게  //(  ,  ) 벡터안에들어잉ㅅ늗 컬럼네임으로 알수 있오!!!!!
		
		int row=table.getSelectedRow();
		int col=table.getSelectedColumn();
		
		String column=(String)columnName.elementAt(col);
	
		//System.out.println();
		String value=(String)table.getValueAt(row, col); //지정한 좌표의 값반환
		
		String seq=(String)table.getValueAt(row, 0); 
		String sql="update hospital set" +column+"=value";//update hospital set 유저가편집한컬럼명 --e 사용 !
		sql+="where seq="+seq;
		System.out.println(sql);
		
		//쿼리문실행
		try {
			pstmt=con.prepareStatement(sql);
			int result=pstmt.executeUpdate();
			if(result!=0){
				JOptionPane.showMessageDialog(this, "수정완료");
				
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally{
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	
	//thread  문자열 길이만큼
	public void run() {
		//intsertsql에 insert문일 몇개인지 알아보기
		String[] str=intsertsql.toString().split(";"); //스트링형으로 바꿔서 split
		System.out.println("insert문 수는 "+str.length);
		PreparedStatement pstmt=null; //쿼리문마다 일대일매칭데서 올라옴
		
		
		for(int i=0;i<str.length;i++){
			System.out.println(str[i]);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				pstmt=con.prepareStatement(str[i]);
				int result=pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		//기존에 사용했던 StringBuffer 비우기
		intsertsql.delete(0, intsertsql.length());
		if(pstmt!=null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		//모든 insert 가 종료되면 JTable UI 갱신 
		
	}
	
	public static void main(String[] args) {
		new LoadMain();
	}


}
