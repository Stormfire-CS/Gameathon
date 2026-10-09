package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.FamiliesRepository;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class FamilyServiceImpl implements FamilyService {

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
            return null;
        }

        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            return null;
        }

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
        Login user = findUser(username);

        if (user == null) {
            return null;
        }

        List<FamilyMembership> memberships = membershipRepo.findByUserIDAndStatus(user.getId(), ACCEPTED);

        if (memberships.isEmpty()) {
            return null;
        }

        FamilyMembership membership = memberships.getFirst();

        return familiesRepo.findById(membership.getFamilyID()).orElse(null);
    }

    @Override
    public boolean createFamily(String username, String familyName) {
        Login user = findUser(username);

        if (user == null || familyName == null) {
            return false;
        }

        if (hasAcceptedMembership(user.getId())) {
            return false;
        }

        String cleanedName = familyName.trim();

        if (familiesRepo.existsByFamilyNameIgnoreCase(cleanedName)) {
            return false;
        }

        Family family = new Family(cleanedName, user.getId());

        family = familiesRepo.save(family);

        FamilyMembership membership = new FamilyMembership(family.getFamilyID(), user.getId(), ACCEPTED);

        membershipRepo.save(membership);

        return true;
    }

    @Override
    public boolean requestToJoinFamily(String username, Long familyID) {

        Login user = findUser(username);

        if (user == null || familyID == null) {
            return false;
        }

        if (hasAcceptedMembership(user.getId())) {
            return false;
        }

        Family family = familiesRepo.findById(familyID).orElse(null);

        if (family == null) {
            return false;
        }

        FamilyMembership previousRequest = membershipRepo.findByFamilyIDAndUserID(familyID, user.getId());

        if (previousRequest != null) {
            if (PENDING.equals(previousRequest.getStatus()) || ACCEPTED.equals(previousRequest.getStatus())) {
                return false;
            }

            previousRequest.setStatus(PENDING);
            membershipRepo.save(previousRequest);
            return true;
        }

        FamilyMembership request = new FamilyMembership(familyID, user.getId(), PENDING);

        membershipRepo.save(request);

        return true;
    }

    @Override
    public List<FamilyMembership> getPendingRequests(String username) {

        Login admin = findUser(username);

        if (admin == null) {
            return new LinkedList<>();
        }

        Family family = familiesRepo.findByFamilyAdminId(admin.getId());

        if (family == null) {
            return new LinkedList<>();
        }
        return membershipRepo.findByFamilyIDAndStatus(family.getFamilyID(), PENDING);
    }

    @Override
    public boolean acceptRequest(String username, Long membershipID) {

        Login admin = findUser(username);

        if (admin == null || membershipID == null) {
            return false;
        }

        FamilyMembership request = membershipRepo.findById(membershipID).orElse(null);

        if (request == null || !PENDING.equals(request.getStatus())) {
            return false;
        }

        Family family = familiesRepo.findById(request.getFamilyID()).orElse(null);

        if (family == null || !family.getFamilyAdminId().equals(admin.getId())) {
            return false;
        }

        if (hasAcceptedMembership(request.getUserID())) {
            return false;
        }

        request.setStatus(ACCEPTED);
        membershipRepo.save(request);

        return true;
    }

    @Override
    public boolean rejectRequest(String username, Long membershipID) {

        Login admin = findUser(username);

        if (admin == null || membershipID == null) {
            return false;
        }

        FamilyMembership request = membershipRepo.findById(membershipID).orElse(null);

        if (request == null || !PENDING.equals(request.getStatus())) {
            return false;
        }

        Family family = familiesRepo.findById(request.getFamilyID()).orElse(null);

        if (family == null || !family.getFamilyAdminId().equals(admin.getId())) {
            return false;
        }

        request.setStatus(REJECTED);
        membershipRepo.save(request);

        return true;
    }

    @Override
    public boolean isFamilyAdmin(String username, Long familyID) {
        Login user = findUser(username);

        if (user == null || familyID == null) {
            return false;
        }

        Family family = familiesRepo.findById(familyID).orElse(null);

        return family != null && family.getFamilyAdminId().equals(user.getId());
    }
}

