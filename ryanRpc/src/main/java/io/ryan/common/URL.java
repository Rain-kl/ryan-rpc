package io.ryan.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class URL {
    private String hostname;
    private Integer port;
}
