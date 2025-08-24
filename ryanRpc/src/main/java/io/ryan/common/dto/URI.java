package io.ryan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class URI {
    private String hostname;
    private Integer port;
    private String protocol;
}
