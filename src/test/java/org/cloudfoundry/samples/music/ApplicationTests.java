package org.cloudfoundry.samples.music;

import org.cloudfoundry.samples.music.domain.Album;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
public class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrudRepository<Album, String> repository;

    @Test
    public void shouldLoadHome() throws Exception {

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/index.html");
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void shouldLoadSwagger() throws Exception {

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/swagger-ui.html#");
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void shouldLoadSwaggerApiDocs() throws Exception {

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/v2/api-docs");
        this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swagger").value("2.0"))
                .andExpect(jsonPath("$.tags.length()").value(1));
    }

}