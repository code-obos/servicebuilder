package no.obos.util.template.db.model;


import lombok.*;
import no.obos.util.template.model.Template;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class TemplateDb {
    public final Integer id;
    public final String name;
    public final String startDate;
    public final double value;


    public static TemplateDb ofModel(Template from) {
        return builder()
                .value(from.value)
                .startDate(isoStringOfDate(from.startDate))
                .id(from.id)
                .name(from.name)
                .build();
    }

    public Template toModel() {
        return Template.builder()
                .name(name)
                .startDate(dateOfIsoString(startDate))
                .value(value)
                .id(id)
                .build();
    }

    private static LocalDate dateOfIsoString(String from) {
        return from == null
                ? null
                : LocalDate.parse(from);
    }

    private static String isoStringOfDate(LocalDate from) {
        return from == null
                ? null
                : from.format(DateTimeFormatter.ISO_DATE);
    }

    public static class RowMapper implements ResultSetMapper<TemplateDb> {
        public TemplateDb map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return TemplateDb.builder()
                    .name(r.getString("name"))
                    .id(r.getInt("id"))
                    .value(r.getDouble("value"))
                    .startDate(r.getString("startDate"))
                    .build();
        }
    }
}
