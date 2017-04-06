/*
 * JTable �� ���÷� ������ ���� ��Ʈ�ѷ�
 * */
package oracle;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class MyModel extends AbstractTableModel{
	Vector columnName; //�÷��� ������ ���� ���� 
	Vector<Vector> list; //���ڵ带 ���� ������ ���� 
	
	public MyModel(Vector list, Vector columnName) {
		this.list=list;
		this.columnName=columnName;
	}
	
	public int getColumnCount() {
		return columnName.size();
	}
	
//�÷� �̸� �ٲٱ�!!! ABCDE
	public String getColumnName(int col) {
		return (String)columnName.elementAt(col);
	}
	
	
	public int getRowCount() {
	
		return list.size();
	}
	
	//row, col �� ��ġ�� ���� ���������ϰ� �Ѵ�. //�������� �ƿ� å��������! 
	public boolean isCellEditable(int row, int col) {
		boolean flag=false; //���ÿ� ������� �ʾ�
		if(col==0){
			flag=false;	
		}else{
			flag=true;
			
		}
		
		//return true; true �ϱ� ��ﺯ��
		return flag;
	}

	//������ ���� �ݿ��ϴ� �޼��� �������̵�
	//������ ���͸� �ε����� �����ؼ� �����氪�� �־���Ѵ�
	
	public void setValueAt(Object value, int row, int col) {
		//�� , ȣ���� �����Ѵ�!
		Vector vec=list.get(row); //row �� ���ڵ� 
		vec.set(col, value);
		
		this.fireTableCellUpdated(row, col); //��Ƽ���� �ٲ����� ������� ��ȯ���ִ� �޼��� ������!!!!
	}
	
	
	public Object getValueAt(int row, int col) {
		Vector vec=list.get(row);
		return vec.elementAt(col);
	}
	
}
