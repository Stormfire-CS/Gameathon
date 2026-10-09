package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.Families;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamiliesRepository extends JpaRepository<Families, Long> {

    List<Families> findByFamilyAdminId(Long familyAdminId);

    boolean existsByFamilyNameIgnoreCase(String familyName);
}
