package org.dromara.soul.executor.service;

import cn.hutool.core.util.StrUtil;
import org.I0Itec.zkclient.ZkClient;
import org.dromara.soul.common.constant.ZkPathConstants;
import org.dromara.soul.common.dto.zk.ExecutorZkDTO;
import org.dromara.soul.executor.util.ServerEnvInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: TODO
 * @author: 402536196@qq.com
 * @date: 2019-03-20 13:24
 * @version: V1.0
 */
@Service
public class ExecutorServerRegister {

    @Autowired
    private ZkClient zkClient;

    @Value("${spring.application.name}")
    String serverName;

    @Value("${server.address}")
    String address;

    @Value("${server.port}")
    String port;


    /**
     * @description spring容器初始化Register的实例时执行
     * @author 402536196@qq.com
     * @date 2019/3/20 14:16
     **/
    @PostConstruct
    @Async
    public void register() throws Exception {
        if (StrUtil.isBlank(address) || "127.0.0.1,0.0.0.0,localhost".contains(address)) {
            //如果配置文件中没有指定服务ip获取本机ip
            address = InetAddress.getLocalHost().getHostAddress();
        }
        if (StrUtil.isBlank(port)) {
            port = "9800";
        }

        String executorPath = ZkPathConstants.buildExecutorPath();
        final String serversPath = executorPath + "/servers";
        if (!zkClient.exists(serversPath)) {
            zkClient.createPersistent(serversPath, true);
        }


        Map<String, Object> map = new HashMap<>();
       /* ObjectMapper mapper = new ObjectMapper();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        map.put("os", mapper.writeValueAsString(os));

        CentralProcessor cpu = hal.getProcessor();
        map.put("cpu", mapper.writeValueAsString(cpu));

        GlobalMemory mem = hal.getMemory();
        map.put("mem", mapper.writeValueAsString(mem));

        ComputerSystem cs = hal.getComputerSystem();
        map.put("cs", mapper.writeValueAsString(cs));*/

        map.put("jvm", ServerEnvInfo.jvm());

        final String server_node = executorPath + "/server_";
        zkClient.createEphemeralSequential(server_node, new ExecutorZkDTO(serverName.concat("/"+address).concat(":"+port),serverName, address, port, map));

    }
}
