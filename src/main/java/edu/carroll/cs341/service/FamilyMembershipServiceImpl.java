package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.FamiliesRepository;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class FamilyMembershipServiceImpl implements FamilyMembershipService{

    /** Logger used to record service operations and validation outcomes. */
    private static final Logger log = LoggerFactory.getLogger(FamilyMembershipServiceImpl.class);

    /** Status assigned to a membership request awaiting a decision. */
    private static final String PENDING = "PENDING";

    /** Status assigned to an approved family membership. */
    private static final String ACCEPTED = "ACCEPTED";

    /** Status assigned to a rejected family membership request. */
    private static final String REJECTED = "REJECTED";

    /** Repository used to retrieve and save family records. */
    private final FamiliesRepository familiesRepo;

    /** Repository used to retrieve and update family membership records. */
    private final FamilyMembershipRepository membershipRepo;

    /** Repository used to retrieve login records for users. */
    private final UserRepository userRepo;

    /**
     * Creates the family service with its required repositories.
     *
     * @param familiesRepo repository for family records
     * @param membershipRepo repository for family membership records
     * @param userRepo repository for login records
     */
    public FamilyMembershipServiceImpl(FamiliesRepository familiesRepo, FamilyMembershipRepository membershipRepo, UserRepository userRepo) {
        this.familiesRepo = familiesRepo;
        this.membershipRepo = membershipRepo;
        this.userRepo = userRepo;
    }

    /**
     * Retrieves the family associated with a user's accepted membership.
     *
     * @param user the username of the user
     * @return the associated family if the user has exactly one accepted
     *         membership; {@code null} if the user is {@code null}, has a
     *         different number of accepted memberships, or the family is missing
     */
    @Override
    public Family getFamilyForUser(User user) {
        log.info("getFamilyForUser: looking up family for '{}'", user);

        // Checks that a valid user was provided.
        if (user == null) {
            log.error("getFamilyForUser: user is null");
            return null;
        }

        // Only accepted memberships determine which family belongs to the user.
        List<FamilyMembership> memberships = membershipRepo.findByUserAndStatus(user, ACCEPTED);
        if (memberships.size() != 1) {
            log.warn("getFamilyForUser: user {} has {} accepted membership('s)", user, memberships.size());
            return null;
        }

        // Use the first accepted membership to locate the corresponding family.
        FamilyMembership membership = memberships.getFirst();
        Family family = membership.getFamily();
        if (family == null) {
            log.error("getFamilyForUser: family not found for membership {}", membership);
            return null;
        }

        Family familyUser = user.getFamily();
        if (familyUser == null) {
            log.error("getFamilyForUser: Family retrieved was null from the user.");
            return null;
        }

        if(!familyUser.equals(family)) {
            log.error("getFamilyForUser: Family: {} retrieved from users accepted membership {}, does not match family {} retrieved from user {}.", family, membership, user.getFamily(), user);
            return null;
        }

        log.debug("getFamilyForUser: retrieved family '{}'", family);
        return family;
    }

    /**
     * Submits a request for a user to join a family.
     *
     * <p>The request is rejected if the user cannot be found, the family ID
     * is invalid, the family does not exist, or the user already has an
     * accepted membership. A previously rejected request can be resubmitted.</p>
     *
     * @param user the username of the user requesting membership
     * @param familyID the ID of the family the user wants to join
     * @return {@code true} if the request is submitted or resubmitted;
     *         {@code false} if validation fails or an existing request is
     *         already pending or accepted
     */
    @Transactional
    @Override
    public boolean requestToJoinFamily(User user, Long familyID) {
        log.info("requestToJoinFamily: user '{}' requested to join family with ID: {}", user, familyID);

        // A valid user is required to submit a request.
        if (user == null ) {
            log.error("requestToJoinFamily: user is null");
            return false;
        }

        // A valid family ID is required to submit a request.
        if (familyID == null) {
            log.error("requestToJoinFamily: familyID is null");
            return false;
        }

        // Users who already belong to a family cannot request another membership.
        if (hasAcceptedMembership(user)) {
            log.debug("requestToJoinFamily: user '{}' already belongs to a family", user);
            return false;
        }

        // Confirm that the requested family exists before creating a membership.
        Family family = familiesRepo.findById(familyID).orElse(null);

        if (family == null) {
            log.debug("requestToJoinFamily: family {} not found", familyID);
            return false;
        }

        // Check for an existing request from this user to this particular family.
        FamilyMembership previousRequest = membershipRepo.findByFamilyAndUser(family, user);

        if (previousRequest != null) {
            // Pending requests are already awaiting a decision, and accepted
            // requests must not be duplicated.
            if (PENDING.equals(previousRequest.getStatus()) || ACCEPTED.equals(previousRequest.getStatus())) {
                log.debug("requestToJoinFamily: request already exists with status {}", previousRequest.getStatus());
                return false;
            }

            // A rejected request can be reopened by changing its status to pending.
            previousRequest.setStatus(PENDING);
            membershipRepo.save(previousRequest);

            log.info("requestToJoinFamily: user '{}' resubmitted request to join family {}", user, family);
            return true;
        }

        // Create a new pending membership when no previous request exists.
        FamilyMembership request = new FamilyMembership(family, user, PENDING);

        log.debug("requestToJoinFamily: user successfully {} submitted request to join family {} for the first time", user, family);
        membershipRepo.save(request);

        return true;
    }

    /**
     * Retrieves pending membership requests for the family administered
     * by the specified user.
     *
     * @param user the user whose administered family's pending requests
     *             are being retrieved
     * @return a list of pending membership requests, or an empty list if the
     *         user is {@code null} or does not administer a family
     */
    @Override
    public List<FamilyMembership> getPendingRequests(User user) {

        log.info("getPendingRequests: retrieving requests for {}", user);

        // Return an empty list when the supplied username is not associated
        // with a valid login record.
        if (user == null) {
            log.error("getPendingRequests: user was null");
            return new LinkedList<>();
        }

        // Locate the family administered by this user.
        Family family = familiesRepo.findByFamilyAdmin(user);

        // Users who do not administer a family have no requests to review.
        if (family == null) {
            log.warn("getPendingRequests: user '{}' does not administer a family", user);
            return new LinkedList<>();
        }

        // Only requests awaiting an administrator's decision are returned.
        return membershipRepo.findByFamilyAndStatus(family, PENDING);
    }

    /**
     * Accepts a pending membership request if the specified user administers
     * the family associated with that request.
     *
     * <p>The request is not accepted if the administrator or request cannot
     * be found, the request is not pending, the administrator does not
     * administer the family, or the requesting user already has an accepted
     * membership.</p>
     *
     * @param user the family administrator handling the request
     * @param familyMembership the membership request being processed
     * @return {@code true} if the request is accepted and saved;
     *         {@code false} if validation fails
     */
    @Transactional
    @Override
    public boolean acceptRequest(User user, FamilyMembership familyMembership) {
        log.info("acceptRequest: user '{}' attempted to accept request {}", user, familyMembership);

        // A valid user is required to accept a request.
        if (user == null ) {
            log.error("acceptRequest: user is null");
            return false;
        }

        // A non-null membership is required to accept a request.
        if (familyMembership == null ) {
            log.error("acceptRequest: user is null");
            return false;
        }

        if (!PENDING.equals(familyMembership.getStatus())) {
            log.warn("acceptRequest: request not pending, is {}", familyMembership.getStatus());
            return false;
        }

        // Verify that the requesting user is administering the family
        // associated with this membership request.
        Family family = user.getFamily();
        if (family == null) {
            log.warn("acceptRequest: user '{}' does not have a family", user);
            return false;
        }

        if (!family.getFamilyAdmin().equals(user)) {
            log.warn("acceptRequest: user '{}' is not the family admin", user);
            return false;
        }

        // A user cannot be accepted into another family if they already
        // have an accepted membership.
        if (hasAcceptedMembership(familyMembership.getUser())) {
            log.debug("acceptRequest: user {} already belongs to a family", familyMembership.getUser());
            return false;
        }

        // Accept the selected membership request.
        familyMembership.setStatus(ACCEPTED);
        membershipRepo.save(familyMembership);

        // Update the requesting user's family reference.
        User requestingUser = familyMembership.getUser();
        requestingUser.setFamily(family);
        userRepo.save(requestingUser);

        // Reject the user's other pending family requests.
        List<FamilyMembership> pendingRequests =
                membershipRepo.findByUserAndStatus(requestingUser, PENDING);

        for (FamilyMembership request : pendingRequests) {
            // Do not reject the request that was just accepted.
            if (!request.equals(familyMembership)) {
                request.setStatus(REJECTED);
                membershipRepo.save(request);
            }
        }

        log.debug("acceptRequest: user successfully {} accepted membership request from {}",
                user, requestingUser);

        return true;
    }

    /**
     * Rejects a pending membership request if the specified user administers
     * the family associated with that request.
     *
     * @param user the family administrator handling the request
     * @param familyMembership the membership request being processed
     * @return {@code true} if the request is rejected and saved;
     *         {@code false} if validation fails
     */
    @Transactional
    @Override
    public boolean rejectRequest(User user, FamilyMembership familyMembership) {
        log.info("rejectRequest: user {} attempted to reject request from {}", user, familyMembership);

        // A valid user is required to reject a request.
        if (user == null ) {
            log.error("rejectRequest: user is null");
            return false;
        }

        // A non-null membership is required to reject a request.
        if (familyMembership == null ) {
            log.error("rejectRequest: user is null");
            return false;
        }

        if (!PENDING.equals(familyMembership.getStatus())) {
            log.warn("rejectRequest: request {} not pending.", familyMembership);
            return false;
        }

        // Confirm that the current user administers the family tied to the request.
        Family family = user.getFamily();

        if (family == null) {
            log.warn("rejectRequest: no family found for user '{}'", user);
            return false;
        }
        if (!family.getFamilyAdmin().equals(user)) {
            log.warn("rejectRequest: user '{}' is not the family admin", user);
            return false;
        }

        // Mark the request as rejected so it is no longer pending.
        familyMembership.setStatus(REJECTED);
        membershipRepo.save(familyMembership);

        log.debug("rejectRequest: user successfully {} rejected membership request {} from user '{}'", user, familyMembership, familyMembership.getUser());

        return true;
    }

    /**
     * Retrieves a family membership using its ID.
     *
     * @param membershipID the ID of the membership to retrieve
     * @return the matching membership, or {@code null} if it does not exist
     */
    @Override
    public FamilyMembership getMembershipByID(Long membershipID) {
        if (membershipID == null) {
            log.warn("getMembershipByID: membershipID is null");
            return null;
        }

        return membershipRepo.findById(membershipID).orElse(null);
    }

    /**
     * Removes an accepted member from a family administered by the specified user.
     *
     * <p>The operation fails if the user or membership ID is {@code null},
     * the user does not administer a family, the membership does not exist,
     * the membership does not belong to the administrator's family, the
     * membership is not accepted, or the administrator attempts to remove
     * themselves.</p>
     *
     * @param user the family administrator requesting the removal
     * @param membershipID the ID of the membership to remove
     * @return {@code true} if the membership was removed successfully;
     *         {@code false} otherwise
     */
    @Override
    @Transactional
    public boolean removeFamilyMember(User user, Long membershipID) {

        log.info("removeFamilyMember: user {} is attempting to remove family member with membershipID: {}", user, membershipID);

        if (user == null) {
            log.error("removeFamilyMember: user is null");
            return false;
        }
        if(membershipID == null) {
            log.error("removeFamilyMember: membershipID is null");
            return false;
        }

        // Confirm that the current user administers a family.
        Family family = familiesRepo.findByFamilyAdmin(user);

        if (family == null || !family.getFamilyAdmin().equals(user)) {
            log.debug("removeFamilyMember: user '{}' is not the family admin", user);
            return false;
        }

        // Find the membership record.
        FamilyMembership membership = membershipRepo.findById(membershipID).orElse(null);

        if (membership == null) {
            log.debug("removeFamilyMember: membership {} was not found", membershipID);
            return false;
        }

        // Only remove accepted members of this administrator's family.
        if (!family.equals(membership.getFamily()) || !ACCEPTED.equals(membership.getStatus())) {
            log.debug("removeFamilyMember: user {} with membership ID {} is not an accepted member of this family", membership.getUser().getUsername(), membershipID);
            return false;
        }

        // Do not allow the administrator to remove themselves.
        if (membership.getUser().equals(user)) {
            log.debug("removeFamilyMember: administrator cannot remove themselves");
            return false;
        }

        // Remove the member's accepted membership.
        membershipRepo.delete(membership);
        membership.getUser().setFamily(null);
        userRepo.save(membership.getUser());

        log.debug("removeFamilyMember: user '{}' removed '{}' from family '{}'", user.getUsername(), membership.getUser().getUsername(), family.getFamilyName());

        return true;
    }

    /**
     * Retrieves the accepted members of the family associated with the specified
     * user.
     *
     * <p>The family is located through the administrator's family first. If the
     * user is not an administrator, the service attempts to retrieve the family
     * through the user's accepted membership.</p>
     *
     * @param user the user whose family members are being retrieved
     * @return a list of accepted family memberships, or an empty list if the
     *         user is {@code null} or no associated family is found
     */
    @Override
    public List<FamilyMembership> getFamilyMembers(User user) {

        log.info("getFamilyMembers: getting family members for user: {}", user);
        if (user == null) {
            log.error("getFamilyMembers: user was null");
            return new LinkedList<>();
        }

        Family family = getFamilyForUser(user);

        // Ensure family is not null.
        if (family == null) {
            log.debug("getFamilyMembers: user had no family");
            return new LinkedList<>();
        }

        List<FamilyMembership> familyMembers = membershipRepo.findByFamilyAndStatus(family, ACCEPTED);

        log.debug("getFamilyMembers: retrieved family memberships {} for user {}", familyMembers, user);
        return familyMembers;
    }

    /**
     * Allows a family member to leave their current family.
     *
     * @param user the user who wants to leave the family
     * @return {@code true} if the user successfully leaves the family;
     *         {@code false} otherwise
     */
    @Transactional
    @Override
    public boolean leaveFamily(User user) {
        log.info("leaveFamily: user {} tried to leave family.", user);

        if (user == null) {
            log.error("leaveFamily: user is null");
            return false;
        }

        List<FamilyMembership> memberships = membershipRepo.findByUserAndStatus(user, ACCEPTED);

        if (memberships.isEmpty()) {
            log.debug("leaveFamily: user '{}' does not belong to a family", user);
            return false;
        }

        // Get the accepted membership and its associated family.
        FamilyMembership membership = memberships.getFirst();
        Family family = membership.getFamily();

        if (family == null) {
            log.debug("leaveFamily: user '{}' does not belong to a family", user);
            return false;
        }

        // The family administrator cannot leave while still administering it.
        if (family.getFamilyAdmin().equals(user)) {
            log.debug("leaveFamily: administrator '{}' cannot leave their own family", user);
            return false;
        }

        // Remove the membership and clear the user's family reference.
        membershipRepo.delete(membership);
        user.setFamily(null);
        userRepo.save(user);

        log.debug("leaveFamily: user '{}' left family '{}'", user, family);

        return true;
    }

    /**
     * Checks whether a user has an accepted family membership.
     *
     * @param user the user whose accepted memberships are being checked
     * @return {@code true} if the user has at least one accepted membership;
     *         {@code false} otherwise
     */
    private boolean hasAcceptedMembership(User user) {
        // A user with at least one accepted membership already belongs to a family.
        return !membershipRepo.findByUserAndStatus(user, ACCEPTED).isEmpty();
    }

    /**
     * Checks whether a user has a pending family membership request.
     *
     * @param user the user whose pending membership requests are being checked
     * @return {@code true} if the user has at least one pending request;
     *         {@code false} otherwise
     */
    private boolean hasPendingRequest(User user) {
        // Pending requests have not yet been approved or rejected by an administrator.
        return !membershipRepo.findByUserAndStatus(user, PENDING).isEmpty();
    }
}
