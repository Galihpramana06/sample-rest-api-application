package org.cloudfoundry.samples.music.versioning;

import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Type;
import java.time.LocalDate;

public interface VersionHandler {
    void beforeBodyRead(JsonNode bodyJsonNode, Type targetType, LocalDate localDate);
    void beforeBodyWrite(JsonNode bodyJsonNode, Class<?> returnType, LocalDate localDate);
}
