package lk.sliit.recruitment.model;

/** The four user roles defined in the proposal (Section 05). */
public enum Role {
    CANDIDATE("Candidate / Job Seeker"),
    RECRUITER("Recruitment Staff"),
    EMPLOYER("Employer / HR Representative"),
    ADMIN("System Administrator");

    private final String label;

    Role(String label) { this.label = label; }

    public String getLabel() { return label; }
}
