package cn.e3mall.zk.lock.masterselect;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: zkdemo
 * @Auther: GERRY
 * @Date: 2019/1/9 19:21
 * @Description:
 */
public class MasterSelectTest {

    //启动的服务个数
    private static final int CLIENT_COUNT = 10;
    //zookeeper服务器的地址
    private static final String ZOOKEEPER_SERVER = "192.168.221.100:2181";


    public static void main(String[] args) throws Exception {
        //保存所有zkClient的列表
        List<ZkClient> clients = new ArrayList<ZkClient>();
        //保存所有服务的列表
        List<WorkServer>  workServers = new ArrayList<WorkServer>();

        try {
            for ( int i = 0; i < CLIENT_COUNT; ++i ) { // 模拟创建10个服务器并启动
                //创建zkClient
                ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
                clients.add(client);
                //创建serverData
                WorkServerData runningData = new WorkServerData();
                runningData.setCid(Long.valueOf(i));
                runningData.setName("Client #" + i);
                //创建服务
                WorkServer  workServer = new WorkServer(runningData);
                workServer.setZkClient(client);

                workServers.add(workServer);
                workServer.start();
            }

            System.out.println("敲回车键退出！\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

        } finally {

            System.out.println("Shutting down...");

            for ( WorkServer workServer : workServers ) {
                try {
                    workServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for ( ZkClient client : clients ) {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
