package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

/**
 * Tests the {@link FamilyMembershipService} methods responsible for retrieving family memberships
 * and managing join requests, member removal, and departures.
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the services use their configured dependencies. The
 * {@link Transactional} annotation rolls back database changes after each
 * test.</p>
 */
@SpringBootTest
@Transactional
public class FamilyMembershipServiceTest {
    private static final String username = "membershiptestuser";
    private static final String password = "membershiptestpass";

    @Autowired
    private FamilyMembershipService membershipService;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * = = = = = = = = = = =
     * Get Family For User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that an accepted member's family is returned.
     */
    @Test
    public void getFamilyForUserSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);

        Family result = membershipService.getFamilyForUser(member);

        assertEquals("getFamilyForUserSuccessTest: should return the member's family",
                family, result);
    }

    /**
     * Verifies that a null user has no family.
     */
    @Test
    public void getFamilyForUserNullUserTest() {
        Family result = membershipService.getFamilyForUser(null);

        assertNull("getFamilyForUserNullUserTest: should return null", result);
    }

    /**
     * Verifies that pending and rejected memberships do not establish a family.
     */
    @Test
    public void getFamilyForUserNoAcceptedMembershipTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        User rejectedUser = insertTestingUser("rejecteduser");

        // No membership requests for the applicant
        Family result = membershipService.getFamilyForUser(applicant);
        assertNull("getFamilyForUserNoAcceptedMembershipTest: a user with no requests should have no family", result);

        // Pending and rejected requests
        insertTestingPendingRequest(family, applicant);
        insertTestingRejectedRequest(family, rejectedUser);

        var actualFamilyForUser = membershipService.getFamilyForUser(applicant);
        assertNull("getFamilyForUserNoAcceptedMembershipTest: pending user should have no family",
                actualFamilyForUser);
        var actualFamilyForUser2 = membershipService.getFamilyForUser(rejectedUser);
        assertNull("getFamilyForUserNoAcceptedMembershipTest: rejected user should have no family",
                actualFamilyForUser2);
    }

    /**
     * Verifies that an accepted membership requires a matching user family reference.
     */
    @Test
    public void getFamilyForUserNullFamilyReferenceTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingUser("member");
        insertTestingInconsistentMembership(family, member, "ACCEPTED");

        Family result = membershipService.getFamilyForUser(member);

        assertNull("getFamilyForUserNullFamilyReferenceTest: should reject a missing reference",
                result);
    }

    /**
     * Verifies that inconsistent family references are rejected.
     */
    @Test
    public void getFamilyForUserMismatchedFamilyTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User member = insertTestingUser("member");

        insertTestingInconsistentMembership(family, member, "ACCEPTED");
        member.setFamily(otherFamily);
        entityManager.flush();

        Family result = membershipService.getFamilyForUser(member);

        assertNull("getFamilyForUserMismatchedFamilyTest: should reject mismatched families",
                result);
    }

    /**
     * Verifies that multiple accepted memberships are rejected as ambiguous.
     */
    @Test
    public void getFamilyForUserMultipleAcceptedMembershipsTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User member = insertTestingMember("member", family);

        insertTestingInconsistentMembership(otherFamily, member, "ACCEPTED");

        Family result = membershipService.getFamilyForUser(member);

        assertNull("getFamilyForUserMultipleAcceptedMembershipsTest: should return null",
                result);
    }

    /*
     * = = = = = = = = = = =
     * Request To Join Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a new request is saved as pending without assigning a family.
     */
    @Test
    public void requestToJoinFamilySuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");

        boolean result = membershipService.requestToJoinFamily(
                applicant, family.getFamilyID());

        assertTrue("requestToJoinFamilySuccessTest: should submit the request", result);

        FamilyMembership request = findTestingPendingRequest(family, applicant);

        assertNotNull("requestToJoinFamilySuccessTest: should save the request", request);
        var actualStatus = request.getStatus();
        assertEquals("requestToJoinFamilySuccessTest: should mark the request pending",
                "PENDING", actualStatus);
        var actualFamily = applicant.getFamily();
        assertNull("requestToJoinFamilySuccessTest: should not assign a family yet",
                actualFamily);
    }

    /**
     * Verifies that a null user cannot request membership.
     */
    @Test
    public void requestToJoinFamilyNullUserTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);

        boolean result = membershipService.requestToJoinFamily(
                null, family.getFamilyID());

        assertFalse("requestToJoinFamilyNullUserTest: should reject a null user",
                result);
    }

    /**
     * Verifies that a null family ID is rejected.
     */
    @Test
    public void requestToJoinFamilyNullFamilyIDTest() {
        User user = insertTestingUser();

        boolean result = membershipService.requestToJoinFamily(user, null);

        assertFalse("requestToJoinFamilyNullFamilyIDTest: should reject a null ID",
                result);
    }

    /**
     * Verifies that a nonexistent family ID is rejected.
     */
    @Test
    public void requestToJoinFamilyNonexistentFamilyTest() {
        User user = insertTestingUser();

        boolean result = membershipService.requestToJoinFamily(user, -1L);

        assertFalse("requestToJoinFamilyNonexistentFamilyTest: should reject a nonexistent family",
                result);
        assertEquals("requestToJoinFamilyNonexistentFamilyTest: should not save a membership", 0L, countMemberships(user));
    }

    /**
     * Verifies that an accepted member cannot request another family.
     */
    @Test
    public void requestToJoinFamilyAlreadyMemberTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);

        boolean result = membershipService.requestToJoinFamily(
                admin, otherFamily.getFamilyID());

        assertFalse("requestToJoinFamilyAlreadyMemberTest: should reject an existing member",
                result);
        assertEquals("requestToJoinFamilyAlreadyMemberTest: should preserve only the original membership",
                1L, countMemberships(admin));
        assertEquals("requestToJoinFamilyAlreadyMemberTest: should preserve the original family",
                family, membershipService.getFamilyForUser(admin));
    }

    /**
     * Verifies that a pending request cannot be duplicated.
     */
    @Test
    public void requestToJoinFamilyDuplicatePendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request = insertTestingPendingRequest(family, applicant);
        long previousCount = countMemberships(applicant);

        boolean result = membershipService.requestToJoinFamily(
                applicant, family.getFamilyID());

        assertFalse("requestToJoinFamilyDuplicatePendingTest: should reject a duplicate",
                result);
        var savedCount = countMemberships(applicant);
        assertEquals("requestToJoinFamilyDuplicatePendingTest: should preserve the record count",
                previousCount, savedCount);
        var actualStatus = request.getStatus();
        assertEquals("requestToJoinFamilyDuplicatePendingTest: should keep the request pending",
                "PENDING", actualStatus);
    }

    /**
     * Verifies that rejected requests are reopened using the existing record.
     */
    @Test
    public void requestToJoinFamilyRejectedRequestTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request = insertTestingRejectedRequest(family, applicant);
        Long originalID = request.getMembershipID();
        long previousCount = countMemberships(applicant);

        boolean result = membershipService.requestToJoinFamily(
                applicant, family.getFamilyID());

        assertTrue("requestToJoinFamilyRejectedRequestTest: should reopen the request",
                result);

        FamilyMembership reopened = findTestingPendingRequest(family, applicant);

        var actualMembershipID = reopened.getMembershipID();
        assertEquals("requestToJoinFamilyRejectedRequestTest: should reuse the record",
                originalID, actualMembershipID);
        var actualStatus = reopened.getStatus();
        assertEquals("requestToJoinFamilyRejectedRequestTest: should mark it pending",
                "PENDING", actualStatus);
        var savedCount = countMemberships(applicant);
        assertEquals("requestToJoinFamilyRejectedRequestTest: should preserve the record count",
                previousCount, savedCount);
    }

    /*
     * = = = = = = = = = = =
     * Get Pending Requests Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that only pending requests for the administrator's family are returned.
     */
    @Test
    public void getPendingRequestsSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);

        // One family without any pending requests
        List<FamilyMembership> result = membershipService.getPendingRequests(admin);
        assertNotNull("getPendingRequestsSuccessTest: should return a list before any requests", result);
        assertTrue("getPendingRequestsSuccessTest: should start with no pending requests", result.isEmpty());

        // Two families with separate pending requests
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);

        FamilyMembership pending = insertTestingPendingRequest(family, insertTestingUser("applicant"));
        insertTestingMember("member", family);
        insertTestingRejectedRequest(family, insertTestingUser("rejecteduser"));
        FamilyMembership otherPending = insertTestingPendingRequest(otherFamily, insertTestingUser("otherapplicant"));

        // Check the first family's pending requests.
        result = membershipService.getPendingRequests(admin);
        int requestCount = result.size();
        assertEquals("getPendingRequestsSuccessTest: first administrator should see one request", 1, requestCount);
        Long membershipID = result.getFirst().getMembershipID();
        assertEquals("getPendingRequestsSuccessTest: first administrator should see their family's request",
                pending.getMembershipID(), membershipID);

        // Check the second family's pending requests.
        result = membershipService.getPendingRequests(otherAdmin);
        requestCount = result.size();
        assertEquals("getPendingRequestsSuccessTest: second administrator should see one request", 1, requestCount);
        membershipID = result.getFirst().getMembershipID();
        assertEquals("getPendingRequestsSuccessTest: second administrator should see their family's request",
                otherPending.getMembershipID(), membershipID);
    }

    /**
     * Verifies that a null user receives an empty list.
     */
    @Test
    public void getPendingRequestsNullUserTest() {
        List<FamilyMembership> result = membershipService.getPendingRequests(null);

        assertNotNull("getPendingRequestsNullUserTest: should return a list", result);
        var emptyResult = result.isEmpty();
        assertTrue("getPendingRequestsNullUserTest: should return an empty list",
                emptyResult);
    }

    /**
     * Verifies that a regular member cannot retrieve pending requests.
     */
    @Test
    public void getPendingRequestsNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        insertTestingPendingRequest(family, insertTestingUser("applicant"));

        List<FamilyMembership> result = membershipService.getPendingRequests(member);

        var emptyResult = result.isEmpty();
        assertTrue("getPendingRequestsNotAdminTest: should return an empty list",
                emptyResult);
    }

    /*
     * = = = = = = = = = = =
     * Accept Request Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that acceptance assigns the family and rejects other pending requests.
     */
    @Test
    public void acceptRequestSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User applicant = insertTestingUser("applicant");

        FamilyMembership request = insertTestingPendingRequest(family, applicant);
        FamilyMembership otherRequest =
                insertTestingPendingRequest(otherFamily, applicant);

        boolean result = membershipService.acceptRequest(admin, request);

        assertTrue("acceptRequestSuccessTest: should accept the request", result);
        assertEquals("acceptRequestSuccessTest: should save the accepted status",
                "ACCEPTED", savedMembership(request).getStatus());
        assertEquals("acceptRequestSuccessTest: should assign the family",
                family, userService.validateUser("applicant", password).getFamily());
        assertEquals("acceptRequestSuccessTest: should reject other pending requests",
                "REJECTED", savedMembership(otherRequest).getStatus());
    }

    /**
     * Verifies that a null administrator is rejected.
     */
    @Test
    public void acceptRequestNullUserTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.acceptRequest(null, request);

        assertFalse("acceptRequestNullUserTest: should reject a null user", result);
        assertEquals("acceptRequestNullUserTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that a null request is rejected.
     */
    @Test
    public void acceptRequestNullRequestTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);

        boolean result = membershipService.acceptRequest(admin, null);

        assertFalse("acceptRequestNullRequestTest: should reject a null request",
                result);
    }

    /**
     * Verifies that rejected requests cannot be accepted without resubmission.
     */
    @Test
    public void acceptRequestNotPendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingRejectedRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.acceptRequest(admin, request);

        assertFalse("acceptRequestNotPendingTest: should reject a nonpending request",
                result);
        assertEquals("acceptRequestNotPendingTest: should preserve the rejected status",
                "REJECTED", savedMembership(request).getStatus());
    }

    /**
     * Verifies that a user without a family cannot accept requests.
     */
    @Test
    public void acceptRequestNoFamilyTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User outsider = insertTestingUser("outsider");
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.acceptRequest(outsider, request);

        assertFalse("acceptRequestNoFamilyTest: should reject a user without a family",
                result);
        assertEquals("acceptRequestNoFamilyTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that regular members cannot accept requests.
     */
    @Test
    public void acceptRequestNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.acceptRequest(member, request);

        assertFalse("acceptRequestNotAdminTest: should reject a regular member", result);
        assertEquals("acceptRequestNotAdminTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that an applicant already accepted elsewhere cannot be accepted again.
     */
    @Test
    public void acceptRequestAlreadyMemberTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User applicant = insertTestingMember("applicant", otherFamily);
        FamilyMembership request = insertTestingInconsistentMembership(family, applicant, "PENDING");

        boolean result = membershipService.acceptRequest(admin, request);

        assertFalse("acceptRequestAlreadyMemberTest: should reject an existing member",
                result);
        var actualFamily = applicant.getFamily();
        assertEquals("acceptRequestAlreadyMemberTest: should preserve the original family",
                otherFamily, actualFamily);
        assertEquals("acceptRequestAlreadyMemberTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that administrators cannot accept another family's requests.
     */
    @Test
    public void acceptRequestDifferentFamilyTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request =
                insertTestingPendingRequest(otherFamily, applicant);

        boolean result = membershipService.acceptRequest(admin, request);

        assertFalse("acceptRequestDifferentFamilyTest: should reject another family's request",
                result);
        assertEquals("acceptRequestDifferentFamilyTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
        var actualFamily = applicant.getFamily();
        assertNull("acceptRequestDifferentFamilyTest: should not assign a family",
                actualFamily);
    }

    /*
     * = = = = = = = = = = =
     * Reject Request Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that rejection changes only the selected request.
     */
    @Test
    public void rejectRequestSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User applicant = insertTestingUser("applicant");

        FamilyMembership request = insertTestingPendingRequest(family, applicant);
        FamilyMembership otherRequest =
                insertTestingPendingRequest(otherFamily, applicant);

        boolean result = membershipService.rejectRequest(admin, request);

        assertTrue("rejectRequestSuccessTest: should reject the request", result);
        assertEquals("rejectRequestSuccessTest: should save the rejected status",
                "REJECTED", savedMembership(request).getStatus());
        assertEquals("rejectRequestSuccessTest: should preserve other pending requests",
                "PENDING", savedMembership(otherRequest).getStatus());
        var actualFamily = applicant.getFamily();
        assertNull("rejectRequestSuccessTest: should not assign a family",
                actualFamily);
    }

    /**
     * Verifies that a null administrator is rejected.
     */
    @Test
    public void rejectRequestNullUserTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.rejectRequest(null, request);

        assertFalse("rejectRequestNullUserTest: should reject a null user", result);
        assertEquals("rejectRequestNullUserTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that a null request is rejected.
     */
    @Test
    public void rejectRequestNullRequestTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);

        boolean result = membershipService.rejectRequest(admin, null);

        assertFalse("rejectRequestNullRequestTest: should reject a null request",
                result);
    }

    /**
     * Verifies that accepted memberships cannot be rejected as pending requests.
     */
    @Test
    public void rejectRequestNotPendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership membership = findTestingAcceptedMembership(family, member);

        boolean result = membershipService.rejectRequest(admin, membership);

        assertFalse("rejectRequestNotPendingTest: should reject a nonpending request",
                result);
        assertEquals("rejectRequestNotPendingTest: should preserve accepted status",
                "ACCEPTED", savedMembership(membership).getStatus());
        var actualFamily = member.getFamily();
        assertEquals("rejectRequestNotPendingTest: should preserve the member's family",
                family, actualFamily);
    }

    /**
     * Verifies that a user without a family cannot reject requests.
     */
    @Test
    public void rejectRequestNoFamilyTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User outsider = insertTestingUser("outsider");
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.rejectRequest(outsider, request);

        assertFalse("rejectRequestNoFamilyTest: should reject a user without a family",
                result);
        assertEquals("rejectRequestNoFamilyTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that regular members cannot reject requests.
     */
    @Test
    public void rejectRequestNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.rejectRequest(member, request);

        assertFalse("rejectRequestNotAdminTest: should reject a regular member", result);
        assertEquals("rejectRequestNotAdminTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /**
     * Verifies that administrators cannot reject another family's requests.
     */
    @Test
    public void rejectRequestDifferentFamilyTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        FamilyMembership request = insertTestingPendingRequest(otherFamily, insertTestingUser("applicant"));

        boolean result = membershipService.rejectRequest(admin, request);

        assertFalse("rejectRequestDifferentFamilyTest: should reject another family's request",
                result);
        assertEquals("rejectRequestDifferentFamilyTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
    }

    /*
     * = = = = = = = = = = =
     * Membership Lookup Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that an existing membership can be retrieved by ID.
     */
    @Test
    public void getMembershipByIDSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = findTestingAcceptedMembership(family, admin);

        FamilyMembership result =
                membershipService.getMembershipByID(membership.getMembershipID());

        assertNotNull("getMembershipByIDSuccessTest: should find the membership", result);
        var actualMembershipID = result.getMembershipID();
        assertEquals("getMembershipByIDSuccessTest: should return the matching membership",
                membership.getMembershipID(), actualMembershipID);
    }

    /**
     * Verifies that a null membership ID returns null.
     */
    @Test
    public void getMembershipByIDNullIDTest() {
        var actualMembershipByID = membershipService.getMembershipByID(null);
        assertNull("getMembershipByIDNullIDTest: should return null",
                actualMembershipByID);
    }

    /**
     * Verifies that a nonexistent membership ID returns null.
     */
    @Test
    public void getMembershipByIDNonexistentIDTest() {
        assertNull("getMembershipByIDNonexistentIDTest: should return null",
                membershipService.getMembershipByID(-1L));
    }

    /**
     * Verifies that the user associated with a membership can be retrieved.
     */
    @Test
    public void getUserByMembershipIDSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = findTestingAcceptedMembership(family, admin);

        User result = membershipService.getUserByMembershipID(
                membership.getMembershipID());

        assertEquals("getUserByMembershipIDSuccessTest: should return the matching user",
                admin, result);
    }

    /**
     * Verifies that a null membership ID returns no user.
     */
    @Test
    public void getUserByMembershipIDNullIDTest() {
        var actualUserByMembershipID = membershipService.getUserByMembershipID(null);
        assertNull("getUserByMembershipIDNullIDTest: should return null",
                actualUserByMembershipID);
    }

    /**
     * Verifies that a nonexistent membership ID returns no user.
     */
    @Test
    public void getUserByMembershipIDNonexistentIDTest() {
        assertNull("getUserByMembershipIDNonexistentIDTest: should return null",
                membershipService.getUserByMembershipID(-1L));
    }

    /*
     * = = = = = = = = = = =
     * Remove Family Member Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that an administrator can remove an accepted member.
     */
    @Test
    public void removeFamilyMemberSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership membership = findTestingAcceptedMembership(family, member);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertTrue("removeFamilyMemberSuccessTest: should remove the member", result);
        assertNull("removeFamilyMemberSuccessTest: should delete the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
        assertNull("removeFamilyMemberSuccessTest: should clear the user's family",
                userService.validateUser("member", password).getFamily());
        assertEquals("removeFamilyMemberSuccessTest: should preserve the family",
                family, membershipService.getFamilyForUser(admin));
    }

    /**
     * Verifies that a null user cannot remove a member.
     */
    @Test
    public void removeFamilyMemberNullUserTest() {
        assertFalse("removeFamilyMemberNullUserTest: should reject a null user",
                membershipService.removeFamilyMember(null, -1L));
    }

    /**
     * Verifies that a null membership ID is rejected.
     */
    @Test
    public void removeFamilyMemberNullIDTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);

        assertFalse("removeFamilyMemberNullIDTest: should reject a null ID",
                membershipService.removeFamilyMember(admin, null));
    }

    /**
     * Verifies that a nonexistent membership cannot be removed.
     */
    @Test
    public void removeFamilyMemberNonexistentIDTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);

        assertFalse("removeFamilyMemberNonexistentIDTest: should reject a nonexistent ID",
                membershipService.removeFamilyMember(admin, -1L));
    }

    /**
     * Verifies that regular members cannot remove other members.
     */
    @Test
    public void removeFamilyMemberNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        User otherMember = insertTestingMember("othermember", family);
        FamilyMembership membership =
                findTestingAcceptedMembership(family, otherMember);

        boolean result = membershipService.removeFamilyMember(
                member, membership.getMembershipID());

        assertFalse("removeFamilyMemberNotAdminTest: should reject a regular member",
                result);
        assertNotNull("removeFamilyMemberNotAdminTest: should preserve the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
    }

    /**
     * Verifies that administrators cannot remove themselves.
     */
    @Test
    public void removeFamilyMemberSelfTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = findTestingAcceptedMembership(family, admin);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertFalse("removeFamilyMemberSelfTest: should reject administrator removal",
                result);
        assertNotNull("removeFamilyMemberSelfTest: should preserve the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
        var actualFamily = admin.getFamily();
        assertEquals("removeFamilyMemberSelfTest: should preserve the family reference",
                family, actualFamily);
    }

    /**
     * Verifies that pending requests cannot be removed as accepted memberships.
     */
    @Test
    public void removeFamilyMemberPendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingPendingRequest(family, insertTestingUser("applicant"));

        boolean result = membershipService.removeFamilyMember(
                admin, request.getMembershipID());

        assertFalse("removeFamilyMemberPendingTest: should reject a pending membership",
                result);
        assertNotNull("removeFamilyMemberPendingTest: should preserve the request",
                membershipService.getMembershipByID(request.getMembershipID()));
    }

    /**
     * Verifies that administrators cannot remove another family's members.
     */
    @Test
    public void removeFamilyMemberDifferentFamilyTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);
        User member = insertTestingMember("member", otherFamily);
        FamilyMembership membership =
                findTestingAcceptedMembership(otherFamily, member);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertFalse("removeFamilyMemberDifferentFamilyTest: should reject another family's member",
                result);
        assertNotNull("removeFamilyMemberDifferentFamilyTest: should preserve the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
        var actualFamily = member.getFamily();
        assertEquals("removeFamilyMemberDifferentFamilyTest: should preserve the family reference",
                otherFamily, actualFamily);
    }

    /*
     * = = = = = = = = = = =
     * Get Family Members Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that administrators and regular members see only accepted
     * memberships from their own family.
     */
    @Test
    public void getFamilyMembersSuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);

        insertTestingPendingRequest(family, insertTestingUser("applicant"));
        insertTestingRejectedRequest(family, insertTestingUser("rejecteduser"));

        User otherAdmin = insertTestingUser("otheradmin");
        insertTestingFamily(otherAdmin);

        List<FamilyMembership> adminResult = membershipService.getFamilyMembers(admin);
        List<FamilyMembership> memberResult = membershipService.getFamilyMembers(member);

        var resultSize = adminResult.size();
        assertEquals("getFamilyMembersSuccessTest: admin should see two accepted members",
                2, resultSize);
        var resultSize2 = memberResult.size();
        assertEquals("getFamilyMembersSuccessTest: member should see two accepted members",
                2, resultSize2);
        assertTrue("getFamilyMembersSuccessTest: admin should see both accepted memberships",
                adminResult.stream().map(FamilyMembership::getUser).toList().containsAll(List.of(admin, member)));
        assertTrue("getFamilyMembersSuccessTest: member should see both accepted memberships",
                memberResult.stream().map(FamilyMembership::getUser).toList().containsAll(List.of(admin, member)));
        assertTrue("getFamilyMembersSuccessTest: admin's results should all be accepted members of this family",
                adminResult.stream().allMatch(membership -> family.equals(membership.getFamily())
                        && "ACCEPTED".equals(membership.getStatus())));
        assertTrue("getFamilyMembersSuccessTest: member's results should all be accepted members of this family",
                memberResult.stream().allMatch(membership -> family.equals(membership.getFamily())
                        && "ACCEPTED".equals(membership.getStatus())));
    }

    /**
     * Verifies that a null user receives an empty list.
     */
    @Test
    public void getFamilyMembersNullUserTest() {
        List<FamilyMembership> result = membershipService.getFamilyMembers(null);

        assertNotNull("getFamilyMembersNullUserTest: should return a list", result);
        var emptyResult = result.isEmpty();
        assertTrue("getFamilyMembersNullUserTest: should return an empty list",
                emptyResult);
    }

    /**
     * Verifies that a user without a family receives an empty list.
     */
    @Test
    public void getFamilyMembersNoFamilyTest() {
        User user = insertTestingUser();

        List<FamilyMembership> result = membershipService.getFamilyMembers(user);

        var emptyResult = result.isEmpty();
        assertTrue("getFamilyMembersNoFamilyTest: should return an empty list",
                emptyResult);
    }

    /*
     * = = = = = = = = = = =
     * Leave Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a regular member can leave without affecting the administrator.
     */
    @Test
    public void leaveFamilySuccessTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership membership = findTestingAcceptedMembership(family, member);

        boolean result = membershipService.leaveFamily(member);

        assertTrue("leaveFamilySuccessTest: should leave the family", result);
        assertNull("leaveFamilySuccessTest: should delete the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
        assertNull("leaveFamilySuccessTest: should clear the user's family",
                userService.validateUser("member", password).getFamily());
        var actualFamily = admin.getFamily();
        assertEquals("leaveFamilySuccessTest: should preserve the administrator's family",
                family, actualFamily);
        assertEquals("leaveFamilySuccessTest: should preserve the family record",
                family, membershipService.getFamilyForUser(admin));
    }

    /**
     * Verifies that a null user cannot leave a family.
     */
    @Test
    public void leaveFamilyNullUserTest() {
        assertFalse("leaveFamilyNullUserTest: should reject a null user",
                membershipService.leaveFamily(null));
    }

    /**
     * Verifies that a user without an accepted membership cannot leave.
     */
    @Test
    public void leaveFamilyNoAcceptedMembershipTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request = insertTestingPendingRequest(family, applicant);

        boolean result = membershipService.leaveFamily(applicant);

        assertFalse("leaveFamilyNoAcceptedMembershipTest: should reject a pending applicant",
                result);
        assertNotNull("leaveFamilyNoAcceptedMembershipTest: should preserve the pending request",
                membershipService.getMembershipByID(request.getMembershipID()));
    }

    /**
     * Verifies that administrators cannot leave while administering the family.
     */
    @Test
    public void leaveFamilyAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = findTestingAcceptedMembership(family, admin);

        boolean result = membershipService.leaveFamily(admin);

        assertFalse("leaveFamilyAdminTest: should reject the administrator", result);
        assertNotNull("leaveFamilyAdminTest: should preserve the membership",
                membershipService.getMembershipByID(membership.getMembershipID()));
        var actualFamily = admin.getFamily();
        assertEquals("leaveFamilyAdminTest: should preserve the family reference",
                family, actualFamily);
    }

    /*
     * = = = = = = = = = = =
     * Test Helpers
     * = = = = = = = = = = =
     */

    private User insertTestingUser() {
        return insertTestingUser(username);
    }

    /**
     * Creates a test user and verifies that the saved credentials work.
     */
    private User insertTestingUser(String username) {
        User user = userService.validateUser(username, password);
        assertNull("insertTestingUser: user should not exist yet", user);

        user = userService.newUser(username, password);
        assertNotNull("insertTestingUser: user should exist after creation", user);
        assertNotNull("insertTestingUser: user should have an ID", user.getId());
        assertEquals("insertTestingUser: user should have the correct username", username, user.getUsername());

        User userCheck = userService.validateUser(username, password);
        assertNotNull("insertTestingUser: user should exist after validation", userCheck);
        assertEquals("insertTestingUser: login should return the created user's ID", user.getId(), userCheck.getId());
        assertEquals("insertTestingUser: login should return the expected username", username, userCheck.getUsername());
        return userCheck;
    }

    /**
     * Creates a family and checks its administrator and initial membership.
     */
    private Family insertTestingFamily(User admin) {
        final String familyName = "Family of " + admin.getUsername();
        assertNull("insertTestingFamily: the administrator should start without a family",
                membershipService.getFamilyForUser(admin));

        boolean result = familyService.createFamily(admin, familyName);
        assertTrue("insertTestingFamily: should create the family", result);

        Family family = membershipService.getFamilyForUser(admin);
        assertNotNull("insertTestingFamily: should find the created family", family);
        assertNotNull("insertTestingFamily: family should have an ID", family.getFamilyID());
        assertEquals("insertTestingFamily: should store the expected name", familyName, family.getFamilyName());
        assertEquals("insertTestingFamily: should assign the administrator", admin, family.getFamilyAdmin());
        List<FamilyMembership> memberships = membershipService.getFamilyMembers(admin);
        assertEquals("insertTestingFamily: should create exactly one membership", 1, memberships.size());
        assertEquals("insertTestingFamily: should accept the administrator", "ACCEPTED", memberships.getFirst().getStatus());
        assertEquals("insertTestingFamily: membership should belong to the administrator", admin, memberships.getFirst().getUser());
        return family;
    }

    /**
     * Creates a member through the request and acceptance workflow.
     */
    private User insertTestingMember(String username, Family family) {
        User member = insertTestingUser(username);
        FamilyMembership request = insertTestingPendingRequest(family, member);

        boolean result = membershipService.acceptRequest(family.getFamilyAdmin(), request);
        assertTrue("insertTestingMember: should accept the member", result);

        FamilyMembership membership = membershipService.getMembershipByID(request.getMembershipID());
        assertNotNull("insertTestingMember: should find the accepted membership", membership);
        assertEquals("insertTestingMember: should save accepted status", "ACCEPTED", membership.getStatus());
        assertEquals("insertTestingMember: should associate the member with the family",
                family, membershipService.getFamilyForUser(member));
        return member;
    }

    /**
     * Submits a join request and verifies its persisted status and associations.
     */
    private FamilyMembership insertTestingPendingRequest(Family family, User applicant) {
        User admin = family.getFamilyAdmin();
        assertTrue("insertTestingPendingRequest: applicant should have no pending request for this family",
                membershipService.getPendingRequests(admin).stream()
                        .noneMatch(request -> applicant.equals(request.getUser())));

        boolean result = membershipService.requestToJoinFamily(applicant, family.getFamilyID());
        assertTrue("insertTestingPendingRequest: should submit the request", result);

        return findTestingPendingRequest(family, applicant);
    }

    /**
     * Checks that the family has exactly one pending request for the applicant.
     */
    private FamilyMembership findTestingPendingRequest(Family family, User applicant) {
        List<FamilyMembership> requests = membershipService.getPendingRequests(family.getFamilyAdmin()).stream()
                .filter(request -> applicant.equals(request.getUser()))
                .toList();
        assertEquals("findTestingPendingRequest: should find one request for the applicant", 1, requests.size());
        FamilyMembership request = requests.getFirst();
        assertNotNull("findTestingPendingRequest: request should have an ID", request.getMembershipID());
        assertEquals("findTestingPendingRequest: should return the expected family", family, request.getFamily());
        assertEquals("findTestingPendingRequest: should return pending status", "PENDING", request.getStatus());
        return request;
    }

    /**
     * Submits and rejects a request, then checks its status and family assignment.
     */
    private FamilyMembership insertTestingRejectedRequest(Family family, User applicant) {
        FamilyMembership request = insertTestingPendingRequest(family, applicant);

        boolean result = membershipService.rejectRequest(family.getFamilyAdmin(), request);
        assertTrue("insertTestingRejectedRequest: should reject the request", result);

        FamilyMembership rejected = savedMembership(request);
        assertEquals("insertTestingRejectedRequest: should save rejected status", "REJECTED", rejected.getStatus());
        assertNull("insertTestingRejectedRequest: applicant should have no family", applicant.getFamily());
        return rejected;
    }

    /**
     * Persists a membership that the public service workflow would reject.
     */
    private FamilyMembership insertTestingInconsistentMembership(Family family, User user, String status) {
        FamilyMembership membership = new FamilyMembership(family, user, status);
        entityManager.persist(membership);
        entityManager.flush();

        assertNotNull("insertTestingInconsistentMembership: membership should have an ID", membership.getMembershipID());
        FamilyMembership saved = savedMembership(membership);
        assertEquals("insertTestingInconsistentMembership: should save the expected family", family, saved.getFamily());
        assertEquals("insertTestingInconsistentMembership: should save the expected user", user, saved.getUser());
        assertEquals("insertTestingInconsistentMembership: should save the expected status", status, saved.getStatus());
        return saved;
    }

    /**
     * Checks that an accepted user has exactly one membership in the specified family.
     */
    private FamilyMembership findTestingAcceptedMembership(Family family, User user) {
        List<FamilyMembership> memberships = membershipService.getFamilyMembers(family.getFamilyAdmin()).stream()
                .filter(membership -> user.equals(membership.getUser()))
                .toList();
        assertEquals("findTestingAcceptedMembership: should find one membership for the user", 1, memberships.size());
        FamilyMembership membership = memberships.getFirst();
        assertNotNull("findTestingAcceptedMembership: membership should have an ID", membership.getMembershipID());
        assertEquals("findTestingAcceptedMembership: should return the expected family", family, membership.getFamily());
        assertEquals("findTestingAcceptedMembership: should return accepted status", "ACCEPTED", membership.getStatus());
        return membership;
    }

    /**
     * Retrieves an existing membership by its generated ID.
     */
    private FamilyMembership savedMembership(FamilyMembership membership) {
        FamilyMembership saved = membershipService.getMembershipByID(membership.getMembershipID());
        assertNotNull("savedMembership: should find the membership", saved);
        assertEquals("savedMembership: should return the same membership ID",
                membership.getMembershipID(), saved.getMembershipID());
        return saved;
    }

    /**
     * Counts all of a user's memberships, regardless of status.
     */
    private long countMemberships(User user) {
        return entityManager.createQuery("""
                        select count(membership) from FamilyMembership membership
                        where membership.user = :user
                        """, Long.class)
                .setParameter("user", user)
                .getSingleResult();
    }
}
