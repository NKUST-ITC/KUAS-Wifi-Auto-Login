package tw.edu.kuas.wifiautologin.models;

public class UserModel {

	public enum LoginType {
		STUDENT, CYBER, DORM, TEACHER, GUEST
	}

	public String username;
	public String userpwd;
	public String idtype;
	public LoginType loginType;

}
