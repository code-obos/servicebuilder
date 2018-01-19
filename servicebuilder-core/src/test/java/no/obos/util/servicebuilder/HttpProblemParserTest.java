package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.SerializationSpec;
import no.obos.util.servicebuilder.util.JsonUtil;
import no.obos.util.servicebuilder.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HttpProblemParserTest {

    private HttpProblem httpProblem;

    @Before
    public void setUp() {
        Map<String, String> context = new HashMap<>();
        context.put("banos", "banos_val");
        httpProblem = HttpProblem.builder()
                .title("title")
                .detail("detail")
                .context("banos_key", "banos_val")
                .context("nugatti_key", "nugatti_val")
                .incidentReferenceId("incidentReferenceId")
                .suggestedUserMessageInDetail(true)
                .status(403)
                .type("about:null")
                .build();
    }

    @Test
    public void serializeAndDeserializeJson() throws IOException {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper(SerializationSpec.standard);
        String jsonString = objectMapper.writeValueAsString(httpProblem);

        HttpProblem value = objectMapper.readValue(jsonString, HttpProblem.class);
        assertThat(value, is(equalTo(httpProblem)));
    }

    @Test
    public void serializeAndDeserializeXml() throws IOException {
        ObjectMapper xmlMapper = XmlUtil.createObjectMapper(SerializationSpec.standard);

        String xmlString = xmlMapper.writeValueAsString(httpProblem);

        HttpProblem value = xmlMapper.readValue(xmlString, HttpProblem.class);
        assertThat(value, is(equalTo(httpProblem)));
    }
}
