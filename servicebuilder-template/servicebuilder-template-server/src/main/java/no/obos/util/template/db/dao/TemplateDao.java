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

        @SqlQuery("SELECT * FROM template ORDER BY date")
        List<TemplateDb> selectAll();

        @GetGeneratedKeys
        @SqlUpdate("INSERT INTO template(string, double, date) VALUES (:string, :myDouble, :date)")
        int insert(@BindBean TemplateDb toInsert);

        @SqlUpdate("UPDATE template SET string = :string, double = :myDouble, date = :date WHERE id = :updateId")
        void update(@Bind("updateId") int id, @BindBean TemplateDb toInsert);

        @SqlUpdate("DELETE FROM template WHERE id = :id")
        void delete(@Bind("id") int id);
}
