package org.studyeasy.SpringRestDemo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.studyeasy.SpringRestDemo.model.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
  List<Photo> findByAlbum_id(long id);
    
}
