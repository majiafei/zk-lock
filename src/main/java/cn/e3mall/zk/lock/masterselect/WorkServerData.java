package cn.e3mall.zk.lock.masterselect;

import java.io.Serializable;


/**
 * @ProjectName: zkdemo
 * @Auther: GERRY
 * @Date: 2019/1/9 19:18
 * @Description: 工作服务器信息
 */

public class WorkServerData implements Serializable {

    private static final long serialVersionUID = 4260577459043203630L;


    private Long cid;
    private String name;
    public Long getCid() {
        return cid;
    }
    public void setCid(Long cid) {
        this.cid = cid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}
