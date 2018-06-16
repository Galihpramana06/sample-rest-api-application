package org.cloudfoundry.samples.music.validators;

import org.cloudfoundry.samples.music.domain.Album;
import org.cloudfoundry.samples.music.repositories.AlbumRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AlbumCreateRequestValidator implements Validator {

    private static final String ALREADY_EXISTS_CODE = "album-already-exists";
    private static final String ALREADY_EXISTS_DESC = "Album already exists for the provided artist and title";

    private AlbumRepository repository;

    public AlbumCreateRequestValidator(CrudRepository<Album, String> repository) {
        if (AlbumRepository.class.isAssignableFrom(repository.getClass())) {
            this.repository = (AlbumRepository) repository;
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Album.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (repository == null) {
            return;
        }

        Album targetAlbum = (Album) target;
        boolean exists = this.repository.existsByArtistAndTitle(targetAlbum.getArtist(), targetAlbum.getTitle());
        if (exists) {
            errors.reject(ALREADY_EXISTS_CODE, ALREADY_EXISTS_DESC);
        }
    }
}
