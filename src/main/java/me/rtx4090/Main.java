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
import static me.rtx4090.download.Downloader.*;

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
        int requesterEntries = (int) urls.stream().filter(i -> i.requester.equals(requester)).count();
        if (requesterEntries > 3) {
            requester.openPrivateChannel().queue(channel -> channel.sendMessage("You have too many requests in the queue. Please wait for them to finish before making new requests.").queue());
            return;
        }
        switch (args[0]) {
            case "!bug":
                String bugList;
                bugList = "Here's a List of Known Bug:\n" +
                        "1. Video downloaded does not come with audio\n";
                String message = "Hello " + requester.getName() + ",\n" +
                        bugList +
                        "If you encounter some new issue, please contact with _rtx on discord.\n" +
                        "We will try to fix it ASAP\n" +
                        "Thank you for your understanding!";
                requester.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                return;
            case "!progress":
                //requester.openPrivateChannel().queue(channel -> channel.sendMessage("*This function is under development*").queue());
                if (args.length != 2) return;
                String id = args[1];
                Item item = urls.stream()
                        .filter(i -> i.id.equals(id))
                        .findFirst()
                        .orElse(null);
                if (item == null) {
                    if (runningItem != null && runningItem.id.equals(id)) {
                        item = runningItem;
                        item.informRequester("Your request is currently being processed. It is at (0/" + urls.size() + ") in the queue.");
                        return;

                    } else {
                        requester.openPrivateChannel().queue(channel -> channel.sendMessage("A request with the provided id was not found.").queue());
                        return;
                    }
                }
                item.informRequester("Your request is at (" + urls.size() + "/" + urls.size() + ") in the queue.");
                return;
            case "!dl":
                if (args.length != 3) return;

                info("Received command from " + requester.getName() + ": " + String.join(" ", args));
                Item unverifiedItem = new Item(requester, args[1], args[2]);
                itemVerify(unverifiedItem);
                return;
        }
    }


    public static void itemVerify(Item unverifiedItem) {
        // check yt link
        try {
            unverifiedItem.yt = new Youtube(unverifiedItem.url);
            unverifiedItem.title = unverifiedItem.yt.getTitle();
            // Throws if not valid/available
        } catch (Exception e) {
            error("Invalid or unavailable video: " + e.getMessage());
            unverifiedItem.informRequester("Your download request for " + unverifiedItem.url + " is invalid or unavailable.");
            return;
        }

        //send to download queue or return error to requester
        info("Command from " + unverifiedItem.requester.getName() + " is valid. Video title: " + unverifiedItem.title);
        addUrl(unverifiedItem);
        unverifiedItem.informRequester("Your download request for " + unverifiedItem.title + " has been added to the queue at (" + urls.size() + "/" + urls.size() + ") with id ```" + unverifiedItem.id + "```\n" +
                "You can check the progress of your download by using the command `!progress <id>`.");
    }
    public static void error(String message) {
        System.err.println("Error: " + message);
    }

    public static void info(String message) {
        System.out.println("Info: " + message);
    }

/*    public static void progress(User requester) {
        Item item = Downloader.urls.stream()
                .filter(item -> item.requester.getId().equals(requester.getId()))
                .toList();
        String message = "Hello " + requester.getName() + ",\n" +
                "The bot is currently under development, so there is no progress to report.\n" +
                "If you have any suggestions or feedback, please let us know!\n" +
                "Thank you for your understanding!";
        requester.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
    }*/

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