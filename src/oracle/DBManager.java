//싱글톤 패턴으로 

package oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	static private DBManager instance; //new는 못하지만 자료형으로 가져오게끔 , getInstance가 접근해주기 위해 static
	private String driver="oracle.jdbc.driver.OracleDriver";
	private String url="jdbc:oracle:thin:@localhost:1521:XE";
	private String user="batman";
	private String password="1234";
	
	//접속에 필요한 객체들 
	Connection con; // 접속 후, 그 정보 담는 객체
	
	//1.드라이버 로드 2. 접속 3. 쿼리실행 4. 반납
	
	
	//new 막기 위함 
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
	
	//인스턴스를 new 하지 않고 호출해주기 위해 static 
	static public DBManager getInstance(){
		if(instance==null){
			instance =new DBManager(); //문제 -> 계속 생성하게 됨 따라서 if 로  
		}
		return instance;
	}
	//접속객체 반환 
	public Connection getConnection(){
		return con;
	}
	//접속 해제
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
