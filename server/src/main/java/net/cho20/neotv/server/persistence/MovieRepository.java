package net.cho20.neotv.server.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    MovieEntity findByTitle(String title);

    List<MovieEntity> findAllByDateAfter(Date date);

}
