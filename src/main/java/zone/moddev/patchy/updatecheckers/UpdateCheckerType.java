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

package zone.moddev.patchy.updatecheckers;

public enum UpdateCheckerType {
    MINECRAFT("minecraft", NotificationChannelType.MINECRAFT),
    BLOCKBENCH("blockbench", NotificationChannelType.BLOCKBENCH),
    NEOFORGE("neoforge", NotificationChannelType.NEOFORGE),
    FORGE("forge", NotificationChannelType.FORGE),
    PARCHMENT("parchment", NotificationChannelType.PARCHMENT),
    FABRIC_LOADER("fabric_loader", NotificationChannelType.FABRIC),
    FABRIC_API("fabric_api", NotificationChannelType.FABRIC);

    private final String name;
    private final NotificationChannelType channelType;

    UpdateCheckerType(String name, NotificationChannelType channelType) {
        this.name = name;
        this.channelType = channelType;
    }

    String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public NotificationChannelType getChannelType() {
        return channelType;
    }
}
