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

package org.dromara.soul.web.balance.spi;

import org.dromara.soul.common.dto.convert.DivideUpstream;
import org.dromara.soul.common.enums.LoadBalanceEnum;

import java.util.List;
import java.util.Random;

/**
 * random algorithm impl.
 *
 * @author xiaoyu(Myth)
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    private static final Random RANDOM = new Random();

    @Override
    public DivideUpstream doSelect(final List<DivideUpstream> upstreamList, final String ip) {
        // 总个数
        int length = upstreamList.size();
        // 总权重
        int totalWeight = 0;
        // 权重是否都一样
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = upstreamList.get(i).getWeight();
            // 累计总权重
            totalWeight += weight;
            if (sameWeight && i > 0
                    && weight != upstreamList.get(i - 1).getWeight()) {
                // 计算所有权重是否一样
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = RANDOM.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (DivideUpstream divideUpstream : upstreamList) {
                offset -= divideUpstream.getWeight();
                if (offset < 0) {
                    return divideUpstream;
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return upstreamList.get(RANDOM.nextInt(length));
    }

    /**
     * get algorithm name.
     *
     * @return this is algorithm name.
     */
    @Override
    public String algorithm() {
        return LoadBalanceEnum.RANDOM.getName();
    }
}
