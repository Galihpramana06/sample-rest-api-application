package org.cloudfoundry.samples.music;

import org.cloudfoundry.samples.music.domain.Album;
import org.hamcrest.Matchers;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
public class AlbumIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrudRepository<Album, String> repository;

    private Album createAlbum() {
        Album album = new Album();
        album.setId("aid");
        album.setArtist("Blah Blah");
        album.setTitle("Blah Blah Title");
        album.setGenre("The Genre");
        album.setReleaseYear("2000");
        album.setTrackCount(5);
        return album;
    }

    @Test
    public void shouldGetAlbums() throws Exception {

        // given
        Album album = createAlbum();
        Mockito.when(this.repository.findAll()).thenReturn(Arrays.asList(album));

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/albums")
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"id\":\"aid\",\"title\":\"Blah Blah Title\",\"artist\":\"Blah Blah\",\"releaseYear\":\"2000\",\"genre\":\"The Genre\",\"trackCount\":5}]";
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldGetAlbum() throws Exception {

        // given
        Album album = createAlbum();
        Mockito.when(this.repository.findById("aid")).thenReturn(Optional.ofNullable(album));

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/albums/aid")
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "{\"id\":\"aid\",\"title\":\"Blah Blah Title\",\"artist\":\"Blah Blah\",\"releaseYear\":\"2000\",\"genre\":\"The Genre\",\"trackCount\":5}";
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldCreateAlbum() throws Exception {

        // given
        String requestContent = "{\"title\":\"New Title\",\"artist\":\"New Artist\",\"releaseYear\":\"2000\",\"genre\":\"New Genre\",\"trackCount\":10}";
        Album album = createAlbum();
        Mockito.when(this.repository.save(Mockito.any())).thenReturn(album);

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/albums")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        assertEquals("http://localhost/albums/aid", result.getResponse().getHeader(HttpHeaders.LOCATION));
        String expected = "{\"id\":\"aid\",\"title\":\"Blah Blah Title\",\"artist\":\"Blah Blah\",\"releaseYear\":\"2000\",\"genre\":\"The Genre\",\"trackCount\":5}";
        assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    public void shouldFailGetAlbumWhenUnexpectedErrorOccurs() throws Exception {

        // given
        Mockito.when(this.repository.findById("aid")).thenThrow(new RuntimeException("kaboom"));

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/albums/aid")
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"operation-failed\", \"description\":\"an unexpected error occurred\"}]";
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldFailToGetAlbumUsingInvalidId() throws Exception {

        // given
        Mockito.when(this.repository.findById("aid")).thenReturn(Optional.empty());

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/albums/aid")
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"resource-not-found\", \"description\":\"album could not be found for parameters {id=aid}\"}]";
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldFailToGetAlbumForInvalidMediaType() throws Exception {

        // given
        Album album = new Album();
        album.setId("aid");
        album.setArtist("Blah Blah");
        Mockito.when(this.repository.findById("aid")).thenReturn(Optional.ofNullable(album));

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/albums/aid")
                .accept(MediaType.APPLICATION_XML);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), result.getResponse().getStatus());
        assertEquals("", result.getResponse().getContentAsString());
    }

    @Test
    public void shouldFailToCreateAlbumMissingAttributesInRequestContent() throws Exception {

        // given
        String requestContent = "{\"title\":null,\"artist\":null,\"releaseYear\":null,\"genre\":null,\"trackCount\":0}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/albums")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"missing-information\", \"subcode\":\"artist-required\"},{\"code\":\"missing-information\", \"subcode\":\"title-required\"}]";
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    @Ignore
    public void shouldFailToGetAlbumsUsingInvalidURL() throws Exception {

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/invalid-path")
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"resource-not-found\"}]";
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldFailToUpdateAlbumUsingInvalidMethod() throws Exception {

        // given
        String requestContent = "{\"title\":\"newTitle\",\"artist\":\"newArtist\",\"releaseYear\":\"2005\",\"genre\":\"newGenre\",\"trackCount\":9}";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch("/albums")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"invalid-method\"}]";
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }

    @Test
    public void shouldFailToUpdateAlbumUsingInvalidRequestMediaType() throws Exception {

        // given
        String requestContent = "<album><id>aid</id></album>";

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/albums")
                .content(requestContent)
                .contentType(MediaType.APPLICATION_XML);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        String expected = "[{\"code\":\"unsupported-request-media-type\", \"description\":\"Content type 'application/xml;charset=UTF-8' not supported\"}]";
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), result.getResponse().getStatus());
        assertEquals(expected, result.getResponse().getContentAsString(), JSONCompareMode.STRICT);
    }
}