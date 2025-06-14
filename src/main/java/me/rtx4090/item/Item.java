package me.rtx4090.item;

import com.github.felipeucelli.javatube.Youtube;
import me.rtx4090.Main;
import me.rtx4090.upload.Uploader;
import net.dv8tion.jda.api.entities.User;

import java.nio.file.Path;
import java.util.UUID;

public class Item {
    public User requester;
    public String format;
    public String url;
    public String res = "1080p";
    public String fps = "60";
    public String id = UUID.randomUUID().toString();
    public Youtube yt;
    public String title;

    public Item(User requester, String format, String url) {
        this.requester = requester;
        this.format = format;
        this.url = url;
    }

    public void respondRequester(Path filepath) {
        String url = Uploader.uploadFile(filepath.toFile());
        if (url == null) {
            informRequester("Failed to upload file.");
            return;
        }
        informRequester("File uploaded: \n" + url);
        Main.cleanTemp(this);
    }

    public void informRequester(String message) {
        requester.openPrivateChannel().queue(channel -> channel.sendMessage(message).queue());
        Main.info("Informed " + requester.getName() + ": " + message);
    }
}
