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

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Tests the {@link FamilyService} methods responsible for creating, renaming, and deleting families
 * and managing administrator permissions.
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the services use their configured dependencies. The
 * {@link Transactional} annotation rolls back database changes after each
 * test.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with generating Javadoc comments and tests for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author and tests followed authors layout.</li>
 * </ul>
 */
@SpringBootTest
@Transactional
public class FamilyServiceTest {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserService userService;

    @Autowired
    private FamilyMembershipService membershipService;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * = = = = = = = = = = =
     * Create Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that creation assigns the creator as administrator
     * and creates an accepted membership.
     */
    @Test
    public void createFamilySuccessTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);

        boolean result = familyService.createFamily(user, familyName);

        assertTrue("createFamilySuccessTest: should create the family", result);

        Family family = membershipService.getFamilyForUser(user);

        assertNotNull("createFamilySuccessTest: should save the family", family);
        var actualFamilyName = family.getFamilyName();
        assertEquals("createFamilySuccessTest: should save the name",
                familyName, actualFamilyName);
        var actualFamilyAdmin = family.getFamilyAdmin();
        assertEquals("createFamilySuccessTest: should assign the administrator",
                user, actualFamilyAdmin);
        assertEquals("createFamilySuccessTest: should update the user's family",
                family, userService.validateUser(username, password).getFamily());

        List<FamilyMembership> memberships = membershipService.getFamilyMembers(user);
        assertEquals("createFamilySuccessTest: should save only one membership for the family",
                1L, countMemberships(family.getFamilyID()));

        var resultSize = memberships.size();
        assertEquals("createFamilySuccessTest: should create one membership",
                1, resultSize);
        assertEquals("createFamilySuccessTest: should create the creator's membership",
                user, memberships.getFirst().getUser());
        assertEquals("createFamilySuccessTest: should accept the creator",
                "ACCEPTED", memberships.getFirst().getStatus());
    }

    /**
     * Verifies that surrounding spaces are removed from the family name.
     */
    @Test
    public void createFamilyNameWithSpacesTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);

        // No families in the database
        boolean result = familyService.createFamily(user, "Test Family ");
        assertTrue("createFamilyNameWithSpacesTest: should accept a trailing space", result);
        Family family = membershipService.getFamilyForUser(user);
        assertNotNull("createFamilyNameWithSpacesTest: should save the first family", family);
        String savedName = family.getFamilyName();
        assertEquals("createFamilyNameWithSpacesTest: should trim the name",
                familyName, savedName);

        // One family in the database
        User secondAdmin = insertTestingUser("secondadmin", password);
        result = familyService.createFamily(secondAdmin, " Second Family");
        assertTrue("createFamilyNameWithSpacesTest: should accept a leading space", result);
        Family secondFamily = membershipService.getFamilyForUser(secondAdmin);
        assertNotNull("createFamilyNameWithSpacesTest: should save the second family", secondFamily);
        String secondName = secondFamily.getFamilyName();
        assertEquals("createFamilyNameWithSpacesTest: should trim the leading space", "Second Family", secondName);

        // Two families in the database
        User thirdAdmin = insertTestingUser("thirdadmin", password);
        result = familyService.createFamily(thirdAdmin, " Third Family ");
        assertTrue("createFamilyNameWithSpacesTest: should accept spaces on both ends", result);
        Family thirdFamily = membershipService.getFamilyForUser(thirdAdmin);
        assertNotNull("createFamilyNameWithSpacesTest: should save the third family", thirdFamily);
        String thirdName = thirdFamily.getFamilyName();
        assertEquals("createFamilyNameWithSpacesTest: should trim spaces on both ends", "Third Family", thirdName);
    }

    /**
     * Verifies that creation fails when the user is null.
     */
    @Test
    public void createFamilyNullUserTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        // No families in the database
        long previousCount = countFamilies();
        boolean result = familyService.createFamily(null, familyName);
        assertFalse("createFamilyNullUserTest: should reject a null user with no families", result);
        assertEquals("createFamilyNullUserTest: should not create a family",
                previousCount, countFamilies());

        // One family in the database
        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        result = familyService.createFamily(null, "Another Family");
        assertFalse("createFamilyNullUserTest: should reject a null user with an existing family", result);
        assertEquals("createFamilyNullUserTest: should leave the family count unchanged",
                previousCount + 1, countFamilies());
        assertEquals("createFamilyNullUserTest: should preserve the existing family",
                family, membershipService.getFamilyForUser(admin));
    }

    /**
     * Verifies that creation fails when the name is null.
     */
    @Test
    public void createFamilyNullNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);
        long previousCount = countFamilies();

        boolean result = familyService.createFamily(user, null);

        assertFalse("createFamilyNullNameTest: should reject a null name", result);
        assertNull("createFamilyNullNameTest: should not assign a family",
                user.getFamily());
        assertEquals("createFamilyNullNameTest: should not save a family", previousCount, countFamilies());
    }

    /**
     * Verifies that creation fails when the name is empty.
     */
    @Test
    public void createFamilyEmptyNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);
        long previousCount = countFamilies();

        boolean result = familyService.createFamily(user, "");

        assertFalse("createFamilyEmptyNameTest: should reject an empty name", result);
        assertNull("createFamilyEmptyNameTest: should not assign a family",
                user.getFamily());
        assertEquals("createFamilyEmptyNameTest: should not save a family", previousCount, countFamilies());
    }

    /**
     * Verifies that creation fails when the name contains only spaces.
     */
    @Test
    public void createFamilyWhitespaceNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);
        long previousCount = countFamilies();

        boolean result = familyService.createFamily(user, "   ");

        assertFalse("createFamilyWhitespaceNameTest: should reject a blank name",
                result);
        assertNull("createFamilyWhitespaceNameTest: should not assign a family",
                user.getFamily());
        assertEquals("createFamilyWhitespaceNameTest: should not save a family", previousCount, countFamilies());
    }

    /**
     * Verifies that a user already belonging to a family cannot create another.
     */
    @Test
    public void createFamilyExistingFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family originalFamily = insertTestingFamily(user, familyName);
        long previousCount = countFamilies();

        boolean result = familyService.createFamily(user, "Another Family");

        assertFalse("createFamilyExistingFamilyTest: should reject an existing member",
                result);
        var actualFamily = user.getFamily();
        assertEquals("createFamilyExistingFamilyTest: should preserve the user's family",
                originalFamily, actualFamily);
        var savedCount = countFamilies();
        assertEquals("createFamilyExistingFamilyTest: should not create another family",
                previousCount, savedCount);
        var actualFamilyName = originalFamily.getFamilyName();
        assertEquals("createFamilyExistingFamilyTest: should preserve the original name",
                familyName, actualFamilyName);
    }

    /*
     * = = = = = = = = = = =
     * Is Family Admin Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that the family's administrator is recognized.
     */
    @Test
    public void isFamilyAdminSuccessTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        boolean result = familyService.isFamilyAdmin(user, family);

        assertTrue("isFamilyAdminSuccessTest: should recognize the administrator",
                result);
    }

    /**
     * Verifies that membership alone does not grant administrator permissions.
     */
    @Test
    public void isFamilyAdminRegularMemberTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        boolean result = familyService.isFamilyAdmin(member, family);

        assertFalse("isFamilyAdminRegularMemberTest: should reject a regular member",
                result);
    }

    /**
     * Verifies that a null user is not an administrator.
     */
    @Test
    public void isFamilyAdminNullUserTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        boolean result = familyService.isFamilyAdmin(null, family);

        assertFalse("isFamilyAdminNullUserTest: should reject a null user", result);
    }

    /**
     * Verifies that a null family has no administrator.
     */
    @Test
    public void isFamilyAdminNullFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);

        boolean result = familyService.isFamilyAdmin(user, null);

        assertFalse("isFamilyAdminNullFamilyTest: should reject a null family",
                result);
    }

    /*
     * = = = = = = = = = = =
     * Rename Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that the administrator can rename the family independently of other families.
     */
    @Test
    public void renameFamilySuccessTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";
        final String newName = "New Family Name";
        final String secondName = "Second Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        // One family in the database
        boolean result = familyService.renameFamily(user, newName);
        assertTrue("renameFamilySuccessTest: should rename the family", result);
        Family renamedFamily = membershipService.getFamilyForUser(user);
        assertNotNull("renameFamilySuccessTest: should find the renamed family", renamedFamily);
        assertEquals("renameFamilySuccessTest: should keep the same family ID",
                family.getFamilyID(), renamedFamily.getFamilyID());
        assertEquals("renameFamilySuccessTest: should save the new name",
                newName, renamedFamily.getFamilyName());
        assertEquals("renameFamilySuccessTest: should preserve the administrator",
                user, renamedFamily.getFamilyAdmin());

        // Two families in the database
        User secondAdmin = insertTestingUser("secondadmin", password);
        insertTestingFamily(secondAdmin, secondName);
        result = familyService.renameFamily(user, "Updated Family Name");
        assertTrue("renameFamilySuccessTest: should rename the first family again", result);
        assertEquals("renameFamilySuccessTest: should save the first family's updated name",
                "Updated Family Name", membershipService.getFamilyForUser(user).getFamilyName());
        assertEquals("renameFamilySuccessTest: should preserve the second family's name",
                secondName, membershipService.getFamilyForUser(secondAdmin).getFamilyName());
    }

    /**
     * Verifies that surrounding spaces are removed from the new name.
     */
    @Test
    public void renameFamilyNameWithSpacesTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        insertTestingFamily(user, familyName);

        boolean result = familyService.renameFamily(user, "  New Family Name  ");

        assertTrue("renameFamilyNameWithSpacesTest: should accept the name", result);
        assertEquals("renameFamilyNameWithSpacesTest: should trim the new name",
                "New Family Name",
                membershipService.getFamilyForUser(user).getFamilyName());
    }

    /**
     * Verifies that renaming fails when the user is null.
     */
    @Test
    public void renameFamilyNullUserTest() {
        boolean result = familyService.renameFamily(null, "New Family Name");

        assertFalse("renameFamilyNullUserTest: should reject a null user", result);
    }

    /**
     * Verifies that renaming fails when the name is null.
     */
    @Test
    public void renameFamilyNullNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        boolean result = familyService.renameFamily(user, null);

        assertFalse("renameFamilyNullNameTest: should reject a null name", result);
        var actualFamilyName = family.getFamilyName();
        assertEquals("renameFamilyNullNameTest: should preserve the original name",
                familyName, actualFamilyName);
    }

    /**
     * Verifies that renaming fails when the name is empty.
     */
    @Test
    public void renameFamilyEmptyNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        boolean result = familyService.renameFamily(user, "");

        assertFalse("renameFamilyEmptyNameTest: should reject an empty name", result);
        var actualFamilyName = family.getFamilyName();
        assertEquals("renameFamilyEmptyNameTest: should preserve the original name",
                familyName, actualFamilyName);
    }

    /**
     * Verifies that renaming fails when the name contains only spaces.
     */
    @Test
    public void renameFamilyWhitespaceNameTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamily(user, familyName);

        boolean result = familyService.renameFamily(user, "   ");

        assertFalse("renameFamilyWhitespaceNameTest: should reject a blank name",
                result);
        var actualFamilyName = family.getFamilyName();
        assertEquals("renameFamilyWhitespaceNameTest: should preserve the original name",
                familyName, actualFamilyName);
    }

    /**
     * Verifies that a user without a family cannot rename one.
     */
    @Test
    public void renameFamilyNoFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);

        boolean result = familyService.renameFamily(user, "New Family Name");

        assertFalse("renameFamilyNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that a regular member cannot rename the family.
     */
    @Test
    public void renameFamilyNotAdminTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        boolean result = familyService.renameFamily(member, "New Family Name");

        assertFalse("renameFamilyNotAdminTest: should reject a regular member",
                result);
        var actualFamilyName = family.getFamilyName();
        assertEquals("renameFamilyNotAdminTest: should preserve the name",
                familyName, actualFamilyName);
    }

    /*
     * = = = = = = = = = = =
     * Change Admin Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that administrator permissions can be transferred
     * to an accepted member of the same family.
     */
    @Test
    public void changeAdminSuccessTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        boolean result = familyService.changeAdmin(admin, member);

        assertTrue("changeAdminSuccessTest: should change the administrator", result);
        assertEquals("changeAdminSuccessTest: should save the new administrator",
                member,
                membershipService.getFamilyForUser(member).getFamilyAdmin());
        assertTrue("changeAdminSuccessTest: should recognize the new administrator",
                familyService.isFamilyAdmin(member, family));
        assertFalse("changeAdminSuccessTest: should remove the old administrator's permissions",
                familyService.isFamilyAdmin(admin, family));
        var actualFamily = admin.getFamily();
        assertEquals("changeAdminSuccessTest: should preserve the old administrator's membership",
                family, actualFamily);
        assertEquals("changeAdminSuccessTest: should preserve both accepted memberships",
                2, membershipService.getFamilyMembers(member).size());
    }

    /**
     * Verifies that a null current administrator is rejected.
     */
    @Test
    public void changeAdminNullCurrentAdminTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);

        boolean result = familyService.changeAdmin(null, user);

        assertFalse("changeAdminNullCurrentAdminTest: should reject a null current admin",
                result);
    }

    /**
     * Verifies that a null proposed administrator is rejected.
     */
    @Test
    public void changeAdminNullNewAdminTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);

        boolean result = familyService.changeAdmin(admin, null);

        assertFalse("changeAdminNullNewAdminTest: should reject a null new admin",
                result);
        var actualFamilyAdmin = family.getFamilyAdmin();
        assertEquals("changeAdminNullNewAdminTest: should preserve the administrator",
                admin, actualFamilyAdmin);
    }

    /**
     * Verifies that a regular member cannot transfer administrator permissions.
     */
    @Test
    public void changeAdminNotAdminTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        boolean result = familyService.changeAdmin(member, admin);

        assertFalse("changeAdminNotAdminTest: should reject a regular member", result);
        var actualFamilyAdmin = family.getFamilyAdmin();
        assertEquals("changeAdminNotAdminTest: should preserve the administrator",
                admin, actualFamilyAdmin);
    }

    /**
     * Verifies that a user without a family cannot become administrator.
     */
    @Test
    public void changeAdminNewAdminWithoutFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User outsider = insertTestingUser("familyoutsider", password);

        boolean result = familyService.changeAdmin(admin, outsider);

        assertFalse("changeAdminNewAdminWithoutFamilyTest: should reject an outsider",
                result);
        var actualFamilyAdmin = family.getFamilyAdmin();
        assertEquals("changeAdminNewAdminWithoutFamilyTest: should preserve the administrator",
                admin, actualFamilyAdmin);
    }

    /**
     * Verifies that a member of another family cannot become administrator.
     */
    @Test
    public void changeAdminDifferentFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User otherAdmin = insertTestingUser("otherfamilyadmin", password);

        Family otherFamily = insertTestingFamily(otherAdmin, "Other Family");

        boolean result = familyService.changeAdmin(admin, otherAdmin);

        assertFalse("changeAdminDifferentFamilyTest: should reject another family's member",
                result);
        assertEquals("changeAdminDifferentFamilyTest: should preserve the first family's administrator",
                admin, family.getFamilyAdmin());
        assertEquals("changeAdminDifferentFamilyTest: should preserve the second family's administrator",
                otherAdmin, otherFamily.getFamilyAdmin());
    }

    /*
     * = = = = = = = = = = =
     * Delete Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that deletion removes the family and its memberships,
     * clears accepted members' family references, and preserves a pending
     * applicant's membership in another family.
     */
    @Test
    public void deleteFamilySuccessTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        User otherAdmin = insertTestingUser("otherfamilyadmin", password);
        FamilyMembership pending = insertTestingPendingRequest(family, otherAdmin);
        Family otherFamily = insertTestingFamily(otherAdmin, "Other Family");
        assertEquals("deleteFamilySuccessTest: the request should still be pending before deletion",
                "PENDING", membershipService.getMembershipByID(pending.getMembershipID()).getStatus());

        Long familyID = family.getFamilyID();
        Long otherFamilyID = otherFamily.getFamilyID();

        boolean result = familyService.deleteFamily(admin);

        assertTrue("deleteFamilySuccessTest: should delete the family", result);

        entityManager.flush();
        entityManager.clear();

        assertNull("deleteFamilySuccessTest: should remove the family record",
                entityManager.find(Family.class, familyID));
        assertEquals("deleteFamilySuccessTest: should remove all associated memberships",
                0L, countMemberships(familyID));
        assertNull("deleteFamilySuccessTest: should clear the administrator's family",
                userService.validateUser(admin.getUsername(), password).getFamily());
        assertNull("deleteFamilySuccessTest: should clear the member's family",
                userService.validateUser(member.getUsername(), password).getFamily());
        assertEquals("deleteFamilySuccessTest: should preserve the applicant's other family",
                otherFamilyID,
                userService.validateUser(otherAdmin.getUsername(), password).getFamily().getFamilyID());
        assertNotNull("deleteFamilySuccessTest: should preserve the other family record",
                entityManager.find(Family.class, otherFamilyID));
        assertEquals("deleteFamilySuccessTest: should preserve the other family's membership",
                1L, countMemberships(otherFamilyID));
    }

    /**
     * Verifies that deletion fails when the user is null.
     */
    @Test
    public void deleteFamilyNullUserTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        long previousCount = countFamilies();

        boolean result = familyService.deleteFamily(null);

        assertFalse("deleteFamilyNullUserTest: should reject a null user", result);
        var savedCount = countFamilies();
        assertEquals("deleteFamilyNullUserTest: should preserve existing families",
                previousCount, savedCount);
        assertEquals("deleteFamilyNullUserTest: should preserve the administrator's family",
                family, membershipService.getFamilyForUser(admin));
    }

    /**
     * Verifies that a user without a family cannot delete one.
     */
    @Test
    public void deleteFamilyNoFamilyTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";

        User user = insertTestingUser(username, password);

        boolean result = familyService.deleteFamily(user);

        assertFalse("deleteFamilyNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that a regular member cannot delete the family.
     */
    @Test
    public void deleteFamilyNotAdminTest() {
        final String username = "familytestuser";
        final String password = "familytestpass";
        final String familyName = "Test Family";

        User admin = insertTestingUser(username, password);
        Family family = insertTestingFamily(admin, familyName);
        User member = insertTestingMember("familymember", password, family);

        boolean result = familyService.deleteFamily(member);

        assertFalse("deleteFamilyNotAdminTest: should reject a regular member", result);
        assertNotNull("deleteFamilyNotAdminTest: should preserve the family record",
                entityManager.find(Family.class, family.getFamilyID()));
        assertEquals("deleteFamilyNotAdminTest: should preserve memberships",
                2L, countMemberships(family.getFamilyID()));
        var actualFamily = admin.getFamily();
        assertEquals("deleteFamilyNotAdminTest: should preserve the administrator's family",
                family, actualFamily);
        var actualFamily2 = member.getFamily();
        assertEquals("deleteFamilyNotAdminTest: should preserve the member's family",
                family, actualFamily2);
    }

    /*
     * = = = = = = = = = = =
     * Test Helpers
     * = = = = = = = = = = =
     */

    /**
     * Creates a test user and verifies that the saved credentials work.
     *
     * @param username the username of the user to create
     * @param password the password of the user to create
     * @return the created user
     */
    private User insertTestingUser(String username, String password) {
        // Verify that the username is not already associated with an existing user.
        User user = userService.validateUser(username, password);
        assertNull("insertTestingUser: user should not exist yet", user);

        // Create the new user and verify that the user was saved successfully.
        user = userService.newUser(username, password);
        assertNotNull("insertTestingUser: user should exist after creation", user);
        assertNotNull("insertTestingUser: user should have an ID", user.getId());
        assertEquals("insertTestingUser: user should have the correct username", username, user.getUsername());

        // Validate the newly created user's credentials to verify that login works.
        User userCheck = userService.validateUser(username, password);
        assertNotNull("insertTestingUser: user should exist after validation", userCheck);

        // Verify that authentication returns the same user that was originally created.
        assertEquals("insertTestingUser: login should return the created user's ID", user.getId(), userCheck.getId());
        assertEquals("insertTestingUser: login should return the expected username", username, userCheck.getUsername());
        return userCheck;
    }

    /**
     * Creates a family and checks its administrator and initial membership.
     *
     * @param admin the administrator of the family to create
     * @param familyName the name of the family to create
     * @return the created family
     */
    private Family insertTestingFamily(User admin, String familyName) {
        // Verify that the administrator does not already belong to a family.
        assertNull("insertTestingFamily: the administrator should start without a family",
                membershipService.getFamilyForUser(admin));

        // Create the family using the specified administrator and name.
        boolean result = familyService.createFamily(admin, familyName);
        assertTrue("insertTestingFamily: should create the family", result);

        // Retrieve the newly created family through the administrator's membership.
        Family family = membershipService.getFamilyForUser(admin);

        // Verify that the family was created with the expected information.
        assertNotNull("insertTestingFamily: should find the created family", family);
        assertNotNull("insertTestingFamily: family should have an ID", family.getFamilyID());
        assertEquals("insertTestingFamily: should store the expected name", familyName, family.getFamilyName());
        assertEquals("insertTestingFamily: should assign the administrator", admin, family.getFamilyAdmin());

        // Retrieve the family's memberships and verify that the administrator has an accepted membership.
        List<FamilyMembership> memberships = membershipService.getFamilyMembers(admin);
        assertEquals("insertTestingFamily: should create exactly one membership", 1, memberships.size());
        assertEquals("insertTestingFamily: should accept the administrator", "ACCEPTED", memberships.getFirst().getStatus());
        assertEquals("insertTestingFamily: membership should belong to the administrator", admin, memberships.getFirst().getUser());
        return family;
    }

    /**
     * Creates a member through the request and acceptance workflow.
     *
     * @param username the username of the member to create
     * @param password the password of the member to create
     * @param family the family to request to join
     * @return the created member
     */
    private User insertTestingMember(String username, String password, Family family) {
        // Create a new user who will request to join the family.
        User member = insertTestingUser(username, password);

        // Submit a membership request for the newly created user.
        FamilyMembership request = insertTestingPendingRequest(family, member);

        // Accept the membership request using the family's administrator.
        boolean result = membershipService.acceptRequest(family.getFamilyAdmin(), request);
        assertTrue("insertTestingMember: should accept the member", result);

        // Retrieve the saved membership to verify that the request was accepted.
        FamilyMembership membership = membershipService.getMembershipByID(request.getMembershipID());
        assertNotNull("insertTestingMember: should find the accepted membership", membership);

        // Verify that the membership has accepted status and the user belongs to the specified family.
        assertEquals("insertTestingMember: should save accepted status", "ACCEPTED", membership.getStatus());
        assertEquals("insertTestingMember: should associate the member with the family",
                family, membershipService.getFamilyForUser(member));
        return member;
    }

    /**
     * Submits a join request and verifies its persisted status and associations.
     *
     * @param family the family to request to join
     * @param applicant the user who is requesting to join the family
     * @return the created membership request
     */
    private FamilyMembership insertTestingPendingRequest(Family family, User applicant) {
        // Retrieve the administrator of the family to access its pending requests.
        User admin = family.getFamilyAdmin();

        // Verify that the applicant does not already have a pending request for this family.
        assertTrue("insertTestingPendingRequest: applicant should have no pending request for this family",
                membershipService.getPendingRequests(admin).stream()
                        .noneMatch(request -> applicant.equals(request.getUser())));

        // Submit a request for the applicant to join the specified family.
        boolean result = membershipService.requestToJoinFamily(applicant, family.getFamilyID());
        assertTrue("insertTestingPendingRequest: should submit the request", result);

        // Retrieve pending requests and find the request belonging to the applicant.
        List<FamilyMembership> requests = membershipService.getPendingRequests(admin).stream()
                .filter(request -> applicant.equals(request.getUser()))
                .toList();

        // Verify that exactly one pending request was created for the applicant.
        assertEquals("insertTestingPendingRequest: should find one request for the applicant", 1, requests.size());

        // Retrieve the matching request and verify its ID, associations, and status.
        FamilyMembership request = requests.getFirst();
        assertNotNull("insertTestingPendingRequest: request should have an ID", request.getMembershipID());
        assertEquals("insertTestingPendingRequest: should save the expected family", family, request.getFamily());
        assertEquals("insertTestingPendingRequest: should save the expected applicant", applicant, request.getUser());
        assertEquals("insertTestingPendingRequest: should save pending status", "PENDING", request.getStatus());

        return request;
    }

    /**
     * Counts family rows, including any family without a membership.
     *
     * @return the number of families
     */
    private long countFamilies() {
        // Execute a JPQL query to count all family records in the database.
        return entityManager.createQuery("select count(family) from Family family", Long.class)
                .getSingleResult();
    }

    /**
     * Counts memberships in every status, including pending and rejected requests.
     *
     * @param familyID the ID of the family to count memberships for
     * @return the number of memberships in the family
     */
    private long countMemberships(Long familyID) {
        // Count every membership associated with the specified family.
        // No status condition is applied, so all membership statuses are included.
        return entityManager.createQuery("""
                    select count(membership) from FamilyMembership membership
                    where membership.family.familyID = :familyID
                    """, Long.class)
                // Supply the family ID used to filter the query.
                .setParameter("familyID", familyID)
                .getSingleResult();
    }
}
