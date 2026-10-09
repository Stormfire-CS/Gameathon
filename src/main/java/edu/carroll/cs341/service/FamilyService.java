package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Families;
import edu.carroll.cs341.jpa.model.FamilyMembership;

import java.util.List;

public interface FamilyService {

    Families getFamilyForUser(String username);

    boolean createFamily(String username, String familyName);

    boolean requestToJoinFamily(String username, Long familyID);

    List<FamilyMembership> getPendingRequests(String username);

    boolean acceptRequest(String username, Long membershipID);

    boolean rejectRequest(String username, Long membershipID);
}
