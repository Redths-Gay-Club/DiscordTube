package me.rtx4090.events;

import me.rtx4090.Main;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class ReadyEventListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        Main.info("Bot is ready! Logged in as: " + jda.getSelfUser().getName());

        Main.onReady();

    }
}
