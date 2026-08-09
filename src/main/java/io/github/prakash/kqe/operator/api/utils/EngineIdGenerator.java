package io.github.prakash.kqe.operator.api.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public final class EngineIdGenerator {
    private EngineIdGenerator() {
    }
    public static String generate(String bootstrapServers, String topic) {

        String input = bootstrapServers + "|" + topic;

        String hash = DigestUtils.md5DigestAsHex(
                input.getBytes(StandardCharsets.UTF_8));

        return "kqe-" + hash.substring(0, 12);
    }
}
