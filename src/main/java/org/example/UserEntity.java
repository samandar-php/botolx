package org.example;

class UserEntity {
    private final Long chatId;
    private final String role;
    private String step;
    private String email;
    private String code;
    private boolean active;

    public UserEntity(Long chatId, String role, String step, String email, String code, boolean active) {
        this.chatId = chatId;
        this.role = role;
        this.step = step;
        this.email = email;
        this.code = code;
        this.active = active;
    }


    public Long getChatId() { return chatId; }
    public String getRole() { return role; }
    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}