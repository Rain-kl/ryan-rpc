package io.ryan.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;
import java.util.Base64;

@Data
@AllArgsConstructor
public class ServiceURI {
    private String hostname;
    private Integer port;
    private String protocol;

    public static ServiceURI of(URI uri) {
        return new ServiceURI(uri.getHost(), uri.getPort(), uri.getScheme());
    }

    @Override
    public String toString() {
        return protocol + "://" + hostname + ":" + port;
    }

    public String encode() {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(this.toString().getBytes());
    }

    public static ServiceURI decode(String encodedString) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(encodedString);
        return ServiceURI.of(URI.create(new String(decodedBytes)));
    }
}
