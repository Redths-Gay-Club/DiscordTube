package me.rtx4090;

import com.github.felipeucelli.javatube.Youtube;
import me.rtx4090.download.Downloader;
import me.rtx4090.events.MessageListener;
import me.rtx4090.events.ReadyEventListener;
import me.rtx4090.item.Item;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;

import static me.rtx4090.Token.TOKEN;
import static me.rtx4090.download.Downloader.downloadPath;

public class Main {
    public static JDA jda;


    public static void main(String[] args) {
        startBot(TOKEN);
    }

    public static void startBot(String botToken) {
        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES
                            , GatewayIntent.GUILD_MESSAGES
                            , GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new ReadyEventListener())
                    .addEventListeners(new MessageListener())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onReady() {

    }

    public static void onComingCommand(User requester, String[] args) {
        info("Received command from " + requester.getName() + ": " + String.join(" ", args));
        Item unverifiedItem = new Item(requester, args[1], args[2]);
        itemVerify(unverifiedItem);
    }


    public static void itemVerify(Item unverifiedItem) {
        // check yt link
        try {
            unverifiedItem.yt = new Youtube(unverifiedItem.url);
            unverifiedItem.title = unverifiedItem.yt.getTitle();
            //yt.checkAvailability(); // Throws if not valid/available
        } catch (Exception e) {
            error("Invalid or unavailable video: " + e.getMessage());
            unverifiedItem.requester.openPrivateChannel().queue(channel -> {
                channel.sendMessage("Your download request for " + unverifiedItem.url + " is invalid or unavailable.").queue();
            });
            return;
        }

        //send to download queue or return error to requester
        info("Command from " + unverifiedItem.requester.getName() + " is valid. Video title: " + unverifiedItem.title);
        Downloader.addUrl(unverifiedItem);
        unverifiedItem.requester.openPrivateChannel().queue(channel -> {
            channel.sendMessage("Your download request for " + unverifiedItem.title + " has been added to the queue.").queue();
        });


    }
    public static void error(String message) {
        System.err.println("Error: " + message);
    }

    public static void info(String message) {
        System.out.println("Info: " + message);
    }

    public static void cleanTemp(Item item) {
        File directory = new File(downloadPath + File.separator + item.id);
        try {
            Files.walk(directory.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + p, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}