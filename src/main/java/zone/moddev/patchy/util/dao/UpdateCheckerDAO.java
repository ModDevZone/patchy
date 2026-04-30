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

package zone.moddev.patchy.util.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jspecify.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;

import java.util.List;

public interface UpdateCheckerDAO {

    @SqlUpdate("INSERT INTO updatenotifier_versions (type, key, version, raw) VALUES (:type, :key, :version, jsonb(:raw)) ON CONFLICT DO NOTHING")
    void addNewVersion(@Bind("type") UpdateCheckerType type, @Bind("key") String key, @Bind("version") String versionId, @Bind("raw") String raw);

    @Nullable
    @SqlQuery("SELECT json(raw) FROM updatenotifier_versions WHERE type = :type AND key = :key ORDER BY id LIMIT 1")
    String getLatest(@Bind("type") UpdateCheckerType type, @Bind("key") String key);

    @SqlBatch("INSERT INTO updatenotifier_versions (type, key, version, raw) VALUES (:type, :key, :version, jsonb(:raw)) ON CONFLICT DO NOTHING")
    void batchUpdate(@Bind("type") UpdateCheckerType type, @Bind("key") List<String> keys, @Bind("version") List<String> versions, @Bind("raw") List<String> raw);
}
