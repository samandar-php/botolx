package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

public class MyBot implements LongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {

        if ( update.hasMessage() ) {
            Message message = update.getMessage();

            if ( message.hasText() ) {
                messageHandler( message );
            }

        }


    }

    private void messageHandler(Message message) {
        String text = message.getText();
        Long id = message.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);

    }
    public BotOptions getOptions() {
        return null;
    }

    @Override
    public void clearWebhook() {

    }

    @Override
    public String getBotUsername() {
        return "Olx bot";
    }

    @Override
    public String getBotToken() {
        return "8221166771:AAHRlr9xfmNZuKx9zJaXPkNII3fZINKFtgo";
    }
}
