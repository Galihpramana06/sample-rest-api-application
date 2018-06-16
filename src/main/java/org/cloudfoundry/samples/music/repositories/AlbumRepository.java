package org.cloudfoundry.samples.music.repositories;

public interface AlbumRepository {

    boolean existsByArtistAndTitle(String artist, String album);

}
