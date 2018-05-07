package org.cloudfoundry.samples.music.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String code;
    private String subcode;
    private String description;
    private String localizedMessage;

    public ApiError(String code, String subcode, String description) {
        this(code, subcode,description,null);
    }

    public ApiError(String code, String subcode, String description, String localizedMessage) {
        this.code = code;
        this.subcode = subcode;
        this.description = description;
        this.localizedMessage = localizedMessage;
    }

    public String getCode() {
        return code;
    }

    public String getSubcode() {
        return subcode;
    }

    public String getDescription() {
        return description;
    }

    public String getLocalizedMessage() { return localizedMessage; }
}
