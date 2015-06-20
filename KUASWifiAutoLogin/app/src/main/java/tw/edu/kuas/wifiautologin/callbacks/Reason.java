package tw.edu.kuas.wifiautologin.callbacks;

public class Reason {

    public static String dumpReason(int reason)
    {
        switch (reason)
        {
            case 1: return "登入頁面出現了系統錯誤！";
            case 2: return "沒有指定認證策略！";
            case 3: return "認證策略中自動增加指定的組不存在！";
            case 7: return "用戶已經被列入黑名單！";
            case 8: return "超出帳號最大登入數！";
            case 9: return "帳號綁定檢查失敗！";
            case 10: case 44: case 56: return "帳號不存在！";
            case 11: case 45: case 57: return "密碼不正確！";
            case 12: return "該帳號已經被凍結！";
            case 20:case 21:case 22:case 25: return "連接RADIUS伺服器時，發生了故障(" + reason +")！";
            case 24: return "無法連接到指定的RADIUS伺服器！";
            case 26: return "RADIUS伺服器回應數據不正確！";
            case 27:case 35: return "認證失敗，請檢查您的帳號及密碼 ";
            case 30: return "無法連接到指定的POP3伺服器！";
            case 31:case 32: return "連接POP3伺服器時，發生了故障(" + reason + ")！";
            case 33:case 34: return "POP3伺服器回應數據不正確(" + reason + ")！";
            case 40:case 42:case 43: return "連接LDAP伺服器時，發生了故障(" + reason + ")！";
            case 41: return "無法連接到指定的LDAP伺服器！";
            case 50: return "AD伺服器域名配置錯誤！";
            case 51:case 53:case 54: return "連接AD伺服器時，發生了故障(" + reason + ")！";
            case 52: return "無法連接到指定的AD伺服器！";
            case 55: return "AD伺服器的查詢密碼不正確！";
            case 60: return "登入失敗次數超出最大限制！";
            default: return "web_auth_error_" + reason;
        }
    }

}