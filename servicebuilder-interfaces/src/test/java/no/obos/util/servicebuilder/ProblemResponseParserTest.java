package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.model.ProblemResponse;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProblemResponseParserTest {

    private ProblemResponse problemResponse;

    @Before
    public void setUp() throws Exception {
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
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(problemResponse);

        ProblemResponse value = objectMapper.readValue(jsonString, ProblemResponse.class);
        assertThat(value, is(equalTo(problemResponse)));
    }

    @Test
    public void serializeAndDeserializeXML() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ProblemResponse.class);
        Marshaller marshaller = jc.createMarshaller();
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        marshaller.marshal(problemResponse, output);
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        ProblemResponse unmarshal = (ProblemResponse) unmarshaller.unmarshal(input);
        assertThat(unmarshal, is(equalTo(problemResponse)));
    }
}
