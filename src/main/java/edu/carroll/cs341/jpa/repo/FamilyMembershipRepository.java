package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.FamilyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyMembershipRepository extends JpaRepository<FamilyMembership, Long> {

    List<FamilyMembership> findByUserIDAndStatus(Long userID, String status);

    List<FamilyMembership> findByFamilyIDAndStatus(Long familyID, String status);

    FamilyMembership findByFamilyIDAndUserID(Long familyID, Long userID);
}
