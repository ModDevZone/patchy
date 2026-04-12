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

import net.dv8tion.jda.api.entities.Guild;
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
    private final Path CONFIG_DIRECTORY = Paths.get("patchy-bot-configs");
    private final Path GUILD_CONFIG_DIRECTORY = CONFIG_DIRECTORY.resolve("guild-configs");
    private final PatchyConfig patchyConfig;

    public ConfigManager() {
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        try {
            Files.createDirectories(CONFIG_DIRECTORY);
            Files.createDirectories(GUILD_CONFIG_DIRECTORY);
            this.patchyConfig = loadPatchyConfig();
        } catch (IOException exception) {
            throw new RuntimeException("Could not create or load config directory/files: " + exception.getMessage());
        }
    }

    private PatchyConfig loadPatchyConfig() throws IOException {
        Path path = CONFIG_DIRECTORY.resolve("patchy-config.json");
        if (Files.notExists(path)) {
            PatchyConfig defaultConfig = new PatchyConfig();
            mapper.writeValue(path.toFile(), defaultConfig);
            return defaultConfig;
        } else {
            return mapper.readValue(path.toFile(), PatchyConfig.class);
        }
    }

    public PatchyConfig getPatchyConfig() {
        return patchyConfig;
    }

    public GuildConfig loadOrCreateGuildConfig(Guild guild) throws IOException {
        Path path = GUILD_CONFIG_DIRECTORY.resolve(guild.getId() + ".json");
        if (Files.notExists(path)) {
            GuildConfig defaultConfig = new GuildConfig();
            defaultConfig.setServerName(guild.getName());
            mapper.writeValue(path.toFile(), defaultConfig);
            return defaultConfig;
        } else {
            GuildConfig config = mapper.readValue(path.toFile(), GuildConfig.class);
            // Update the server name if it has changed
            if (!guild.getName().equals(config.getServerName())) {
                config.setServerName(guild.getName());
                saveGuildConfig(guild.getId(), config);
            }
            return config;
        }
    }

    public void saveGuildConfig(String guildId, GuildConfig config) throws IOException {
        Path path = GUILD_CONFIG_DIRECTORY.resolve(guildId + ".json");
        mapper.writeValue(path.toFile(), config);
    }
}
