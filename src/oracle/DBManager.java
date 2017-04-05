//�̱��� �������� 

package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	static private DBManager instance; //new�� �������� �ڷ������� �������Բ� , getInstance�� �������ֱ� ���� static
	private String driver="oracle.jdbc.driver.OracleDriver";
	private String url="jdbc:oracle:thin:@localhost:1521:XE";
	private String user="batman";
	private String password="1234";
	
	//���ӿ� �ʿ��� ��ü�� 
	Connection con; // ���� ��, �� ���� ��� ��ü
	
	//1.����̹� �ε� 2. ���� 3. �������� 4. �ݳ�
	
	
	//new ���� ���� 
	private DBManager(){
		try {
			Class.forName(driver);
			con=DriverManager.getConnection(url,user,password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//�ν��Ͻ��� new ���� �ʰ� ȣ�����ֱ� ���� static 
	static public DBManager getInstance(){
		if(instance==null){
			instance =new DBManager(); //���� -> ��� �����ϰ� �� ���� if ��  
		}
		return instance;
	}
	//���Ӱ�ü ��ȯ 
	public Connection getConnection(){
		return con;
	}
	//���� ����
	public void disConnect(Connection con){
		if(con!=null){
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			}
		}
	}
}
