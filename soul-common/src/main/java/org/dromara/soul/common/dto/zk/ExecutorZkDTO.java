package org.dromara.soul.common.dto.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorZkDTO  implements Serializable {
    private String id;

    private String serverName;

    private String ip;

    private String port;

    private Map<String,Object> env;
}
