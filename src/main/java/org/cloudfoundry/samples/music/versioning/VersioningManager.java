package org.cloudfoundry.samples.music.versioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cloudfoundry.samples.music.domain.ApiError;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class VersioningManager {

    private static final Logger logger = LoggerFactory.getLogger(VersioningManager.class);

    public static String VERSION_HEADER_NAME = "x-version";

    private List<VersionHandler> handlers;

    private ObjectMapper objectMapper;

    @Autowired
    public VersioningManager(ObjectMapper objectMapper, List<VersionHandler> handlers) {
        this.objectMapper = objectMapper;
        this.handlers = handlers;
    }

    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, Type targetType) {

        if (inputMessage == null) {
            return null;
        }

        LocalDate localDate = extractVersionDate(inputMessage.getHeaders());
        JsonNode bodyJsonNode = readJsonNode(inputMessage);
        for (VersionHandler handler : this.handlers) {
            handler.beforeBodyRead(bodyJsonNode, targetType, localDate);
        }
        InputStream requestStream = writeJsonNode(bodyJsonNode);
        return new MappingJacksonInputMessage(requestStream, inputMessage.getHeaders());
    }

    private LocalDate extractVersionDate(HttpHeaders headers) {
        try {
            String version = headers.getFirst(VERSION_HEADER_NAME);
            return (version == null ? LocalDate.now() : LocalDate.parse(version));
        } catch (DateTimeParseException e) {
            logger.warn("Error occurred handling version header", e);
            return LocalDate.now();
        }
    }

    private ByteArrayInputStream writeJsonNode(JsonNode bodyJsonNode) {
        try {
            return new ByteArrayInputStream(this.objectMapper.writeValueAsBytes(bodyJsonNode));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode readJsonNode(HttpInputMessage inputMessage) {
        try {
            return this.objectMapper.readValue(inputMessage.getBody(), JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object beforeBodyWrite(Object body, HttpHeaders headers) {

        if (body == null) {
            return null;
        }

        LocalDate localDate = extractVersionDate(headers);
        JsonNode bodyJsonNode = this.objectMapper.convertValue(body, JsonNode.class);
        for (VersionHandler handler : this.handlers) {
            handler.beforeBodyWrite(bodyJsonNode, body.getClass(), localDate);
        }
        return bodyJsonNode;
    }
}
