package cn.ipman.registry.cluster;

import cn.ipman.registry.RegistryConfigProperties;
import cn.ipman.registry.service.RegistryManService;
import cn.ipman.registry.service.RegistryService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/14 21:00
 */
@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    static String ip;

    @Getter
    Server MYSELF;
    // Server LEADER;

    static {
        try {
            // 获取当前IP
            ip = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            System.out.println(" ===>>> findFirstNonLoopbackHostInfo().getIpAddress() = " + ip);
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
    }

    public Server myself() { // 192.168.31.232
        if (MYSELF == null) {
            Server my = new Server("http://" + ip + ":" + port, false, true, -1);
            System.out.println(" ========>>>>>>  myself: " + my);
            MYSELF = my;
        }
        // 给予注册中心服务里, 最新的版本号
        MYSELF.setVersion(RegistryManService.VERSION.get());
        return MYSELF;
    }

    RegistryConfigProperties registryConfigProperties;

    // 注册中心所有的server
    @Getter
    List<Server> servers;

    // server健康状态检查
    ServerHealth serverHealth;

    public Cluster(RegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }


    private String convertLocalhost(String url) {
        if (url.contains("localhost")) {
            return url.replace("localhost", ip);
        }
        if (url.contains("127.0.0.1")) {
            return url.replace("127.0.0.1", ip);
        }
        return url;
    }

    public void init() {
        // 获取当前Server
        myself();
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerlist()) {
            if (MYSELF.getUrl().equalsIgnoreCase(url)
                    || MYSELF.getUrl().equals(convertLocalhost(url))) {
                // 当前server
                System.out.println("add myself to servers: " + MYSELF);
                servers.add(MYSELF);
            } else {
                // 其它server
                System.out.println("add server to servers: " + url);
                Server server = new Server();
                server.setUrl(convertLocalhost(url));
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1);
                servers.add(server);
            }
        }
        this.servers = servers;
        System.out.println(" =======>>>>>> initialized, servers:" + servers);
        System.out.println(" =======>>>>>> initialized, myself:" + myself());
//        serverHealth = new ServerHealth(this);
//        serverHealth.checkServerHealth();
    }

    public Server getLeader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

    public boolean isLeader() {
        return myself().isLeader();
    }
}
