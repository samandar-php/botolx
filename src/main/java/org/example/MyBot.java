package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyBot extends TelegramLongPollingBot {

    private final Path userPath = Path.of("user_data.txt");
    private final Map<Long, UserEntity> userMap = new HashMap<>();

    public MyBot() {
        loadUsers();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage());
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        UserEntity user = userMap.get(chatId);

        if (text.equals("/start")) {

            if (user == null || !user.isActive()) {
                user = new UserEntity(chatId, "user", "INPUT", null, null, false);
                userMap.put(chatId, user);
                sendMessage(chatId, "<b>Salom! Botdan ro'yxatdan o'tmagansiz</b>\n\n1. Ro'yxatdan o'tish (Sign Up)\n2. Kirish (Sign In)");
            } else {
                sendMainMenu(chatId);
            }
            saveUsers();
            return;
        }



        switch (user.getStep()) {
            case "INPUT" -> handleInputStep(chatId, text, user);
            case "EMAIL_INPUT" -> handleEmailInputStep(chatId, text, user);
            case "CODE_INPUT" -> handleCodeInputStep(chatId, text, user);
            case "SIGNIN_EMAIL" -> handleSignInEmailStep(chatId, text, user);
            case "SIGNIN_CODE" -> handleSignInCodeStep(chatId, text, user);
            case "FINISH" -> handleFinishStep(chatId, text);
            default -> sendMessage(chatId, "<b>Noma'lum buyruq. Iltimos /start ni yuboring.</b>");
        }

        saveUsers();
    }

    private void handleInputStep(Long chatId, String text, UserEntity user) {
        if (text.equals("1")) {
            sendMessage(chatId, "<b>Ro'yxatdan o'tish</b>\n\nEmail manzilingizni yuboring:");
            user.setStep("EMAIL_INPUT");
        } else if (text.equals("2")) {
            sendMessage(chatId, "<b>Tizimga kirish</b>\n\nEmail manzilingizni yuboring:");
            user.setStep("SIGNIN_EMAIL");
        } else {
            sendMessage(chatId, "<b>Noto'g'ri tanlov. Iltimos 1 yoki 2 ni tanlang.</b>");
        }
    }

    private void handleEmailInputStep(Long chatId, String text, UserEntity user) {
        if (text.contains("@") && text.contains(".")) {
            user.setEmail(text);
            Random random = new Random();
            int randomInt = 10000 + random.nextInt(90000);
            user.setCode(String.valueOf(randomInt));
            sendMessage(chatId, "<b>Rahmat!</b> \n\nEmail manzilingizni tasdiqlash uchun kod yuborildi:\n\n" + randomInt);
            user.setStep("CODE_INPUT");
        } else {
            sendMessage(chatId, "<b>Noto'g'ri email format. Iltimos qayta kiriting:</b>");
        }
    }

    private void handleCodeInputStep(Long chatId, String text, UserEntity user) {
        if (text.equals(user.getCode())) {
            user.setActive(true);
            sendMainMenu(chatId);
            user.setStep("FINISH");
        } else {
            sendMessage(chatId, "<b>Tasdiqlash kodi noto'g'ri. Iltimos qayta urinib ko'ring.</b>");
        }
    }

    private void handleSignInEmailStep(Long chatId, String text, UserEntity user) {
        boolean found = false;
        for (UserEntity u : userMap.values()) {
            if (text.equals(u.getEmail()) && u.isActive()) {
                user.setEmail(u.getEmail());
                user.setCode(u.getCode());
                sendMessage(chatId, "<b>Email manzilingizni tasdiqlash uchun kod yuborildi:</b> " + u.getCode());
                user.setStep("SIGNIN_CODE");
                found = true;
                break;
            }
        }
        if (!found) {
            sendMessage(chatId, "<b>Bunday email mavjud emas yoki tasdiqlanmagan. Iltimos qayta urinib ko'ring.</b>");
            user.setStep("INPUT");
        }
    }

    private void handleSignInCodeStep(Long chatId, String text, UserEntity user) {
        if (text.equals(user.getCode())) {
            sendMainMenu(chatId);
            user.setStep("FINISH");
        } else {
            sendMessage(chatId, "<b>Tasdiqlash kodi noto'g'ri. Iltimos qayta urinib ko'ring.</b>");
        }
    }

    private void handleFinishStep(Long chatId, String text) {
        switch (text) {
            case "1" -> sendMessage(chatId, "<b>Yangi e'lon joylash funksiyasi hozircha ishga tushurilmagan.</b>");
            case "2" -> sendMessage(chatId, "<b>Sizning e'lonlaringiz funksiyasi hozircha ishga tushurilmagan.</b>");
            case "3" -> sendMessage(chatId, "<b>Barcha e'lonlar funksiyasi hozircha ishga tushurilmagan.</b>");
            default -> sendMessage(chatId, "<b>Noto'g'ri tanlov. Iltimos quyidagilardan birini tanlang:</b>\n\n1. Yangi e'lon joylash\n2. E'lonlarim\n3. Barcha e'lonlar");
        }
    }

    private void sendMainMenu(Long chatId) {
        sendMessage(chatId, """
                <b>Asosiy menyu</b>
                
                1. Yangi e'lon joylash
                2. E'lonlarim
                3. Barcha e'lonlar""");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Xabar yuborishda xatolik: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(userPath)) {
            for (UserEntity user : userMap.values()) {
                writer.write(user.getChatId() + "," +
                        user.getRole() + "," +
                        user.getStep() + "," +
                        (user.getEmail() != null ? user.getEmail() : "") + "," +
                        (user.getCode() != null ? user.getCode() : "") + "," +
                        user.isActive());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Foydalanuvchi ma'lumotlarini saqlashda xatolik: " + e.getMessage());
        }
    }

    private void loadUsers() {
        if (!Files.exists(userPath)) return;

        try (BufferedReader reader = Files.newBufferedReader(userPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length >= 6) {
                    try {
                        Long chatId = Long.parseLong(parts[0]);
                        String role = parts[1];
                        String step = parts[2];
                        String email = parts[3].isEmpty() ? null : parts[3];
                        String code = parts[4].isEmpty() ? null : parts[4];
                        boolean active = Boolean.parseBoolean(parts[5]);
                        userMap.put(chatId, new UserEntity(chatId, role, step, email, code, active));
                    } catch (NumberFormatException e) {
                        System.err.println("Chat ID ni o'qishda xatolik: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Foydalanuvchi ma'lumotlarini yuklashda xatolik: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "OlxDemo_bot";
    }

    @Override
    public String getBotToken() {
        return "8221166771:AAHRlr9xfmNZuKx9zJaXPkNII3fZINKFtgo";
    }
}
