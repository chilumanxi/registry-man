package cn.ipman.registry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述Provider实例
 *
 * @Author IpMan
 * @Date 2024/3/23 14:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
@SuppressWarnings("unused")
public class InstanceMeta {

    private String scheme;
    private String host;
    private Integer port;
    private String context;

    private boolean status; // online or offline
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }


    public String toRcPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "rpcman");
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(
                uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1) // - /rpcman to rpcman
        );
    }

    public String toHttpUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public String toMetas() {
        // 实体的元数据,机房、灰度、单元
        return JSON.toJSONString(this.getParameters());
    }

    public static void main(String[] args) {
        String instanceUrl = "http://192.168.31.232:9082/rpcman";
        InstanceMeta instance = InstanceMeta.from(instanceUrl);
        System.out.println("instance from " +  instance);
    }
}
