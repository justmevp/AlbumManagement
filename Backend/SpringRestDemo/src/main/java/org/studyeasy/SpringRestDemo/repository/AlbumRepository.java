package org.studyeasy.SpringRestDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.studyeasy.SpringRestDemo.model.Album;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByAccount_id(long id);
    
}
