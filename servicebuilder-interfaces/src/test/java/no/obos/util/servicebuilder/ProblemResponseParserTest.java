package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.model.JsonConfig;
import no.obos.util.servicebuilder.model.ProblemResponse;
import no.obos.util.servicebuilder.model.XmlConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProblemResponseParserTest {

    private ProblemResponse problemResponse;

    @Before
    public void setUp() {
        Map<String, String> context = new HashMap<>();
        context.put("banos", "banos_val");
        //        problemResponse = new ProblemResponse("title", "detail", 1, "refId", true, "about:null", context);
        problemResponse = ProblemResponse.builder()
                .title("title")
                .detail("detail")
                .context("banos_key", "banos_val")
                .context("nugatti_key", "nugatti_val")
                .incidentReferenceId("incidentReferenceId")
                .suggestedUserMessageInDetail(true)
                .status(403)
                .type("about:null")
                .build();
        //                ("title", "detail", 1, "refId", true, "about:null", context);
    }

    @Test
    public void serializeAndDeserializeJson() throws IOException {
        ObjectMapper objectMapper = JsonConfig.standard.get();
        String jsonString = objectMapper.writeValueAsString(problemResponse);

        ProblemResponse value = objectMapper.readValue(jsonString, ProblemResponse.class);
        assertThat(value, is(equalTo(problemResponse)));
    }

    @Test
    public void serializeAndDeserializeXml() throws IOException {
        ObjectMapper xmlMapper = XmlConfig.standard.get();

        String xmlString = xmlMapper.writeValueAsString(problemResponse);

        ProblemResponse value = xmlMapper.readValue(xmlString, ProblemResponse.class);
        assertThat(value, is(equalTo(problemResponse)));
    }
}
