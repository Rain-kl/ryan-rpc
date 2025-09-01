package io.ryan.utils;

import java.nio.charset.StandardCharsets;

public class Hashing {

    // MurmurHash3_x64_64bit (简化版，取 64 位)
    public static long murmurHash64(String key) {
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        int length = data.length;
        int seed = 0x1234ABCD; // 可以固定一个种子

        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;
        long h1 = seed;
        long h2 = seed;

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(data)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN);

        while (buffer.remaining() >= 16) {
            long k1 = buffer.getLong();
            long k2 = buffer.getLong();

            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        long k1 = 0, k2 = 0;
        int remaining = buffer.remaining();
        switch (remaining) {
            case 15:
                k2 ^= ((long) buffer.get(14) & 0xff) << 48;
            case 14:
                k2 ^= ((long) buffer.get(13) & 0xff) << 40;
            case 13:
                k2 ^= ((long) buffer.get(12) & 0xff) << 32;
            case 12:
                k2 ^= ((long) buffer.get(11) & 0xff) << 24;
            case 11:
                k2 ^= ((long) buffer.get(10) & 0xff) << 16;
            case 10:
                k2 ^= ((long) buffer.get(9) & 0xff) << 8;
            case 9:
                k2 ^= ((long) buffer.get(8) & 0xff);
            case 8:
                k1 ^= ((long) buffer.get(7) & 0xff) << 56;
            case 7:
                k1 ^= ((long) buffer.get(6) & 0xff) << 48;
            case 6:
                k1 ^= ((long) buffer.get(5) & 0xff) << 40;
            case 5:
                k1 ^= ((long) buffer.get(4) & 0xff) << 32;
            case 4:
                k1 ^= ((long) buffer.get(3) & 0xff) << 24;
            case 3:
                k1 ^= ((long) buffer.get(2) & 0xff) << 16;
            case 2:
                k1 ^= ((long) buffer.get(1) & 0xff) << 8;
            case 1:
                k1 ^= ((long) buffer.get(0) & 0xff);
        }

        if (remaining > 0) {
            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            if (remaining > 8) {
                k2 *= c2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= c1;
                h2 ^= k2;
            }
        }

        h1 ^= length;
        h2 ^= length;
        h1 += h2;
        h2 += h1;

        // fmix64
        h1 ^= h1 >>> 33;
        h1 *= 0xff51afd7ed558ccdL;
        h1 ^= h1 >>> 33;
        h1 *= 0xc4ceb9fe1a85ec53L;
        h1 ^= h1 >>> 33;

        h2 ^= h2 >>> 33;
        h2 *= 0xff51afd7ed558ccdL;
        h2 ^= h2 >>> 33;
        h2 *= 0xc4ceb9fe1a85ec53L;
        h2 ^= h2 >>> 33;

        h1 += h2;
        h2 += h1;

        return h1 & 0x7fffffffffffffffL; // 取低 64 位非负值
    }
}
