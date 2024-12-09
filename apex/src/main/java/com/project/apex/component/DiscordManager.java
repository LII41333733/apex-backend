package com.project.apex.component;

import jakarta.annotation.PostConstruct;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordManager {

    @Value("${discord.token}")
    public String discordToken;

    @PostConstruct
    public void login() {
        DiscordApi api = new DiscordApiBuilder()
                .setToken(discordToken)
                .addIntents(Intent.MESSAGE_CONTENT)
                .login().join();

        api.addMessageCreateListener(event -> {
                System.out.println(event.getMessageContent());
                System.out.println(event.getMessage().getMentionedRoles());
            if (event.getMessageContent().contains("@Traders")) {
                System.out.println(event.getMessageContent());
//                event.getChannel().sendMessage("Pong!");
            }
        });
    }
}
