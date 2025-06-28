package me.rtx4090;

import com.github.felipeucelli.javatube.Youtube;
import me.rtx4090.developer.Developers;
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
import java.util.Arrays;
import java.util.Comparator;

import static me.rtx4090.Token.TOKEN;
import static me.rtx4090.download.Downloader.*;

public class Main {
    public static JDA jda;
    public static Developers developers;


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
        developers = new Developers(jda);

        Main.info("Bot is ready! Logged in as: " + jda.getSelfUser().getName());
    }

    public static void onComingCommand(User requester, String[] args) {
        String id;
        switch (args[0]) {
            case "!bug":
                String bugList;
                bugList =
                        "```0.No bug >:)\n```";
                String message = "Hello " + requester.getName() + ",\n" +
                        "Here's a List of Known Bug:\n" +
                        bugList +
                        "If you encounter some new issue, please contact with _rtx on discord.\n" +
                        "We will try to fix it ASAP\n" +
                        "Thank you for your understanding!";
                requester.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
                return;
            case "!progress":
                //requester.openPrivateChannel().queue(channel -> channel.sendMessage("*This function is under development*").queue());
                if (args.length != 2) return;
                id = args[1];
                Item requestedItem = urls.stream()
                        .filter(i -> i.id.equals(id))
                        .findFirst()
                        .orElse(null);
                if (requestedItem == null) {
                    if (runningItem != null && runningItem.id.equals(id)) {
                        requestedItem = runningItem;
                        requestedItem.informRequester("Your request is currently being processed. It is at (0/" + urls.size() + ") in the queue.");
                        return;

                    } else {
                        requester.openPrivateChannel().queue(channel -> channel.sendMessage("A request with the provided id was not found.").queue());
                        return;
                    }
                }
                requestedItem.informRequester("Your request is at (" + urls.size() + "/" + urls.size() + ") in the queue.");
                return;
            case "!dl":
                if (args.length != 3) return;
                // check if requester have added more than 3 dls in queue
                int requesterEntries = (int) urls.stream().filter(i -> i.requester.equals(requester)).count();
                if (requesterEntries > 3) {
                    requester.openPrivateChannel().queue(channel -> channel.sendMessage("You have too many requests in the queue. Please wait for them to finish before making new requests.").queue());
                    return;
                }

                info("Received command from " + requester.getName() + ": " + String.join(" ", args));
                Item unverifiedItem = new Item(requester, args[1], args[2]);
                itemVerify(unverifiedItem);
                return;
            case "!premium":
                if (args.length != 2) return;
                if (!requester.equals(developers.getOwner())) return; // Only the owner can use this command

                id = args[1];
                Item requestedPItem = urls.stream()
                        .filter(i -> i.id.equals(id))
                        .findFirst()
                        .orElse(null);
                if (requestedPItem == null) {
                    if (runningItem != null && runningItem.id.equals(id)) {
                        requestedPItem = runningItem;
                        requestedPItem.informRequester("Your request is already at (0/" + urls.size() + ") in the queue.");
                        return;

                    } else {
                        requester.openPrivateChannel().queue(channel -> channel.sendMessage("A request with the provided id was not found.").queue());
                        return;
                    }
                }
                Downloader.premiumDl(requestedPItem);
                return;

            case "!help":
                String helpMessage = "Hello " + requester.getName() + ",\n" +
                        "Here is the basic use of the bot:\n" +
                        "`!dl <mp3/mp4> <YouTube url>` - Download a audio or video from YouTube.\n" +
                        "`!progress <id>` - Check current progress for your download using a provided id.\n" +
                        "`!bug` - Check current bug and the contact information to report further issues.";

                requester.openPrivateChannel().queue(channel -> channel.sendMessage(helpMessage).queue());
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
        // check if video is too long
        if (unverifiedItem.format.equalsIgnoreCase("mp4")) {
            try {
                int duration = unverifiedItem.yt.length().intValue();
                if (duration > 9000) { // 2.5 hour in seconds
                    error("Video is too long: over 2 hours");
                    unverifiedItem.informRequester("Your download request for " + unverifiedItem.title + " is too long. Please choose a video shorter than 2 hour.");
                    return;
                }
            } catch (Exception e) {
                error("Failed to retrieve video duration: " + e.getMessage());
                unverifiedItem.informRequester("Your download request for " + unverifiedItem.title + " failed. Unable to retrieve video duration. Please try !bug and report this to the developer.");
                return;
            }
        }




        //send to download queue or return error to requester
        info("Video from " + unverifiedItem.requester.getName() + " is valid. Video title: " + unverifiedItem.title);
        addUrl(unverifiedItem);
        unverifiedItem.informRequester("Your download request for " + unverifiedItem.title + " has been added to the queue at (" + urls.size() + "/" + urls.size() + ") with id ```" + unverifiedItem.id + "```\n" +
                "You can check the progress of your download by using the command `!progress <id>`.");
    }
    public static void error(String message) {
        System.err.println("Error: " + message);

        Arrays.stream(developers.getAcceptNotification()).forEach(user -> {
            user.openPrivateChannel().queue(channel -> channel.sendMessage("_**Developer Error: " + message + "**_").queue());
        });
    }

    public static void info(String message) {
        System.out.println("Info: " + message);

        Arrays.stream(developers.getAcceptNotification()).forEach(user -> {
            user.openPrivateChannel().queue(channel -> channel.sendMessage("_Developer Info: " + message + "_").queue());
        });
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