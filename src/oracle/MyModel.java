/*
 * JTable 이 수시로 정보를 얻어가는 컨트롤러
 * */
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	Vector columnName; //컬럼의 제목을 담을 벡터 
	Vector<Vector> list; //레코드를 담을 이차원 벡터 
	
	public MyModel(Vector list, Vector columnName) {
		this.list=list;
		this.columnName=columnName;
	}
	
	public int getColumnCount() {
		return columnName.size();
	}
	
//컬럼 이름 바꾸기!!! ABCDE
	public String getColumnName(int col) {
		return (String)columnName.elementAt(col);
	}
	
	
	public int getRowCount() {
	
		return list.size();
	}
	
	//row, col 에 위치한 셀을 편집가능하게 한다. //수정까지 아예 책임져야해! 
	public boolean isCellEditable(int row, int col) {
		boolean flag=false; //평상시엔 변경되지 않아
		if(col==0){
			flag=false;	
		}else{
			flag=true;
			
		}
		
		//return true; true 니까 계쏙변경
		return flag;
	}

	//각셀의 값을 반영하는 메서드 오버라이드
	//이차원 벡터를 인덱스로 조절해서 값변경값을 주어야한다
	
	public void setValueAt(Object value, int row, int col) {
		//층 , 호수를 변경한다!
		Vector vec=list.get(row); //row 한 레코드 
		vec.set(col, value);
		
		this.fireTableCellUpdated(row, col); //방아쇠당기듯 바꼇으면 결과값을 반환해주는 메서드 귀찮아!!!!
	}
	
	
	public Object getValueAt(int row, int col) {
		Vector vec=list.get(row);
		return vec.elementAt(col);
	}
	
}
