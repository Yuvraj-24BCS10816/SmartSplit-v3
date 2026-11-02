package com.smartsplit.service;

import com.smartsplit.dao.GroupDAO;
import com.smartsplit.dao.UserDAO;
import com.smartsplit.exception.GroupNotFoundException;
import com.smartsplit.model.Group;
import com.smartsplit.model.Member;
import com.smartsplit.model.User;
import com.smartsplit.util.PasswordUtil;
import com.smartsplit.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Business logic for groups and their members. */
public class GroupService {
    private final GroupDAO dao = new GroupDAO();
    private final UserDAO users = new UserDAO();

    public int create(String name, String description, int creatorId) {
        return dao.create(new Group(
                0,
                Validator.required(name, "Group name"),
                description == null ? "" : description.trim(),
                creatorId,
                LocalDateTime.now()
        ));
    }

    public List<Group> list(int userId) {
        return dao.findByUser(userId);
    }

    public Group get(int id) {
        return dao.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not found."));
    }

    public List<Member> members(int groupId) {
        return dao.members(groupId);
    }

    public void addMemberByUsername(int groupId, String username) {
        String clean = Validator.required(username, "Username");
        User u = users.findByUsername(clean)
                .orElseThrow(() -> new IllegalArgumentException("No user exists with username: " + clean));
        dao.addMember(groupId, u.getId());
    }

    /**
     * Creates a local participant account automatically and adds it to the group.
     * This is useful for demonstrations where participants do not need separate logins.
     */
    public Member addGuestMember(int groupId, String name, String requestedUsername) {
        String cleanName = Validator.required(name, "Participant name");
        boolean hasRequestedUsername = requestedUsername != null && !requestedUsername.trim().isEmpty();
        String username = hasRequestedUsername ? requestedUsername.trim() : uniqueUsername(cleanName);
        if (hasRequestedUsername && !username.matches("[A-Za-z0-9._-]{3,32}")) {
            throw new IllegalArgumentException("Participant username must be 3–32 characters and use only letters, numbers, dots, underscores or hyphens.");
        }

        User existing = users.findByUsername(username).orElse(null);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "That username is already in use. Leave the username blank or choose a different one for this participant.");
        }

        String email = username + "@guest.smartsplit.local";
        String generatedPassword = UUID.randomUUID().toString();
        int id = users.create(
                new User(0, cleanName, username, email),
                PasswordUtil.hash(generatedPassword)
        );
        existing = new User(id, cleanName, username, email);

        dao.addMember(groupId, existing.getId());
        return new Member(existing.getId(), existing.getName(), existing.getUsername(), existing.getEmail());
    }

    public void addDemoMembers(int groupId) {
        addMemberByUsername(groupId, "rahul");
        addMemberByUsername(groupId, "aman");
        addMemberByUsername(groupId, "karan");
    }

    public void addMember(int groupId, int userId) {
        dao.addMember(groupId, userId);
    }

    public void removeMember(int groupId, int userId) {
        if (dao.hasFinancialActivity(groupId, userId)) {
            throw new IllegalArgumentException("This member has expense or settlement history and cannot be removed safely.");
        }
        dao.removeMember(groupId, userId);
    }

    /** Deletes a group only when requested by the account that created it. */
    public void deleteGroup(int groupId, int requesterId) {
        Group group = get(groupId);
        if (group.getCreatedBy() != requesterId) {
            throw new IllegalArgumentException("Only the group creator can delete this group.");
        }
        dao.delete(groupId);
    }

    private String uniqueUsername(String name) {
        String base = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "participant";
        String candidate = base;
        int suffix = 2;
        while (users.findByUsername(candidate).isPresent()) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }
}
