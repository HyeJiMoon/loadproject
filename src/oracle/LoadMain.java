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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class LoadMain extends JFrame implements ActionListener, TableModelListener{
	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load ,bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	FileReader reader=null;
	BufferedReader buffr=null;
	
	//������â�� ������ �̹� ������ Ȯ���س��� . 
	DBManager manager=DBManager.getInstance();  //�Ѵ� ���ʿ� �÷��������� ����!!!!!!!!!!!!! ���ɰ���
	Connection con;
	PreparedStatement pstmt; //��ü�� �ϳ�
	Vector<Vector> list;
	Vector columnName;
	MyModel myModel;
	

	public LoadMain() {
		p_north=new JPanel();
		t_path=new JTextField(20);
		bt_open=new JButton("���Ͽ���");
		bt_load=new JButton("�ε��ϱ�");
		bt_excel=new JButton("�����ε�");
		bt_del=new JButton("�����ϱ�");
	
		table =new JTable(); //JTable�� �����̰����� ����������� tablemodel �� ����ϸ� ������ɱ��� tablemodel�� å��������
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
		
		//������� �����ʿ� ����  //�����츮����????
		this.addWindowListener(new WindowAdapter() {
		
			public void windowClosing(WindowEvent e) {
			//1.�����ͺ��̽� �ڿ� ����
				manager.disConnect(con);
				
			//2.���μ��� ����	
				System.exit(0);
			
			}
			
		});
	
		
		setVisible(true);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE); //���߿� ���� ������ �����ͺ��̽� �����Ҷ� 
	
		init();
	}
	
	//DB���� Connection ���� ����
	private void init() {
		con=manager.getConnection();
		
		
	}
	//���� Ž���� ����
	public void open(){ 
		int result=chooser.showOpenDialog(this); //this������  . 1�ܰ� ��ȯ�� result ������ �������� ������ �����¤���
		//���⸦ ������ �������Ͽ� ��Ʈ���� ��������  
		if(result==JFileChooser.APPROVE_OPTION){
			//������ ������ ���� 
			File file=chooser.getSelectedFile(); //chooser�� ������ �޼����� getSelectedFile �װ� v���Ϸ� ������
	
			
			//��Ʈ������! (������ ������̱� ����)
			try {
				reader = new FileReader(file); //���ڱ������ �Է½�Ʈ��
				buffr=new BufferedReader(reader); //����ä�� ��������� 
				
				//���븸 �Ⱦ��ִ�ä�� Ÿ�ֻ̹� �ε��ϱ⸦ ������ ���پ� �о���̴°� ������ ���� �ε��ϱ⸦���� ���� 
				t_path.setText(file.getAbsolutePath());
			
//				String data;
//				while(true){
//					data=buffr.readLine();//readline ���پ� �о���̱�
//					if(data==null)break;
//					System.out.println(data);                               --> ���븸 ��
//				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		}
	}
	
	//CSV --> Oracle�� ������ ����(migration)�ϱ�
	public void load(){
		//���۽�Ʈ���� �̿��Ͽ� CSV�� �����͸� 1�پ� �о�鿩 insert ��Ű�� 
		//���ڵ尡 ���� �� ����! 
		//while ������ ������ �ʹ� �����Ƿ�, ��Ʈ��ũ�� ������ �� ���� ������ �Ϻη� ������Ű�鼭!
		String data;
		StringBuffer sb=new StringBuffer();
		
		
		
		try {
			while(true){
				data=buffr.readLine();
				if(data==null)break;
				
				String[] value=data.split(","); // , �� �������� String �迭�� ��ȯ����
				//value[0] //=seq����
				//seq���� �����ϰ� insert �ϰڴ�
				if(!value[0].equals("seq")){
					sb.append("insert into hospital(name,addr,regdate,status,dimension,type,seq)");
					sb.append(" values("+value[0]+",'"+value[1]+"','"+value[2]+"','"+value[3]+"','"+value[4]+"',"+value[5]+",'"+value[6]+"')");
					
					System.out.println(sb.toString());
					pstmt=con.prepareStatement(sb.toString());
					
					int result=pstmt.executeUpdate();
					//������ ������ StringBuffer�� �����͸� ��� �����
					sb.delete(0, sb.length()); 
				}else{
					System.out.println("�� 1���̹Ƿ� ����");
				}	
			}
			JOptionPane.showMessageDialog(this, "migration �Ϸ�");
			
			//JTable ������ ó��
			getList();
			table.setModel(new MyModel(list, columnName));
			//���̺�𵨰� �����ʿ��� ���� - JTable �� ���� ���� �ִ� �ڽ��� tablemodel�� ��ȯ���ش� ... Ÿ�ֻ̹� mymodel�� �����ϰ� �����ʸ� �����ؾ� �����ϴ�
			table.getModel().addTableModelListener(this);
			table.updateUI();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) { //pstmt=con.prepareStatement(sb.toString()); ���⼭ �� try/catch �ڵ� �����������ϱ� �Ӥ������� 
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
	//�������� �о db�� migration �ϱ�! 
	//javaSE �������� ���̺귯�� �ִ�? X 
	//open Source ��������Ʈ���� 
	//copyright <---> copyleft (����ġ ��ü) 
	//POI ���̺귯�� ! http://apache.org 
	/*
	 * HSSFWorkbook : ��������
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
				book=new HSSFWorkbook(fis); // ��Ʈ���� ��ƸԾ�� 
				
				HSSFSheet sheet=null;
				sheet=book.getSheet("sheet1"); 
				
				int total=sheet.getLastRowNum();  // ������ϱ�
				DataFormatter df=new DataFormatter(); //���ڿ� ���� �����ε� �ڷ����� ���ѵ�������! 
				
				//for ���ȿ��� �ݺ�������
				for(int a=1;a<=total;a++){
					HSSFRow row=sheet.getRow(a);
					int columnCount=row.getLastCellNum(); //�÷��� ������ ���?
					
					for(int i=0;i<columnCount;i++){
						HSSFCell cell=row.getCell(i);
						//if(cell.getCellType()==){
						//System.out.println(cell.getNumericCellValue()); 
						//�ϳ��� ���ڵ忡�� ���ڿ� ���ڰ� �����ϱ� ������ ������ �ɾ �����������׷��� ������ getCellType ���� �ڷ����� ���ѵ����ʰ� ��� String ó������
						String value=df.formatCellValue(cell);
						System.out.print(value); //���ٻ����� 
					}
					System.out.println("");//�ٹٲ۰�
				}
				//HSSFRow row=sheet.getRow(0); //���ٰ�������
				//HSSFCell cell=row.getCell(0);
						
			}
			catch (FileNotFoundException e) {		
				e.printStackTrace(); //������� �׳� �Ȱ����� ������ �Ϲ������� �ƴϹǷ� POI�̿�! ���ϸ���- > sheet -> row > cell
			} catch (IOException e) {//book=new HSSFWorkbook(fis); �� ���! ����ġ���� 
				e.printStackTrace();
			}
		}
	}
	//��� ���ڵ� �������� 
	public void getList(){
		String sql="select * from hospital order by seq asc";
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		
		try {
			pstmt=con.prepareStatement(sql);
			rs=pstmt.executeQuery();
			
			//�÷��� ����
			ResultSetMetaData meta=rs.getMetaData();
			int count=meta.getColumnCount();
			columnName=new Vector();
			
			for(int i=0; i<count;i++){
				columnName.add(meta.getColumnName(i+1));
				
			}
			
			list = new Vector<Vector>(); //������ ���Ͱ� �� ���� 
			while(rs.next()){ //Ŀ�� ��ĭ ����
		 
				Vector vec = new Vector(); //���ڵ� 1�� ���� �� -> DTO �� JTable ���� X
			
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
	
	
	//������ ���ڵ� ���� 
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
	//���̺� ���� ������ ���� ������ �߻��ϸ�, �� ������ �����ϴ� ������ 
	@Override
	public void tableChanged(TableModelEvent e) {
		System.out.println("���ٲ��?");
	}
	
	
	public static void main(String[] args) {
		new LoadMain();
	}


}
