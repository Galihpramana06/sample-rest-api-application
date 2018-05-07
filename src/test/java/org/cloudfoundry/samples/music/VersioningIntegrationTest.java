package org.cloudfoundry.samples.music;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cloudfoundry.samples.music.Application;
import org.cloudfoundry.samples.music.versioning.VersionHandler;
import org.cloudfoundry.samples.music.versioning.VersioningManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.lang.reflect.Type;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration(classes = VersioningIntegrationTest.VersioningController.class)
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
public class VersioningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    public static class VersioningController {
        @RequestMapping(value ="/versions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public VersionedEntity create(@RequestBody @Valid VersionedEntity versionedEntity) {
            VersionedEntity newEntity = new VersionedEntity();
            newEntity.name = versionedEntity.name + " applied";
            return newEntity;
        }
    }

    public static class VersionedEntity {
        public String name;
    }

    @Component
    public static class VersioningTestVersionHandler implements VersionHandler {
        private static final LocalDate VERSION_DATE = LocalDate.parse("2017-01-01");
        @Override
        public void beforeBodyRead(JsonNode bodyJsonNode, Type targetType, LocalDate localDate) {

            if (VersioningTestVersionHandler.VERSION_DATE.isBefore(localDate)) {
                return;
            }

            if (VersionedEntity.class.getName().equals(targetType.getTypeName())) {
                ObjectNode bodyObjectNode = (ObjectNode)bodyJsonNode;
                bodyObjectNode.set("name", bodyJsonNode.get("oldName"));
                bodyObjectNode.remove("oldName");
            }
        }
        @Override
        public void beforeBodyWrite(JsonNode bodyJsonNode, Class<?> returnType, LocalDate localDate) {

            if (VersioningTestVersionHandler.VERSION_DATE.isBefore(localDate)) {
                return;
            }

            if (VersionedEntity.class.equals(returnType)) {
                ObjectNode bodyObjectNode = (ObjectNode)bodyJsonNode;
                bodyObjectNode.set("oldName2", bodyJsonNode.get("name"));
                bodyObjectNode.remove("name");
            }
        }
    }

    @Test
    public void shouldApplyVersioningToEntity() throws Exception {

        // given
        String requestContent = "{\"oldName\":\"test name\"}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/versions")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(VersioningManager.VERSION_HEADER_NAME, "2017-01-01");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String responseContent = "{\"oldName2\":\"test name applied\"}";
        assertEquals(responseContent, result.getResponse().getContentAsString());
    }

    @Test
    public void shouldNotApplyVersioningToEntity() throws Exception {

        // given
        String requestContent = "{\"name\":\"test name\"}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/versions")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(VersioningManager.VERSION_HEADER_NAME, "2017-01-02");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String responseContent = "{\"name\":\"test name applied\"}";
        assertEquals(responseContent, result.getResponse().getContentAsString());
    }

    @Test
    public void shouldNotApplyVersioningToEntityWithoutVersionHeader() throws Exception {

        // given
        String requestContent = "{\"name\":\"test name\"}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/versions")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String responseContent = "{\"name\":\"test name applied\"}";
        assertEquals(responseContent, result.getResponse().getContentAsString());
    }

    @Test
    public void shouldThrowBadRequestUsingInvalidVersionHeader() throws Exception {

        // given
        String requestContent = "{\"oldName\":\"test name\"}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/versions")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(VersioningManager.VERSION_HEADER_NAME, "2017-1-1");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String responseContent = "{\"name\":\"null applied\"}";
        assertEquals(responseContent, result.getResponse().getContentAsString());
    }
}
