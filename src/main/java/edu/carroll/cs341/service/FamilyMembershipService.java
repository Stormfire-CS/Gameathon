package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
 public interface FamilyMembershipService {

    /**
     * Retrieves the family associated with a user's accepted membership.
     *
     * @param user the user whose family is being retrieved
     * @return the user's family, or {@code null} if the user does not
     * belong to a family
     */
    Family getFamilyForUser(User user);

    /**
     * Submits a request for a user to join an existing family.
     *
     * @param user the user requesting membership
     * @param familyID the ID of the family the user wants to join
     * @return {@code true} if the membership request was submitted
     *         successfully; {@code false} otherwise
     */
    boolean requestToJoinFamily(User user, Long familyID);

    /**
     * Retrieves pending membership requests for families administered
     * by the specified user.
     *
     * @param user the family administrator whose pending requests
     * are being retrieved
     * @return a list of pending membership requests, or an empty list
     * if no pending requests are available
     */
    List<FamilyMembership> getPendingRequests(User user);

    /**
     * Accepts a pending family membership request.
     *
     * @param user the family administrator handling the request
     * @param familyMembership the membership request to accept
     * @return {@code true} if the request was accepted successfully;
     *         {@code false} otherwise
     */
    boolean acceptRequest(User user, FamilyMembership familyMembership);

    /**
     * Rejects a pending family membership request.
     *
     * @param user the family administrator handling the request
     * @param familyMembership the membership request to reject
     * @return {@code true} if the request was rejected successfully;
     *         {@code false} otherwise
     */
    boolean rejectRequest(User user, FamilyMembership familyMembership);

    /**
     * Retrieves a family membership by its ID.
     *
     * @param membershipID the ID of the membership to retrieve
     * @return the matching membership, or {@code null} if no matching
     * membership exists
     */
    FamilyMembership getMembershipByID(Long membershipID);

    /**
     * Removes a member from a family using the membership ID.
     *
     * @param user the user requesting the removal
     * @param membershipID the ID of the membership to remove
     * @return {@code true} if the membership was removed successfully;
     *         {@code false} otherwise
     */
    boolean removeFamilyMember(User user, Long membershipID);

    /**
     * Retrieves the members of the family associated with the specified user.
     *
     * @param user the user whose family members are being retrieved
     * @return a list of family memberships, or an empty list if no members
     * are available
     */
    List<FamilyMembership> getFamilyMembers(User user);

    /**
     * Removes the specified user's membership in their current family.
     *
     * @param user the user leaving the family
     * @return {@code true} if the user left the family successfully;
     *         {@code false} otherwise
     */
    boolean leaveFamily(User user);
}
