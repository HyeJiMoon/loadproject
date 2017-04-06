/*
 * 파일과 관련된 작업을 도와주는 재사용성있는 클래스를 정의하짜
 * (ex . LodaMain에서 확장자 잡아내기!)
 * 
 * 
 * 이메서드는 인스턴스 메서드 따라서 어디서든 줄수있게 static 
 * */

package util.file;

public class FileUtil {
	//넘겨받은 경로에서 확장자 구하기
	public static String getExt(String path){ //확장자 받아야하니까 반환형 void -> String //뭘 넘겨받을지 모르니까 매개변수 String path
		//c:/aa/ddd/test....aa.jpg 
		int last=path.lastIndexOf(".");
		
		
		return path.substring(last+1,path.length());
		
		
	}
}
