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
    @SqlQuery("SELECT json(raw) FROM updatenotifier_versions WHERE type = :type AND key = :key ORDER BY id DESC LIMIT 1")
    String getLatest(@Bind("type") UpdateCheckerType type, @Bind("key") String key);

    @SqlBatch("INSERT INTO updatenotifier_versions (type, key, version, raw) VALUES (:type, :key, :version, jsonb(:raw)) ON CONFLICT DO NOTHING")
    void batchUpdate(@Bind("type") UpdateCheckerType type, @Bind("key") List<String> keys, @Bind("version") List<String> versions, @Bind("raw") List<String> raw);
}
