package org.cloudfoundry.samples.music;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
public class AlbumSchemaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:AlbumSchemaResponse.json")
    private Resource albumSchemaResponse;

    @Test
    public void shouldGetAlbumSchema() throws Exception {

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/schemas/album");
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(StreamUtils.copyToString(this.albumSchemaResponse.getInputStream(), StandardCharsets.UTF_8), result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }
}
