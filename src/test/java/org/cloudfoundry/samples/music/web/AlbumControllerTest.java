package org.cloudfoundry.samples.music.web;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.cloudfoundry.samples.music.validators.AlbumValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class AlbumControllerTest {

    private AlbumController albumController;

    @Mock
    private CrudRepository<Album, String> repository;

    @Mock
    private AlbumValidator validator;

    @Mock
    private UriComponentsBuilder uriBuilder;

    @Before
    public void setUp() {
        this.albumController = new AlbumController(this.repository, validator);
    }

    @Test
    public void shouldGetAlbums() throws Exception {
        // given:
        Iterable<Album> expectedAlbums = Mockito.mock(Iterable.class);
        Mockito.when(repository.findAll()).thenReturn(expectedAlbums);

        // when:
        ResponseEntity<Iterable<Album>> albums = this.albumController.albums(null);

        // then:
        assertEquals(HttpStatus.OK, albums.getStatusCode());
        assertSame(expectedAlbums, albums.getBody());
    }

    @Test
    public void shouldAddAlbum() throws Exception {
        // given:
        Album expectedAlbum = new Album();
        Mockito.when(repository.save(expectedAlbum)).thenReturn(expectedAlbum);

        UriComponents uriComponents = Mockito.mock(UriComponents.class);
        URI locationUri = new URI("http://example.com/albums/1");
        Mockito.when(uriComponents.toUri()).thenReturn(locationUri);
        Mockito.when(this.uriBuilder.path("/albums/{id}")).thenReturn(this.uriBuilder);
        Mockito.when(this.uriBuilder.buildAndExpand(expectedAlbum.getId())).thenReturn(uriComponents);

        // when:
        ResponseEntity<Album> response = this.albumController.add(expectedAlbum, this.uriBuilder);

        // then:
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(locationUri, response.getHeaders().getLocation());
        assertSame(expectedAlbum, response.getBody());
    }

    @Test
    public void shouldUpdateAlbum() {
        // given:
        Album expectedAlbum = new Album();
        Mockito.when(repository.save(expectedAlbum)).thenReturn(expectedAlbum);

        // when:
        Album album = this.albumController.update("aid", expectedAlbum);

        // then:
        assertSame(expectedAlbum, album);
    }

    @Test
    public void shoulGetById() {
        // given:
        Album expectedAlbum = new Album();
        Mockito.when(repository.findById("aid")).thenReturn(Optional.ofNullable(expectedAlbum));

        // when:
        Album album = this.albumController.getById("aid");

        // then:
        assertSame(expectedAlbum, album);
    }

    @Test
    public void shouldFailWhenGetByIdUsingInvalidId() {

        // given:
        Mockito.when(repository.findById("aid")).thenReturn(Optional.empty());

        try {
            // when:
            this.albumController.getById("aid");
            fail();
        } catch (ApplicationException e) {
            // then:
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals(1, e.getErrors().size());
            assertEquals("resource-not-found", e.getErrors().get(0).getCode());
            assertEquals("album could not be found for parameters {id=aid}", e.getErrors().get(0).getDescription());
        }
    }

    @Test
    public void shouldDeleteById() {
        // given:

        // when:

        // then:
    }

}