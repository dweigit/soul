/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.soul.admin.cache;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.soul.common.constant.ZkPathConstants;
import org.dromara.soul.common.dto.zk.ExecutorZkDTO;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 402536196@qq.com
 * @date 2019/3/21 20:37
 **/
@Slf4j
@Component
@SuppressWarnings("all")
public class ZookeeperCacheManager implements CommandLineRunner, DisposableBean {

    private static final Map<String, ExecutorZkDTO> EXECUTOR_MAP = Maps.newConcurrentMap();

    private final ZkClient zkClient;

    /**
     * Instantiates a new Zookeeper cache manager.
     *
     * @param zkClient the zk client
     */
    @Autowired(required = false)
    public ZookeeperCacheManager(final ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public ExecutorZkDTO findExecutorZkDTOById(final String id) {
        return EXECUTOR_MAP.get(id);
    }

    public List<ExecutorZkDTO> findAllExecutorZkDTO() {
        return new ArrayList<ExecutorZkDTO>(EXECUTOR_MAP.values());
    }


    @Override
    public void run(final String... args) {
        loadWatchExecutor();
    }

    private void loadWatchExecutor() {
        String executorPath = ZkPathConstants.EXECUTOR_PARENT;
        if (!zkClient.exists(executorPath)) {
            zkClient.createPersistent(executorPath, true);
        }
        final List<String> childrenList = zkClient.getChildren(executorPath);
        if (CollectionUtils.isNotEmpty(childrenList)) {
            childrenList.forEach(children -> {
                if (children.startsWith("server_")) {
                    final String realPath = buildRealPath(executorPath, children);
                    final ExecutorZkDTO executorZkDTO = zkClient.readData(realPath);
                    Optional.ofNullable(executorZkDTO).ifPresent(dto -> EXECUTOR_MAP.put(dto.getId(), dto));
                    log.info(realPath + "/init executor_map,current map keys:" + EXECUTOR_MAP.keySet());
                    subscribeExecutorDataChanges(realPath);
                }
            });
        }

        zkClient.subscribeChildChanges(executorPath, (parentPath, currentChilds) -> {
            final List<String> childrenList1 = zkClient.getChildren(executorPath);
            //更新缓存
            if (CollectionUtils.isNotEmpty(childrenList1)) {
                childrenList1.forEach(children -> {
                    if (children.startsWith("server_")) {
                        final String realPath = buildRealPath(executorPath, children);
                        final ExecutorZkDTO executorZkDTO = zkClient.readData(realPath);
                        Optional.ofNullable(executorZkDTO).ifPresent(dto -> EXECUTOR_MAP.put(dto.getId(), dto));
//                        subscribeExecutorDataChanges(realPath);
                    }
                });
                log.info("update executor_map,current map keys:" + EXECUTOR_MAP.keySet());
            }
            //更新节点数据监听
            if (CollectionUtils.isNotEmpty(currentChilds)) {
                final List<String> unsubscribePath = unsubscribePath(childrenList1, currentChilds);
                unsubscribePath.stream().map(children -> buildRealPath(parentPath, children))
                        .forEach(this::subscribeExecutorDataChanges);
            }
        });
    }

    private void subscribeExecutorDataChanges(final String realPath) {
        zkClient.subscribeDataChanges(realPath, new IZkDataListener() {
            @Override
            public void handleDataChange(final String dataPath, final Object data) {
                Optional.ofNullable(data).ifPresent(o -> {
                    ExecutorZkDTO executorZkDTO = (ExecutorZkDTO) o;
                    EXECUTOR_MAP.put(executorZkDTO.getId(), executorZkDTO);
                    log.info("update executor_data,executor key:" + executorZkDTO.getId());
                });
            }

            @Override
            public void handleDataDeleted(final String dataPath) {
//                final String key = dataPath.substring(ZkPathConstants.APP_AUTH_PARENT.length() + 1);
//                EXECUTOR_MAP.remove(key);
            }
        });
    }


    private List<String> unsubscribePath(final List<String> alreadyChildren, final List<String> currentChilds) {
        if (CollectionUtils.isEmpty(alreadyChildren)) {
            return currentChilds;
        }
        return currentChilds.stream().filter(c -> alreadyChildren.stream().anyMatch(a -> !c.equals(a))).collect(Collectors.toList());
    }

    private String buildRealPath(final String parent, final String children) {
        return parent + "/" + children;
    }

    @Override
    public void destroy() {
        zkClient.close();
    }
}
