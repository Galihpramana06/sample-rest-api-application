package org.cloudfoundry.samples.music.validators;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.domain.ApiError;
import org.cloudfoundry.samples.music.errors.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AlbumValidator {

    public void validate(Album album) {
        List<ApiError> errors = new ArrayList<>();
        if (StringUtils.isEmpty(album.getArtist())) {
            errors.add(new ApiError("missing-information", "artist-required", null));
        }
        if (StringUtils.isEmpty(album.getTitle())) {
            errors.add(new ApiError("missing-information", "title-required", null));
        }
        if (!errors.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, errors);
        }
    }
}
