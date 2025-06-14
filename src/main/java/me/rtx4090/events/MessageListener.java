package me.rtx4090.events;

import me.rtx4090.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getMessage().getContentRaw().startsWith("!dl")) return;

        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args.length < 3) {
            Main.error("Invalid command format. Use !dl <mp3/mp4> <youtube url>");
            return;
        }

        Main.onComingCommand(event.getAuthor(), args);

    }
}
