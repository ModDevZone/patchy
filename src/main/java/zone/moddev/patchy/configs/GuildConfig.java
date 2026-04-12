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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;

public class GuildConfig {

    @JsonProperty("serverName")
    private String serverName;

    @JsonProperty("botControllerRoleId")
    private String botControllerRoleId;

    @JsonProperty("minecraftNewsChannelId")
    private String minecraftNewsChannelId;

    @JsonProperty("blockbenchNewsChannelId")
    private String blockbenchNewsChannelId;

    @JsonProperty("neoForgeNewsChannelId")
    private String neoForgeNewsChannelId;

    @JsonProperty("forgeNewsChannelId")
    private String forgeNewsChannelId;

    @JsonProperty("parchmentNewsChannelId")
    private String parchmentNewsChannelId;

    @JsonProperty("fabricNewsChannelId")
    private String fabricNewsChannelId;

    public GuildConfig() {
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getBotControllerRoleId() {
        return botControllerRoleId;
    }

    public void setBotControllerRoleId(String botControllerRoleId) {
        this.botControllerRoleId = botControllerRoleId;
    }

    public String getMinecraftNewsChannelId() {
        return minecraftNewsChannelId;
    }

    public void setMinecraftNewsChannelId(String minecraftNewsChannelId) {
        this.minecraftNewsChannelId = minecraftNewsChannelId;
    }

    public String getBlockbenchNewsChannelId() {
        return blockbenchNewsChannelId;
    }

    public void setBlockbenchNewsChannelId(String blockbenchNewsChannelId) {
        this.blockbenchNewsChannelId = blockbenchNewsChannelId;
    }

    public String getNeoForgeNewsChannelId() {
        return neoForgeNewsChannelId;
    }

    public void setNeoForgeNewsChannelId(String neoForgeNewsChannelId) {
        this.neoForgeNewsChannelId = neoForgeNewsChannelId;
    }

    public String getForgeNewsChannelId() {
        return forgeNewsChannelId;
    }

    public void setForgeNewsChannelId(String forgeNewsChannelId) {
        this.forgeNewsChannelId = forgeNewsChannelId;
    }

    public String getParchmentNewsChannelId() {
        return parchmentNewsChannelId;
    }

    public void setParchmentNewsChannelId(String parchmentNewsChannelId) {
        this.parchmentNewsChannelId = parchmentNewsChannelId;
    }

    public String getFabricNewsChannelId() {
        return fabricNewsChannelId;
    }

    public void setFabricNewsChannelId(String fabricNewsChannelId) {
        this.fabricNewsChannelId = fabricNewsChannelId;
    }

    @Nullable
    public String getChannelId(UpdateCheckerType type) {
        return switch (type) {
            case MINECRAFT -> getMinecraftNewsChannelId();
            case BLOCKBENCH -> getBlockbenchNewsChannelId();
            case NEOFORGE -> getNeoForgeNewsChannelId();
            case FORGE -> getForgeNewsChannelId();
            case PARCHMENT -> getParchmentNewsChannelId();
            case FABRIC -> getFabricNewsChannelId();
        };
    }
}
