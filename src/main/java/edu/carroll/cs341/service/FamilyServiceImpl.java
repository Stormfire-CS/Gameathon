package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.FamiliesRepository;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class FamilyServiceImpl implements FamilyService {
    private static final Logger log = LoggerFactory.getLogger(FamilyServiceImpl.class);

    //Will make this enumeration
    private static final String PENDING = "PENDING";
    private static final String ACCEPTED = "ACCEPTED";
    private static final String REJECTED = "REJECTED";

    private final FamiliesRepository familiesRepo;
    private final FamilyMembershipRepository membershipRepo;
    private final LoginRepository loginRepo;

    public FamilyServiceImpl(FamiliesRepository familiesRepo, FamilyMembershipRepository membershipRepo, LoginRepository loginRepo) {
        this.familiesRepo = familiesRepo;
        this.membershipRepo = membershipRepo;
        this.loginRepo = loginRepo;
    }

    private Login findUser(String username) {
        if (username == null) {
            log.debug("findUser: username is null");
            return null;
        }

        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            log.debug("findUser: found {} users for username '{}'",users.size(), username);
            return null;
        }

        log.info("findUser: successfully found {}", username);

        return users.getFirst();
    }

    private boolean hasAcceptedMembership(Long userID) {
        return !membershipRepo.findByUserIDAndStatus(userID, ACCEPTED).isEmpty();
    }

    private boolean hasPendingRequest(Long userID) {
        return !membershipRepo.findByUserIDAndStatus(userID, PENDING).isEmpty();
    }

    @Override
    public Family getFamilyForUser(String username) {
        log.debug("getFamilyForUser: looking up family for '{}'", username);

        Login user = findUser(username);

        if (user == null) {
            log.debug("getFamilyForUser: {} not found", username);
            return null;
        }

        List<FamilyMembership> memberships = membershipRepo.findByUserIDAndStatus(user.getId(), ACCEPTED);

        if (memberships.isEmpty()) {
            log.info("getFamilyForUser: user {} has no membership", username);
            return null;
        }

        FamilyMembership membership = memberships.getFirst();
        Family family = familiesRepo.findById(membership.getFamilyID()).orElse(null);

        if (family == null) {
            log.debug("getFamilyForUser: family {} not found", membership.getFamilyID());
        }

        return family;
    }

    @Override
    public boolean createFamily(String username, String familyName) {
        log.debug("createFamily: user {} attempted to create family {}", username, familyName);

        Login user = findUser(username);

        if (user == null || familyName == null) {
            log.debug("createFamily: user or family name is null");
            return false;
        }

        if (hasAcceptedMembership(user.getId())) {
            log.debug("createFamily: user '{}' already belongs to a family", username);
            return false;
        }

        String cleanedName = familyName.trim();

        if (familiesRepo.existsByFamilyNameIgnoreCase(cleanedName)) {
            log.debug("createFamily: family name '{}' already exists", cleanedName);
            return false;
        }

        Family family = new Family(cleanedName, user.getId());

        family = familiesRepo.save(family);

        FamilyMembership membership = new FamilyMembership(family.getFamilyID(), user.getId(), ACCEPTED);

        membershipRepo.save(membership);

        log.info("createFamily: user '{}' created family '{}'", username, cleanedName);

        return true;
    }

    @Override
    public boolean requestToJoinFamily(String username, Long familyID) {
        log.debug("requestToJoinFamily: user '{}' requested family {}", username, familyID);

        Login user = findUser(username);

        if (user == null || familyID == null) {
            log.debug("requestToJoinFamily: user: {} or family ID: {} is null", user, familyID);
            return false;
        }

        if (hasAcceptedMembership(user.getId())) {
            log.debug("requestToJoinFamily: user '{}' already belongs to a family", username);
            return false;
        }

        Family family = familiesRepo.findById(familyID).orElse(null);

        if (family == null) {
            log.debug("requestToJoinFamily: family {} not found", familyID);
            return false;
        }

        FamilyMembership previousRequest = membershipRepo.findByFamilyIDAndUserID(familyID, user.getId());

        if (previousRequest != null) {
            if (PENDING.equals(previousRequest.getStatus()) || ACCEPTED.equals(previousRequest.getStatus())) {
                log.debug("requestToJoinFamily: request already exists with status {}", previousRequest.getStatus());
                return false;
            }

            previousRequest.setStatus(PENDING);
            membershipRepo.save(previousRequest);

            log.info("requestToJoinFamily: user '{}' resubmitted request to join family {}", username, familyID);
            return true;
        }

        FamilyMembership request = new FamilyMembership(familyID, user.getId(), PENDING);

        log.info("requestToJoinFamily: user successfully {} submitted request to join family {} for the first time", username, familyID);
        membershipRepo.save(request);

        return true;
    }

    @Override
    public List<FamilyMembership> getPendingRequests(String username) {
        log.debug("getPendingRequests: retrieving requests for {}", username);

        Login admin = findUser(username);

        if (admin == null) {
            log.debug("getPendingRequests: admin was not found for username {}", username);
            return new LinkedList<>();
        }

        Family family = familiesRepo.findByFamilyAdminId(admin.getId());

        if (family == null) {
            log.debug("getPendingRequests: user '{}' does not administer a family", username);
            return new LinkedList<>();
        }

        return membershipRepo.findByFamilyIDAndStatus(family.getFamilyID(), PENDING);
    }

    @Override
    public boolean acceptRequest(String username, Long membershipID) {
        log.debug("acceptRequest: user '{}' attempted to accept request {}", username, membershipID);

        Login admin = findUser(username);

        if (admin == null || membershipID == null) {
            log.debug("acceptRequest: admin or membership ID is invalid");
            return false;
        }

        FamilyMembership request = membershipRepo.findById(membershipID).orElse(null);

        if (request == null || !PENDING.equals(request.getStatus())) {
            log.debug("acceptRequest: request {} not found or not pending, is ()", membershipID);
            return false;
        }

        Family family = familiesRepo.findById(request.getFamilyID()).orElse(null);

        if (family == null || !family.getFamilyAdminId().equals(admin.getId())) {
            log.debug("acceptRequest: user '{}' is not the family admin", username);
            return false;
        }

        if (hasAcceptedMembership(request.getUserID())) {
            log.debug("acceptRequest: user {} already belongs to a family", request.getUserID());
            return false;
        }

        request.setStatus(ACCEPTED);
        membershipRepo.save(request);

        log.info("acceptRequest: user successfully {} accepted membership request {}", username, membershipID);

        return true;
    }

    @Override
    public boolean rejectRequest(String username, Long membershipID) {
        log.debug("rejectRequest: user {} attempted to reject request {}", username, membershipID);

        Login admin = findUser(username);

        if (admin == null || membershipID == null) {
            log.debug("rejectRequest: admin or membership ID is invalid");
            return false;
        }

        FamilyMembership request = membershipRepo.findById(membershipID).orElse(null);

        if (request == null || !PENDING.equals(request.getStatus())) {
            log.debug("rejectRequest: request {} not found or not pending", membershipID);
            return false;
        }

        Family family = familiesRepo.findById(request.getFamilyID()).orElse(null);

        if (family == null || !family.getFamilyAdminId().equals(admin.getId())) {
            log.debug("rejectRequest: user '{}' is not the family admin or no family found, family: {}", username, family);
            return false;
        }

        request.setStatus(REJECTED);
        membershipRepo.save(request);

        log.info("rejectRequest: user successfully {} rejected membership request {}", username, membershipID);

        return true;
    }

    @Override
    public boolean isFamilyAdmin(String username, Long familyID) {
        log.debug("isFamilyAdmin: checking to see if {} administers family {}", username, familyID);

        Login user = findUser(username);

        if (user == null || familyID == null) {
            log.debug("isFamilyAdmin: either username {} or familyID {} is false", username, familyID);
            return false;
        }

        Family family = familiesRepo.findById(familyID).orElse(null);

        boolean isAdmin = family != null && family.getFamilyAdminId().equals(user.getId());

        log.info("isFamilyAdmin: user {} admins status is {}", username, isAdmin);

        return isAdmin;
    }
}

