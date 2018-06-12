package org.cloudfoundry.samples.music.web;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.domain.ApiError;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.cloudfoundry.samples.music.validators.AlbumValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/albums", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlbumController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);
    private CrudRepository<Album, String> repository;
    private AlbumValidator validator;

    private Date lastModified = new Date();

    @Autowired
    public AlbumController(CrudRepository<Album, String> repository, AlbumValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Iterable<Album>> albums(@RequestHeader(name=HttpHeaders.IF_MODIFIED_SINCE, required = false) Date modifiedSince) throws Exception {

        logger.info("Initiating GET albums request. Modified Since Header: "+modifiedSince);
        HttpHeaders headers = new HttpHeaders();
        headers.setLastModified(lastModified.getTime());
        if (modifiedSince != null && (modifiedSince.getTime() - this.lastModified.getTime()) >= -1000) {
            logger.info("Returning Not Modified response");
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }

        TimeUnit.SECONDS.sleep(5);
        logger.info("Returning GET albums response");
        return ResponseEntity.ok().headers(headers).body(repository.findAll());
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Album> add(@RequestBody @Valid Album album, UriComponentsBuilder builder) {
        logger.info("Adding album " + album.getId());
        this.validator.validate(album);
        Album savedAlbum = repository.save(album);
        this.lastModified = new Date();

        UriComponents uriComponents = builder.path("/albums/{id}").buildAndExpand(savedAlbum.getId());
        return ResponseEntity.created(uriComponents.toUri()).body(savedAlbum);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Album update(@PathVariable String id, @RequestBody @Valid Album album) {
        logger.info("Updating album " + album.getId());
        album.setId(id);
        this.lastModified = new Date();
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
        this.lastModified = new Date();
        repository.deleteById(id);
    }
}