package com.ethara.repository;

import com.ethara.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner", "members"})
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"owner", "members"})
    @Query("select distinct p from Project p")
    List<Project> findAll();
}
