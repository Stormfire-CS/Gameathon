package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.FamiliesRepository;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

/**
 * Tests family membership requests, permissions, retrieval,
 * member removal, and leaving a family.
 *
 * <p>ChatGPT assisted with generating these tests.
 * Review and adapt them before use.</p>
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

    @Autowired
    private FamiliesRepository familiesRepo;

    @Autowired
    private FamilyMembershipRepository membershipRepo;

    @Autowired
    private UserRepository userRepo;

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

        insertTestingMembership(family, applicant, "PENDING");
        insertTestingMembership(family, rejectedUser, "REJECTED");

        assertNull("getFamilyForUserNoAcceptedMembershipTest: pending user should have no family",
                membershipService.getFamilyForUser(applicant));
        assertNull("getFamilyForUserNoAcceptedMembershipTest: rejected user should have no family",
                membershipService.getFamilyForUser(rejectedUser));
    }

    /**
     * Verifies that an accepted membership requires a matching user family reference.
     */
    @Test
    public void getFamilyForUserNullFamilyReferenceTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingUser("member");
        insertTestingMembership(family, member, "ACCEPTED");

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

        insertTestingMembership(family, member, "ACCEPTED");
        member.setFamily(otherFamily);
        userRepo.save(member);

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

        insertTestingMembership(otherFamily, member, "ACCEPTED");

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

        FamilyMembership request = membershipRepo.findByFamilyAndUser(family, applicant);

        assertNotNull("requestToJoinFamilySuccessTest: should save the request", request);
        assertEquals("requestToJoinFamilySuccessTest: should mark the request pending",
                "PENDING", request.getStatus());
        assertNull("requestToJoinFamilySuccessTest: should not assign a family yet",
                applicant.getFamily());
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
        assertTrue("requestToJoinFamilyNonexistentFamilyTest: should not save a request",
                membershipRepo.findByUserAndStatus(user, "PENDING").isEmpty());
    }

    /**
     * Verifies that an accepted member cannot request another family.
     */
    @Test
    public void requestToJoinFamilyAlreadyMemberTest() {
        User admin = insertTestingUser();
        insertTestingFamily(admin);
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);

        boolean result = membershipService.requestToJoinFamily(
                admin, otherFamily.getFamilyID());

        assertFalse("requestToJoinFamilyAlreadyMemberTest: should reject an existing member",
                result);
        assertNull("requestToJoinFamilyAlreadyMemberTest: should not create a request",
                membershipRepo.findByFamilyAndUser(otherFamily, admin));
    }

    /**
     * Verifies that a pending request cannot be duplicated.
     */
    @Test
    public void requestToJoinFamilyDuplicatePendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request = insertTestingMembership(family, applicant, "PENDING");
        long previousCount = membershipRepo.count();

        boolean result = membershipService.requestToJoinFamily(
                applicant, family.getFamilyID());

        assertFalse("requestToJoinFamilyDuplicatePendingTest: should reject a duplicate",
                result);
        assertEquals("requestToJoinFamilyDuplicatePendingTest: should preserve the record count",
                previousCount, membershipRepo.count());
        assertEquals("requestToJoinFamilyDuplicatePendingTest: should keep the request pending",
                "PENDING", request.getStatus());
    }

    /**
     * Verifies that rejected requests are reopened using the existing record.
     */
    @Test
    public void requestToJoinFamilyRejectedRequestTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User applicant = insertTestingUser("applicant");
        FamilyMembership request = insertTestingMembership(family, applicant, "REJECTED");
        Long originalID = request.getMembershipID();
        long previousCount = membershipRepo.count();

        boolean result = membershipService.requestToJoinFamily(
                applicant, family.getFamilyID());

        assertTrue("requestToJoinFamilyRejectedRequestTest: should reopen the request",
                result);

        FamilyMembership reopened = membershipRepo.findByFamilyAndUser(family, applicant);

        assertEquals("requestToJoinFamilyRejectedRequestTest: should reuse the record",
                originalID, reopened.getMembershipID());
        assertEquals("requestToJoinFamilyRejectedRequestTest: should mark it pending",
                "PENDING", reopened.getStatus());
        assertEquals("requestToJoinFamilyRejectedRequestTest: should preserve the record count",
                previousCount, membershipRepo.count());
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
        User otherAdmin = insertTestingUser("otheradmin");
        Family otherFamily = insertTestingFamily(otherAdmin);

        FamilyMembership pending = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");
        insertTestingMember("member", family);
        insertTestingMembership(family, insertTestingUser("rejecteduser"), "REJECTED");
        insertTestingMembership(otherFamily, insertTestingUser("otherapplicant"), "PENDING");

        List<FamilyMembership> result = membershipService.getPendingRequests(admin);

        assertEquals("getPendingRequestsSuccessTest: should return one request",
                1, result.size());
        assertEquals("getPendingRequestsSuccessTest: should return the matching request",
                pending.getMembershipID(), result.getFirst().getMembershipID());
    }

    /**
     * Verifies that a null user receives an empty list.
     */
    @Test
    public void getPendingRequestsNullUserTest() {
        List<FamilyMembership> result = membershipService.getPendingRequests(null);

        assertNotNull("getPendingRequestsNullUserTest: should return a list", result);
        assertTrue("getPendingRequestsNullUserTest: should return an empty list",
                result.isEmpty());
    }

    /**
     * Verifies that a regular member cannot retrieve pending requests.
     */
    @Test
    public void getPendingRequestsNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        insertTestingMembership(family, insertTestingUser("applicant"), "PENDING");

        List<FamilyMembership> result = membershipService.getPendingRequests(member);

        assertTrue("getPendingRequestsNotAdminTest: should return an empty list",
                result.isEmpty());
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

        FamilyMembership request = insertTestingMembership(family, applicant, "PENDING");
        FamilyMembership otherRequest =
                insertTestingMembership(otherFamily, applicant, "PENDING");

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
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

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
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "REJECTED");

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
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

        boolean result = membershipService.acceptRequest(outsider, request);

        assertFalse("acceptRequestNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that regular members cannot accept requests.
     */
    @Test
    public void acceptRequestNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

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
        FamilyMembership request = insertTestingMembership(family, applicant, "PENDING");

        boolean result = membershipService.acceptRequest(admin, request);

        assertFalse("acceptRequestAlreadyMemberTest: should reject an existing member",
                result);
        assertEquals("acceptRequestAlreadyMemberTest: should preserve the original family",
                otherFamily, applicant.getFamily());
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
                insertTestingMembership(otherFamily, applicant, "PENDING");

        boolean result = membershipService.acceptRequest(admin, request);

        assertFalse("acceptRequestDifferentFamilyTest: should reject another family's request",
                result);
        assertEquals("acceptRequestDifferentFamilyTest: should preserve the pending status",
                "PENDING", savedMembership(request).getStatus());
        assertNull("acceptRequestDifferentFamilyTest: should not assign a family",
                applicant.getFamily());
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

        FamilyMembership request = insertTestingMembership(family, applicant, "PENDING");
        FamilyMembership otherRequest =
                insertTestingMembership(otherFamily, applicant, "PENDING");

        boolean result = membershipService.rejectRequest(admin, request);

        assertTrue("rejectRequestSuccessTest: should reject the request", result);
        assertEquals("rejectRequestSuccessTest: should save the rejected status",
                "REJECTED", savedMembership(request).getStatus());
        assertEquals("rejectRequestSuccessTest: should preserve other pending requests",
                "PENDING", savedMembership(otherRequest).getStatus());
        assertNull("rejectRequestSuccessTest: should not assign a family",
                applicant.getFamily());
    }

    /**
     * Verifies that a null administrator is rejected.
     */
    @Test
    public void rejectRequestNullUserTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

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
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, member);

        boolean result = membershipService.rejectRequest(admin, membership);

        assertFalse("rejectRequestNotPendingTest: should reject a nonpending request",
                result);
        assertEquals("rejectRequestNotPendingTest: should preserve accepted status",
                "ACCEPTED", savedMembership(membership).getStatus());
        assertEquals("rejectRequestNotPendingTest: should preserve the member's family",
                family, member.getFamily());
    }

    /**
     * Verifies that a user without a family cannot reject requests.
     */
    @Test
    public void rejectRequestNoFamilyTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User outsider = insertTestingUser("outsider");
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

        boolean result = membershipService.rejectRequest(outsider, request);

        assertFalse("rejectRequestNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that regular members cannot reject requests.
     */
    @Test
    public void rejectRequestNotAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("member", family);
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

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
        FamilyMembership request = insertTestingMembership(
                otherFamily, insertTestingUser("applicant"), "PENDING");

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
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, admin);

        FamilyMembership result =
                membershipService.getMembershipByID(membership.getMembershipID());

        assertNotNull("getMembershipByIDSuccessTest: should find the membership", result);
        assertEquals("getMembershipByIDSuccessTest: should return the matching membership",
                membership.getMembershipID(), result.getMembershipID());
    }

    /**
     * Verifies that a null membership ID returns null.
     */
    @Test
    public void getMembershipByIDNullIDTest() {
        assertNull("getMembershipByIDNullIDTest: should return null",
                membershipService.getMembershipByID(null));
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
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, admin);

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
        assertNull("getUserByMembershipIDNullIDTest: should return null",
                membershipService.getUserByMembershipID(null));
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
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, member);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertTrue("removeFamilyMemberSuccessTest: should remove the member", result);
        assertFalse("removeFamilyMemberSuccessTest: should delete the membership",
                membershipRepo.existsById(membership.getMembershipID()));
        assertNull("removeFamilyMemberSuccessTest: should clear the user's family",
                userService.validateUser("member", password).getFamily());
        assertTrue("removeFamilyMemberSuccessTest: should preserve the family",
                familiesRepo.existsById(family.getFamilyID()));
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
                membershipRepo.findByFamilyAndUser(family, otherMember);

        boolean result = membershipService.removeFamilyMember(
                member, membership.getMembershipID());

        assertFalse("removeFamilyMemberNotAdminTest: should reject a regular member",
                result);
        assertTrue("removeFamilyMemberNotAdminTest: should preserve the membership",
                membershipRepo.existsById(membership.getMembershipID()));
    }

    /**
     * Verifies that administrators cannot remove themselves.
     */
    @Test
    public void removeFamilyMemberSelfTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, admin);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertFalse("removeFamilyMemberSelfTest: should reject administrator removal",
                result);
        assertTrue("removeFamilyMemberSelfTest: should preserve the membership",
                membershipRepo.existsById(membership.getMembershipID()));
        assertEquals("removeFamilyMemberSelfTest: should preserve the family reference",
                family, admin.getFamily());
    }

    /**
     * Verifies that pending requests cannot be removed as accepted memberships.
     */
    @Test
    public void removeFamilyMemberPendingTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership request = insertTestingMembership(
                family, insertTestingUser("applicant"), "PENDING");

        boolean result = membershipService.removeFamilyMember(
                admin, request.getMembershipID());

        assertFalse("removeFamilyMemberPendingTest: should reject a pending membership",
                result);
        assertTrue("removeFamilyMemberPendingTest: should preserve the request",
                membershipRepo.existsById(request.getMembershipID()));
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
                membershipRepo.findByFamilyAndUser(otherFamily, member);

        boolean result = membershipService.removeFamilyMember(
                admin, membership.getMembershipID());

        assertFalse("removeFamilyMemberDifferentFamilyTest: should reject another family's member",
                result);
        assertTrue("removeFamilyMemberDifferentFamilyTest: should preserve the membership",
                membershipRepo.existsById(membership.getMembershipID()));
        assertEquals("removeFamilyMemberDifferentFamilyTest: should preserve the family reference",
                otherFamily, member.getFamily());
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

        insertTestingMembership(family, insertTestingUser("applicant"), "PENDING");
        insertTestingMembership(family, insertTestingUser("rejecteduser"), "REJECTED");

        User otherAdmin = insertTestingUser("otheradmin");
        insertTestingFamily(otherAdmin);

        FamilyMembership adminMembership =
                membershipRepo.findByFamilyAndUser(family, admin);
        FamilyMembership memberMembership =
                membershipRepo.findByFamilyAndUser(family, member);

        List<FamilyMembership> adminResult = membershipService.getFamilyMembers(admin);
        List<FamilyMembership> memberResult = membershipService.getFamilyMembers(member);

        assertEquals("getFamilyMembersSuccessTest: admin should see two accepted members",
                2, adminResult.size());
        assertEquals("getFamilyMembersSuccessTest: member should see two accepted members",
                2, memberResult.size());
        assertTrue("getFamilyMembersSuccessTest: admin should see both accepted memberships",
                adminResult.contains(adminMembership) && adminResult.contains(memberMembership));
        assertTrue("getFamilyMembersSuccessTest: member should see both accepted memberships",
                memberResult.contains(adminMembership) && memberResult.contains(memberMembership));
    }

    /**
     * Verifies that a null user receives an empty list.
     */
    @Test
    public void getFamilyMembersNullUserTest() {
        List<FamilyMembership> result = membershipService.getFamilyMembers(null);

        assertNotNull("getFamilyMembersNullUserTest: should return a list", result);
        assertTrue("getFamilyMembersNullUserTest: should return an empty list",
                result.isEmpty());
    }

    /**
     * Verifies that a user without a family receives an empty list.
     */
    @Test
    public void getFamilyMembersNoFamilyTest() {
        User user = insertTestingUser();

        List<FamilyMembership> result = membershipService.getFamilyMembers(user);

        assertTrue("getFamilyMembersNoFamilyTest: should return an empty list",
                result.isEmpty());
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
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, member);

        boolean result = membershipService.leaveFamily(member);

        assertTrue("leaveFamilySuccessTest: should leave the family", result);
        assertFalse("leaveFamilySuccessTest: should delete the membership",
                membershipRepo.existsById(membership.getMembershipID()));
        assertNull("leaveFamilySuccessTest: should clear the user's family",
                userService.validateUser("member", password).getFamily());
        assertEquals("leaveFamilySuccessTest: should preserve the administrator's family",
                family, admin.getFamily());
        assertTrue("leaveFamilySuccessTest: should preserve the family record",
                familiesRepo.existsById(family.getFamilyID()));
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
        FamilyMembership request = insertTestingMembership(family, applicant, "PENDING");

        boolean result = membershipService.leaveFamily(applicant);

        assertFalse("leaveFamilyNoAcceptedMembershipTest: should reject a pending applicant",
                result);
        assertTrue("leaveFamilyNoAcceptedMembershipTest: should preserve the pending request",
                membershipRepo.existsById(request.getMembershipID()));
    }

    /**
     * Verifies that administrators cannot leave while administering the family.
     */
    @Test
    public void leaveFamilyAdminTest() {
        User admin = insertTestingUser();
        Family family = insertTestingFamily(admin);
        FamilyMembership membership = membershipRepo.findByFamilyAndUser(family, admin);

        boolean result = membershipService.leaveFamily(admin);

        assertFalse("leaveFamilyAdminTest: should reject the administrator", result);
        assertTrue("leaveFamilyAdminTest: should preserve the membership",
                membershipRepo.existsById(membership.getMembershipID()));
        assertEquals("leaveFamilyAdminTest: should preserve the family reference",
                family, admin.getFamily());
    }

    /*
     * = = = = = = = = = = =
     * Test Helpers
     * = = = = = = = = = = =
     */

    private User insertTestingUser() {
        return insertTestingUser(username);
    }

    private User insertTestingUser(String testUsername) {
        userService.newUser(testUsername, password, password);
        User user = userService.validateUser(testUsername, password);

        assertNotNull("insertTestingUser: should create the test user", user);
        return user;
    }

    /**
     * Creates a family with an administrator and accepted membership.
     */
    private Family insertTestingFamily(User admin) {
        boolean result = familyService.createFamily(
                admin, "Family of " + admin.getUsername());

        assertTrue("insertTestingFamily: should create the family", result);

        Family family = familiesRepo.findByFamilyAdmin(admin);

        assertNotNull("insertTestingFamily: should find the family", family);
        return family;
    }

    /**
     * Creates an accepted member with a matching user family reference.
     */
    private User insertTestingMember(String memberUsername, Family family) {
        User member = insertTestingUser(memberUsername);
        member.setFamily(family);
        userRepo.save(member);
        insertTestingMembership(family, member, "ACCEPTED");
        return member;
    }

    private FamilyMembership insertTestingMembership(
            Family family, User user, String status) {
        return membershipRepo.save(new FamilyMembership(family, user, status));
    }

    private FamilyMembership savedMembership(FamilyMembership membership) {
        return membershipRepo.findById(membership.getMembershipID()).orElseThrow();
    }
}