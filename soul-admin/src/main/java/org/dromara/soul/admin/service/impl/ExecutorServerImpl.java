package org.dromara.soul.admin.service.impl;


import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.dromara.soul.admin.service.ExecutorService;
import org.dromara.soul.common.constant.ZkPathConstants;
import org.dromara.soul.common.dto.zk.ExecutorZkDTO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO
 * @author: 402536196@qq.com
 * @date: 2019-03-21 18:16
 * @version: V1.0
 */
public class ExecutorServerImpl implements ExecutorService {
    @Autowired
    private ZkClient zkClient;

    public static List<ExecutorZkDTO> servers = new ArrayList();


    @PostConstruct
    public void subscribeExecutorDataChanges() {
        // 注册子节点变更监听（此时path节点并不存在，但可以进行监听注册）
        String executorPath = ZkPathConstants.buildExecutorPath();
        zkClient.subscribeChildChanges(executorPath, (parentPath, currentChilds) -> {
            System.out.println("路径" + parentPath + "下面的子节点变更。子节点为：" + currentChilds);
            servers.clear();
            for (String node : currentChilds) {
                if (node.startsWith("server_")) {
                    ExecutorZkDTO obj = zkClient.readData(executorPath+"/" + node);
                    servers.add(obj);
                    System.out.println(obj.toString());
                }
            }
        });
    }
}
