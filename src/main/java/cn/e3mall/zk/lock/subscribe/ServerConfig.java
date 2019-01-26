package cn.e3mall.zk.lock.subscribe;

/**
 * @ProjectName: zkdemo
 * @Auther: GERRY
 * @Date: 2019/1/9 18:30
 * @Description: 服务配置信息
 */
public class ServerConfig {

    private String dbUrl;
    private String dbPwd;
    private String dbUser;
    public String getDbUrl() {
        return dbUrl;
    }
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    public String getDbPwd() {
        return dbPwd;
    }
    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }
    public String getDbUser() {
        return dbUser;
    }
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    @Override
    public String toString() {
        return "ServerConfig [dbUrl=" + dbUrl + ", dbPwd=" + dbPwd
                + ", dbUser=" + dbUser + "]";
    }

}
