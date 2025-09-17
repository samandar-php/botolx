package org.example;

public class UserEntity {
    private Long chatId;
    private String role;
    private String step;
    private String email;
    private String code; // email tasdiqlash kodi

    public UserEntity(Long chatId, String role, String step) {
        this.chatId = chatId;
        this.role = role;
        this.step = step;
    }

    public UserEntity(Long chatId, String role, String step, String email, String code) {
        this.chatId = chatId;
        this.role = role;
        this.step = step;
        this.email = email;
        this.code = code;
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStep() { return step; }
    public void setStep(String step) { this.step = step; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
