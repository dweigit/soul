package org.dromara.soul.executor.util;


import cn.hutool.json.JSONUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: TODO
 * @author: 402536196@qq.com
 * @date: 2019-03-21 17:39
 * @version: V1.0
 */
public class ServerEnvInfo {

    public static String jvm(){
        Map<String,Object> map = new HashMap<>();
        //堆内存信息/方法区内存信息
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        map.put("heap.memory",memBean.getHeapMemoryUsage());
        map.put("method.memory",memBean.getNonHeapMemoryUsage());

        //运行时设置的JVM参数
        List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        map.put("jvm.args",inputArgs);

        //运行时内存情况,总的内存量/空闲的内存量/最大的内存量
        map.put("runtime.mem.total",Runtime.getRuntime().totalMemory());
        map.put("runtime.mem.free",Runtime.getRuntime().freeMemory());
        map.put("runtime.mem.max",Runtime.getRuntime().maxMemory());

        return JSONUtil.toJsonStr(map);
    }
}
