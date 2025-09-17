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

    private final Path userPath = Path.of("folder/user.txt");
    private final Map<Long, UserEntity> map = new HashMap<>();

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

        // Yangi foydalanuvchi bo'lsa, mapga qo'shamiz
        map.putIfAbsent(chatId, new UserEntity(chatId, "user", "START"));
        UserEntity user = map.get(chatId);

        switch (user.getStep()) {
            case "START":
                sendMessage(chatId, "<b>Salom!\nBotdan ro'yhatdan o'tmagansiz\n\n1. Sign Up\n2. Sign In</b>");
                user.setStep("INPUT");
                break;

            case "INPUT":
                sendMessage(chatId, "<b>Ro'yhatdan o'tish\n\nEmailingizni yuboring:</b>");
                user.setStep("EMAIL_INPUT");
                break;

            case "EMAIL_INPUT":
                user.setEmail(text);

                // Random kod yaratish va foydalanuvchiga yuborish
                Random random = new Random();
                int randomInt = 10000 + random.nextInt(90000); // 5 xonali kod
                user.setCode(String.valueOf(randomInt));

                sendMessage(chatId, "<b>Rahmat! \n\nGmail poshtangizni tasdiqlash uchun parol yuborildi:\n\n" + randomInt + "</b>");
                user.setStep("CODE_INPUT");
                break;

            case "CODE_INPUT":
                if (text.equals(user.getCode())) {
                    sendMessage(chatId, "<b>Ma'lumotlar tasdiqlandi! Botni ishlatishingiz mumkin.</b>");
                    user.setStep("FINISH");
                } else {
                    sendMessage(chatId, "<b>Tasdiqlash paroli xato kiritdingiz. Iltimos qayta urinib ko‘ring.</b>");
                }
                break;

            case "FINISH":
                sendMessage(chatId, "<b>Siz allaqachon ro'yxatdan o'tgansiz.</b>");
                break;
        }

        saveUsers();
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        print(sendMessage);
    }

    private void print(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(userPath)) {
            for (UserEntity user : map.values()) {
                // chatId,role,step,email,code
                writer.write(user.getChatId() + "," + user.getRole() + "," + user.getStep()
                        + "," + (user.getEmail() == null ? "" : user.getEmail())
                        + "," + (user.getCode() == null ? "" : user.getCode()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        if (!Files.exists(userPath)) return;

        try (BufferedReader reader = Files.newBufferedReader(userPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Long chatId = Long.parseLong(parts[0]);
                    String role = parts[1];
                    String step = parts[2];
                    String email = parts[3].isEmpty() ? null : parts[3];
                    String code = parts[4].isEmpty() ? null : parts[4];
                    map.put(chatId, new UserEntity(chatId, role, step, email, code));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "@OlxDemo_bot";
    }

    @Override
    public String getBotToken() {
        return "8138276255:AAFzhrbX-MbGD_WrnMJL7oVGmN5K3BDFhlM";
    }
}
