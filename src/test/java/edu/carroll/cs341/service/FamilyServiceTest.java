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
 * Tests family creation, administrator permissions, renaming,
 * administrator changes, and deletion.
 *
 * <p>ChatGPT assisted with generating these tests.
 * Review and adapt them before use.</p>
 */
@SpringBootTest
@Transactional
public class FamilyServiceTest {
    private static final String username = "familytestuser";
    private static final String password = "familytestpass";
    private static final String familyName = "Test Family";

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
     * Create Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that creation assigns the creator as administrator
     * and creates an accepted membership.
     */
    @Test
    public void createFamilySuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.createFamily(user, familyName);

        assertTrue("createFamilySuccessTest: should create the family", result);

        Family family = familiesRepo.findByFamilyAdmin(user);

        assertNotNull("createFamilySuccessTest: should save the family", family);
        assertEquals("createFamilySuccessTest: should save the name",
                familyName, family.getFamilyName());
        assertEquals("createFamilySuccessTest: should assign the administrator",
                user, family.getFamilyAdmin());
        assertEquals("createFamilySuccessTest: should update the user's family",
                family, userRepo.findById(user.getId()).orElseThrow().getFamily());

        List<FamilyMembership> memberships = membershipRepo.findByFamily(family);

        assertEquals("createFamilySuccessTest: should create one membership",
                1, memberships.size());
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
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.createFamily(user, "  Test Family  ");

        assertTrue("createFamilyNameWithSpacesTest: should accept the name", result);
        assertEquals("createFamilyNameWithSpacesTest: should trim the name",
                familyName, familiesRepo.findByFamilyAdmin(user).getFamilyName());
    }

    /**
     * Verifies that creation fails when the user is null.
     */
    @Test
    public void createFamilyNullUserTest() {
        long previousCount = familiesRepo.count();

        boolean result = familyService.createFamily(null, familyName);

        assertFalse("createFamilyNullUserTest: should reject a null user", result);
        assertEquals("createFamilyNullUserTest: should not create a family",
                previousCount, familiesRepo.count());
    }

    /**
     * Verifies that creation fails when the name is null.
     */
    @Test
    public void createFamilyNullNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.createFamily(user, null);

        assertFalse("createFamilyNullNameTest: should reject a null name", result);
        assertNull("createFamilyNullNameTest: should leave the user without a family",
                user.getFamily());
        assertNull("createFamilyNullNameTest: should not save a family",
                familiesRepo.findByFamilyAdmin(user));
    }

    /**
     * Verifies that creation fails when the name is empty.
     */
    @Test
    public void createFamilyEmptyNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.createFamily(user, "");

        assertFalse("createFamilyEmptyNameTest: should reject an empty name", result);
        assertNull("createFamilyEmptyNameTest: should not save a family",
                familiesRepo.findByFamilyAdmin(user));
    }

    /**
     * Verifies that creation fails when the name contains only spaces.
     */
    @Test
    public void createFamilyWhitespaceNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.createFamily(user, "   ");

        assertFalse("createFamilyWhitespaceNameTest: should reject a blank name",
                result);
        assertNull("createFamilyWhitespaceNameTest: should not save a family",
                familiesRepo.findByFamilyAdmin(user));
    }

    /**
     * Verifies that a user already belonging to a family cannot create another.
     */
    @Test
    public void createFamilyExistingFamilyTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family originalFamily = insertTestingFamily(user);
        long previousCount = familiesRepo.count();

        boolean result = familyService.createFamily(user, "Another Family");

        assertFalse("createFamilyExistingFamilyTest: should reject an existing member",
                result);
        assertEquals("createFamilyExistingFamilyTest: should preserve the user's family",
                originalFamily, user.getFamily());
        assertEquals("createFamilyExistingFamilyTest: should not create another family",
                previousCount, familiesRepo.count());
        assertEquals("createFamilyExistingFamilyTest: should preserve the original name",
                familyName, originalFamily.getFamilyName());
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
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.isFamilyAdmin(user, family);

        assertTrue("isFamilyAdminSuccessTest: should recognize the administrator",
                result);
    }

    /**
     * Verifies that membership alone does not grant administrator permissions.
     */
    @Test
    public void isFamilyAdminRegularMemberTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        boolean result = familyService.isFamilyAdmin(member, family);

        assertFalse("isFamilyAdminRegularMemberTest: should reject a regular member",
                result);
    }

    /**
     * Verifies that a null user is not an administrator.
     */
    @Test
    public void isFamilyAdminNullUserTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.isFamilyAdmin(null, family);

        assertFalse("isFamilyAdminNullUserTest: should reject a null user", result);
    }

    /**
     * Verifies that a null family has no administrator.
     */
    @Test
    public void isFamilyAdminNullFamilyTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

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
     * Verifies that the administrator can rename the family.
     */
    @Test
    public void renameFamilySuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.renameFamily(user, "New Family Name");

        assertTrue("renameFamilySuccessTest: should rename the family", result);
        assertEquals("renameFamilySuccessTest: should save the new name",
                "New Family Name",
                familiesRepo.findById(family.getFamilyID()).orElseThrow().getFamilyName());
        assertEquals("renameFamilySuccessTest: should preserve the administrator",
                user, family.getFamilyAdmin());
    }

    /**
     * Verifies that surrounding spaces are removed from the new name.
     */
    @Test
    public void renameFamilyNameWithSpacesTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        insertTestingFamily(user);

        boolean result = familyService.renameFamily(user, "  New Family Name  ");

        assertTrue("renameFamilyNameWithSpacesTest: should accept the name", result);
        assertEquals("renameFamilyNameWithSpacesTest: should trim the new name",
                "New Family Name",
                familiesRepo.findByFamilyAdmin(user).getFamilyName());
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
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.renameFamily(user, null);

        assertFalse("renameFamilyNullNameTest: should reject a null name", result);
        assertEquals("renameFamilyNullNameTest: should preserve the original name",
                familyName, family.getFamilyName());
    }

    /**
     * Verifies that renaming fails when the name is empty.
     */
    @Test
    public void renameFamilyEmptyNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.renameFamily(user, "");

        assertFalse("renameFamilyEmptyNameTest: should reject an empty name", result);
        assertEquals("renameFamilyEmptyNameTest: should preserve the original name",
                familyName, family.getFamilyName());
    }

    /**
     * Verifies that renaming fails when the name contains only spaces.
     */
    @Test
    public void renameFamilyWhitespaceNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Family family = insertTestingFamily(user);

        boolean result = familyService.renameFamily(user, "   ");

        assertFalse("renameFamilyWhitespaceNameTest: should reject a blank name",
                result);
        assertEquals("renameFamilyWhitespaceNameTest: should preserve the original name",
                familyName, family.getFamilyName());
    }

    /**
     * Verifies that a user without a family cannot rename one.
     */
    @Test
    public void renameFamilyNoFamilyTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.renameFamily(user, "New Family Name");

        assertFalse("renameFamilyNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that a regular member cannot rename the family.
     */
    @Test
    public void renameFamilyNotAdminTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        boolean result = familyService.renameFamily(member, "New Family Name");

        assertFalse("renameFamilyNotAdminTest: should reject a regular member",
                result);
        assertEquals("renameFamilyNotAdminTest: should preserve the name",
                familyName, family.getFamilyName());
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
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        boolean result = familyService.changeAdmin(admin, member);

        assertTrue("changeAdminSuccessTest: should change the administrator", result);
        assertEquals("changeAdminSuccessTest: should save the new administrator",
                member,
                familiesRepo.findById(family.getFamilyID()).orElseThrow().getFamilyAdmin());
        assertTrue("changeAdminSuccessTest: should recognize the new administrator",
                familyService.isFamilyAdmin(member, family));
        assertFalse("changeAdminSuccessTest: should remove the old administrator's permissions",
                familyService.isFamilyAdmin(admin, family));
        assertEquals("changeAdminSuccessTest: should preserve the old administrator's membership",
                family, admin.getFamily());
        assertEquals("changeAdminSuccessTest: should preserve both accepted memberships",
                2, membershipRepo.findByFamilyAndStatus(family, "ACCEPTED").size());
    }

    /**
     * Verifies that a null current administrator is rejected.
     */
    @Test
    public void changeAdminNullCurrentAdminTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.changeAdmin(null, user);

        assertFalse("changeAdminNullCurrentAdminTest: should reject a null current admin",
                result);
    }

    /**
     * Verifies that a null proposed administrator is rejected.
     */
    @Test
    public void changeAdminNullNewAdminTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);

        boolean result = familyService.changeAdmin(admin, null);

        assertFalse("changeAdminNullNewAdminTest: should reject a null new admin",
                result);
        assertEquals("changeAdminNullNewAdminTest: should preserve the administrator",
                admin, family.getFamilyAdmin());
    }

    /**
     * Verifies that a regular member cannot transfer administrator permissions.
     */
    @Test
    public void changeAdminNotAdminTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        boolean result = familyService.changeAdmin(member, admin);

        assertFalse("changeAdminNotAdminTest: should reject a regular member", result);
        assertEquals("changeAdminNotAdminTest: should preserve the administrator",
                admin, family.getFamilyAdmin());
    }

    /**
     * Verifies that a user without a family cannot become administrator.
     */
    @Test
    public void changeAdminNewAdminWithoutFamilyTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User outsider = userService.newUser("familyoutsider", password, password);

        boolean result = familyService.changeAdmin(admin, outsider);

        assertFalse("changeAdminNewAdminWithoutFamilyTest: should reject an outsider",
                result);
        assertEquals("changeAdminNewAdminWithoutFamilyTest: should preserve the administrator",
                admin, family.getFamilyAdmin());
    }

    /**
     * Verifies that a member of another family cannot become administrator.
     */
    @Test
    public void changeAdminDifferentFamilyTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User otherAdmin = userService.newUser("otherfamilyadmin", password, password);

        assertTrue("changeAdminDifferentFamilyTest: should create the other family",
                familyService.createFamily(otherAdmin, "Other Family"));

        boolean result = familyService.changeAdmin(admin, otherAdmin);

        assertFalse("changeAdminDifferentFamilyTest: should reject another family's member",
                result);
        assertEquals("changeAdminDifferentFamilyTest: should preserve the administrator",
                admin, family.getFamilyAdmin());
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
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        User otherAdmin = userService.newUser("otherfamilyadmin", password, password);
        assertTrue("deleteFamilySuccessTest: should create the other family",
                familyService.createFamily(otherAdmin, "Other Family"));
        Family otherFamily = otherAdmin.getFamily();

        membershipRepo.save(new FamilyMembership(family, otherAdmin, "PENDING"));

        Long familyID = family.getFamilyID();
        Long otherFamilyID = otherFamily.getFamilyID();

        boolean result = familyService.deleteFamily(admin);

        assertTrue("deleteFamilySuccessTest: should delete the family", result);

        familiesRepo.flush();
        userRepo.flush();

        assertFalse("deleteFamilySuccessTest: should remove the family record",
                familiesRepo.existsById(familyID));
        assertTrue("deleteFamilySuccessTest: should remove all associated memberships",
                membershipRepo.findByFamily(family).isEmpty());
        assertNull("deleteFamilySuccessTest: should clear the administrator's family",
                userRepo.findById(admin.getId()).orElseThrow().getFamily());
        assertNull("deleteFamilySuccessTest: should clear the member's family",
                userRepo.findById(member.getId()).orElseThrow().getFamily());
        assertEquals("deleteFamilySuccessTest: should preserve the applicant's other family",
                otherFamily,
                userRepo.findById(otherAdmin.getId()).orElseThrow().getFamily());
        assertTrue("deleteFamilySuccessTest: should preserve the other family record",
                familiesRepo.existsById(otherFamilyID));
        assertEquals("deleteFamilySuccessTest: should preserve the other family's membership",
                1, membershipRepo.findByFamily(otherFamily).size());
    }

    /**
     * Verifies that deletion fails when the user is null.
     */
    @Test
    public void deleteFamilyNullUserTest() {
        long previousCount = familiesRepo.count();

        boolean result = familyService.deleteFamily(null);

        assertFalse("deleteFamilyNullUserTest: should reject a null user", result);
        assertEquals("deleteFamilyNullUserTest: should preserve existing families",
                previousCount, familiesRepo.count());
    }

    /**
     * Verifies that a user without a family cannot delete one.
     */
    @Test
    public void deleteFamilyNoFamilyTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = familyService.deleteFamily(user);

        assertFalse("deleteFamilyNoFamilyTest: should reject a user without a family",
                result);
    }

    /**
     * Verifies that a regular member cannot delete the family.
     */
    @Test
    public void deleteFamilyNotAdminTest() {
        insertTestingUser();
        User admin = userService.validateUser(username, password);
        Family family = insertTestingFamily(admin);
        User member = insertTestingMember("familymember", family);

        boolean result = familyService.deleteFamily(member);

        assertFalse("deleteFamilyNotAdminTest: should reject a regular member", result);
        assertTrue("deleteFamilyNotAdminTest: should preserve the family record",
                familiesRepo.existsById(family.getFamilyID()));
        assertEquals("deleteFamilyNotAdminTest: should preserve memberships",
                2, membershipRepo.findByFamily(family).size());
        assertEquals("deleteFamilyNotAdminTest: should preserve the administrator's family",
                family, admin.getFamily());
        assertEquals("deleteFamilyNotAdminTest: should preserve the member's family",
                family, member.getFamily());
    }

    private void insertTestingUser() {
        userService.newUser(username, password, password);
    }

    /**
     * Creates a family through the service, including its initial membership.
     */
    private Family insertTestingFamily(User admin) {
        boolean result = familyService.createFamily(admin, familyName);

        assertTrue("insertTestingFamily: should create the test family", result);

        Family family = familiesRepo.findByFamilyAdmin(admin);

        assertNotNull("insertTestingFamily: should find the test family", family);
        return family;
    }

    /**
     * Creates an accepted member with a matching user family reference.
     */
    private User insertTestingMember(String memberUsername, Family family) {
        User member = userService.newUser(memberUsername, password, password);

        assertNotNull("insertTestingMember: should create the test user", member);

        member.setFamily(family);
        userRepo.save(member);
        membershipRepo.save(new FamilyMembership(family, member, "ACCEPTED"));

        return member;
    }
}