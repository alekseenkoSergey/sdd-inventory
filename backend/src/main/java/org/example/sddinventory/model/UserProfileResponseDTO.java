package org.example.sddinventory.model;

public class UserProfileResponseDTO {
    private Long id;
    private String provider;
    private String email;
    private String displayName;
    private String avatarUrl;

    public UserProfileResponseDTO() {}

    public UserProfileResponseDTO(Long id, String provider, String email, String displayName, String avatarUrl) {
        this.id = id;
        this.provider = provider;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
