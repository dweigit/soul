package org.dromara.soul.executor.extplugin;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.soul.common.constant.Constants;
import org.dromara.soul.common.dto.zk.AppAuthZkDTO;
import org.dromara.soul.common.dto.zk.RuleZkDTO;
import org.dromara.soul.common.dto.zk.SelectorZkDTO;
import org.dromara.soul.common.enums.PluginEnum;
import org.dromara.soul.common.enums.PluginTypeEnum;
import org.dromara.soul.common.result.SoulResult;
import org.dromara.soul.common.utils.JsonUtils;
import org.dromara.soul.common.utils.LogUtils;
import org.dromara.soul.common.utils.SignUtils;
import org.dromara.soul.web.cache.ZookeeperCacheManager;
import org.dromara.soul.web.plugin.AbstractSoulPlugin;
import org.dromara.soul.web.plugin.SoulPluginChain;
import org.dromara.soul.web.request.RequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @description: 鉴权插件
 * @author: 402536196@qq.com
 * @date: 2019-03-19 21:35
 * @version: V1.0
 */
@Slf4j
public class AuthzPlugin   extends AbstractSoulPlugin {

    private ZookeeperCacheManager zookeeperCacheManager;

    /**
     * Instantiates a new Sign plugin.
     *
     * @param zookeeperCacheManager the zookeeper cache manager
     */
    public AuthzPlugin(final ZookeeperCacheManager zookeeperCacheManager) {
        super(zookeeperCacheManager);
        this.zookeeperCacheManager = zookeeperCacheManager;
    }

    @Override
    public String named() {
        return PluginEnum.AUTHZ.getName();
    }

    @Override
    public int getOrder() {
        return PluginEnum.AUTHZ.getCode();
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final SoulPluginChain chain, final SelectorZkDTO selector, final RuleZkDTO rule) {
        final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);
        final Boolean success = signVerify(Objects.requireNonNull(requestDTO));
        if (!success) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            final SoulResult error = SoulResult.error(HttpStatus.UNAUTHORIZED.value(), Constants.SIGN_IS_NOT_PASS);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap(Objects.requireNonNull(JsonUtils.toJson(error)).getBytes())));
        }
        return chain.execute(exchange);
    }

    /**
     * verify sign .
     *
     * @param requestDTO {@linkplain RequestDTO}
     * @return result : True is pass, False is not pass.
     */
    private Boolean signVerify(final RequestDTO requestDTO) {
        if(StringUtils.isBlank(requestDTO.getAppKey())){
            LogUtils.error(log, () ->  " app key can not incoming!");
            return false;
        }
        final AppAuthZkDTO appAuthZkDTO = zookeeperCacheManager.findAuthDTOByAppKey(requestDTO.getAppKey());
        if (Objects.isNull(appAuthZkDTO)
                || StringUtils.isBlank(requestDTO.getSign())
                || StringUtils.isBlank(requestDTO.getAppKey())
                || StringUtils.isBlank(appAuthZkDTO.getAppKey())
                || StringUtils.isBlank(appAuthZkDTO.getAppSecret())
                || !appAuthZkDTO.getEnabled()) {
            LogUtils.error(log, () -> requestDTO.getAppKey() + " can not config!");
            return false;
        }
        return SignUtils.getInstance().isValid(requestDTO.getSign(),
                buildParamsMap(requestDTO), appAuthZkDTO.getAppSecret());
    }

    /**
     * return plugin type.
     *
     * @return {@linkplain PluginTypeEnum}
     */
    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.BEFORE;
    }

    private Map<String, String> buildParamsMap(final RequestDTO dto) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(4);
        map.put(Constants.TIMESTAMP, dto.getTimestamp());
        map.put(Constants.MODULE, dto.getModule());
        map.put(Constants.METHOD, dto.getMethod());
        map.put(Constants.RPC_TYPE, dto.getRpcType());
        return map;
    }

}
