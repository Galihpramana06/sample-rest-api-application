package org.cloudfoundry.samples.music.versioning;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDate;

@Component
public class DefaultVersionHandler implements VersionHandler {

    @Override
    public void beforeBodyRead(JsonNode bodyJsonNode, Type targetType, LocalDate localDate) {
        // noop
    }

    @Override
    public void beforeBodyWrite(JsonNode bodyJsonNode, Class<?> returnType, LocalDate localDate) {
        // noop
    }
}
