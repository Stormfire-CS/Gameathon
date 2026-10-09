package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamiliesRepository extends JpaRepository<Family, Long> {

    Family findByFamilyAdminId(Long familyAdminId);

    boolean existsByFamilyNameIgnoreCase(String familyName);
}
