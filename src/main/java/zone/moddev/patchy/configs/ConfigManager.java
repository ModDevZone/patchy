/*
 * MIT License
 *
 * Copyright (c) 2016 - 2026 Mod Dev Zone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package zone.moddev.patchy.configs;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private final ObjectMapper mapper;
    private final Path CONFIG_DIRECTORY = Paths.get("patchy-bot");
    private final Path GUILD_CONFIG_DIRECTORY = CONFIG_DIRECTORY.resolve("guild_config");

    public ConfigManager() {
        // Indent output for better readability.
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        try {
            // Create the directory if it does not exist.
            // This does nothing if it already exists.
            Files.createDirectories(CONFIG_DIRECTORY);
            Files.createDirectories(GUILD_CONFIG_DIRECTORY);
        } catch (IOException exception) {
            // If the directory could not be created, throw an error.
            throw new RuntimeException("Could not create config directory: " + exception.getMessage());
        }
    }

    public PatchyConfig loadPatchyConfig() throws IOException {
        Path path = CONFIG_DIRECTORY.resolve("patchy-config.json");
        if (Files.notExists(path)) {
            // Try to create the default Patchy config file if one does not exist.
            PatchyConfig defaultConfig = new PatchyConfig();
            mapper.writeValue(path.toFile(), defaultConfig);
            return defaultConfig;
        } else {
            // Try to load the existing config file.
            return mapper.readValue(path.toFile(), PatchyConfig.class);
        }
    }

    public GuildConfig loadOrCreateGuildConfig(String guildId) throws IOException {
        Path path = GUILD_CONFIG_DIRECTORY.resolve(guildId + ".json");
        if (Files.notExists(path)) {
            // Create the default Guild config file if one does not exist.
            GuildConfig defaultConfig = new GuildConfig();
            mapper.writeValue(path.toFile(), defaultConfig);
            return defaultConfig;
        } else {
            // Load the existing config file.
            return mapper.readValue(path.toFile(), GuildConfig.class);
        }
    }

    public void saveGuildConfig(String guildId, GuildConfig config) throws IOException {
        Path path = GUILD_CONFIG_DIRECTORY.resolve(guildId + ".json");
        mapper.writeValue(path.toFile(), config);
    }
}
