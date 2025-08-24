package io.ryan.loadbalance;

import io.ryan.common.dto.URI;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance<URI> {

    @Override
    public URI select(List<URI> URIS) {
        Random random = new Random();
        int nextInt = random.nextInt(URIS.size());
        return URIS.get(nextInt);
    }
}
