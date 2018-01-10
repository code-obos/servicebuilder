package no.obos.util.template.db.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
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
public class TemplateDb {
    public final Integer id;
    public final String string;
    public final String date;
    public final double myDouble;


    public static TemplateDb ofModel(Template from) {
        return builder()
                .myDouble(from.aDouble)
                .date(isoStringOfDate(from.date))
                .id(from.id)
                .string(from.string)
                .build();
    }

    public Template toModel() {
        return Template.builder()
                .string(string)
                .date(dateOfIsoString(date))
                .aDouble(myDouble)
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
                    .string(r.getString("string"))
                    .id(r.getInt("id"))
                    .myDouble(r.getDouble("double"))
                    .date(r.getString("date"))
                    .build();
        }
    }
}
