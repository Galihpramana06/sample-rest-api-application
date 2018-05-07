package org.cloudfoundry.samples.music.web;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.domain.ApiError;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.cloudfoundry.samples.music.validators.AlbumValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/albums")
public class AlbumController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);
    private CrudRepository<Album, String> repository;
    private AlbumValidator validator;

    @Autowired
    public AlbumController(CrudRepository<Album, String> repository, AlbumValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Album> albums() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Album> add(@RequestBody @Valid Album album, UriComponentsBuilder builder) {
        logger.info("Adding album " + album.getId());
        this.validator.validate(album);
        Album savedAlbum = repository.save(album);

        UriComponents uriComponents = builder.path("/albums/{id}").buildAndExpand(savedAlbum.getId());
        return ResponseEntity.created(uriComponents.toUri()).body(savedAlbum);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Album update(@PathVariable String id, @RequestBody @Valid Album album) {
        logger.info("Updating album " + album.getId());
        album.setId(id);
        return repository.save(album);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Album getById(@PathVariable String id) {
        logger.info("Getting album " + id);
        return repository.findById(id).orElseThrow(() ->
                new ApplicationException(HttpStatus.NOT_FOUND, new ApiError("resource-not-found", null, String.format("album could not be found for parameters {id=%s}", id))));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteById(@PathVariable String id) {
        logger.info("Deleting album " + id);
        repository.deleteById(id);
    }
}