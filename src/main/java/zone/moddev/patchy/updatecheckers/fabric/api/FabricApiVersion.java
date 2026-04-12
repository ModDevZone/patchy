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

package zone.moddev.patchy.updatecheckers.fabric.api;

import org.jetbrains.annotations.Nullable;

public record FabricApiVersion(String fullVersion, String apiPart, String mcPart) {

    /**
     * Parses a Fabric API version string (e.g., "0.87.0+1.20.1") into its components.
     *
     * @param fullVersion The full version string.
     * @return A {@link FabricApiVersion} record, or {@code null} if the string is not in the expected format.
     */
    @Nullable
    public static FabricApiVersion fromString(@Nullable String fullVersion) {
        if (fullVersion == null) {
            return null;
        }
        final String[] parts = fullVersion.split("\\+");
        if (parts.length != 2) {
            // Not a valid Fabric API version string, which must contain a "+"
            return null;
        }
        return new FabricApiVersion(fullVersion, parts[0], parts[1]);
    }
}
