package org.cloudfoundry.samples.music.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import org.cloudfoundry.samples.music.domain.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/schemas")
public class AlbumSchemaController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumSchemaController.class);

    private final ObjectMapper objectMapper;

    private final JsonSchemaGenerator schemaGenerator;

    @Autowired
    public AlbumSchemaController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaGenerator = new JsonSchemaGenerator(this.objectMapper);
    }

    @RequestMapping(value = "/album", method = RequestMethod.GET)
    public Object getAlbumSchema(UriComponentsBuilder builder) {
        logger.info("Getting album schema");

        ObjectNode schemaJsonNode = (ObjectNode)schemaGenerator.generateJsonSchema(Album.class);
        UriComponents uriComponents = builder.path("/schemas/album").build();
        schemaJsonNode.put("id", uriComponents.toUriString());

        return schemaJsonNode;
    }
}
