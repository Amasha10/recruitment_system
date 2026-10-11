package lk.sliit.recruitment.dto;

import lk.sliit.recruitment.model.Role;

public record CurrentUser(Long id, String fullName, String email, Role role) { }
