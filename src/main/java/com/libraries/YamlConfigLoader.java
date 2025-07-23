package com.libraries;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class YamlConfigLoader {

    private static final String CONFIG_FILE = "application.yml";

    public Credentials loadCredentials() {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException(CONFIG_FILE + " not found in classpath");
            }

            return yaml.loadAs(inputStream, Credentials.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config from " + CONFIG_FILE, e);
        }
    }
}
