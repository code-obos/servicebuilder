package no.obos.util.template.db.dao;

import no.obos.util.template.db.model.TemplateDb;
import org.eclipse.jetty.server.Authentication;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(TemplateDb.RowMapper.class)
public interface TemplateDao {
        @SqlQuery("SELECT * FROM template WHERE id = :id")
        TemplateDb select(@Bind("id") int id);

        @SqlQuery("SELECT * FROM template ORDER BY startDate")
        List<TemplateDb> selectAll();

        @GetGeneratedKeys
        @SqlUpdate("INSERT INTO template(name, value, startDate) VALUES (:name, :value, :startDate)")
        int insert(@BindBean TemplateDb toInsert);

        @SqlUpdate("UPDATE template SET name = :name, value = :value, startDate = :startDate WHERE id = :updateId")
        void update(@Bind("updateId") int id, @BindBean TemplateDb toInsert);

        @SqlUpdate("DELETE FROM template WHERE id = :id")
        void delete(@Bind("id") int id);
}
