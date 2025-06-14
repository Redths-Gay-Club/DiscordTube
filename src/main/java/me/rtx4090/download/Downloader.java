package me.rtx4090.download;

import com.github.felipeucelli.javatube.StreamQuery;
import com.github.felipeucelli.javatube.Youtube;
import me.rtx4090.Main;
import me.rtx4090.item.Item;
import me.rtx4090.upload.Uploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Downloader {
    private static final Queue<Item> urls = new LinkedList<>();
    private static boolean running = false;
    public static final String downloadPath = "E:\\RTXs Lab\\git\\DiscordTube\\temp";

    public static void addUrl(Item item) {
        urls.add(item);
        Main.info("Added " + item.format + " download for " + item.url + " at position " + urls.size());

        startRunning();
    }

    public static void startRunning() {
        if (running) {
            Main.error("Downloader is already running.");
            return;
        }
        new Thread(() -> {
            running = true;
            Main.info("Downloader started, processing " + urls.size() + " items.");
            while (!urls.isEmpty()) {
                Item item = urls.poll();
                new File(downloadPath + "\\" + item.id).mkdir();
                switch (item.format) {
                    case "mp3":
                        downloadMp3(item);
                        break;
                    case "mp4":
                        downloadMp4(item);
                        break;
                }
            }
            running = false;
        }).start();
    }

    public static void downloadMp3(Item item) {
        Main.info(item.title + "/" + item.url + "is now downloading");
        try {
            new Youtube(item.url).streams().getOnlyAudio().download(downloadPath + "\\" + item.id + "\\", "audio");

            File convertedMp3 = mp4ToMp3(new File(downloadPath + "\\" + item.id + "\\" + "audio.mp4"), item);
            convertedMp3.renameTo(new File(downloadPath + "\\" + item.id + "\\" + item.title + ".mp3"));
            item.respondRequester(Path.of(downloadPath + "\\" + item.id + "\\" + item.title + ".mp3"));
        } catch (Exception e) {
            Main.error(e.getMessage());
        }
    }

    public static void downloadMp4(Item item) {
        HashMap<String, String> filters = new HashMap<>();
        filters.put("res", item.res);
        Main.info(item.title + "/" + item.url + "is now downloading");
        try {
            StreamQuery streams = new Youtube(item.url).streams();
            Main.info(String.valueOf(streams));
            streams.filter(filters).getFirst().download(downloadPath + "\\" + item.id + "\\", item.title);
            item.respondRequester(Path.of(downloadPath + "\\" + item.id + "\\", item.title + ".mp4"));
        } catch (Exception e0) {
            Main.error(e0.getMessage());
            try {
                new Youtube(item.url).streams().getHighestResolution().download(downloadPath + "\\" + item.id + "\\", item.title);
                item.respondRequester(Path.of(downloadPath + "\\" + item.id + "\\", item.title + ".mp4"));
            } catch (Exception e1) {
                Main.error(e1.getMessage());
            }
        }

    }

    private static File mp4ToMp3(File mp4File, Item item) {

        String[] command = {"ffmpeg", "-y", "-i", mp4File.getPath(), "-f", "mp3", "-vn", mp4File.getParent() + File.separator + "audio.mp3"};
        ProcessBuilder pb = new ProcessBuilder(command);

        pb.inheritIO(); // Optional: show FFmpeg output in console

        try {
            Process process = pb.start();
            process.waitFor();
            System.out.println("Conversion successful!");
            return (new File(downloadPath + "\\" + item.id + "\\" + "audio.mp3"));

        } catch (Exception e) {
            Main.error("Error during conversion: " + e.getMessage());
            return null;

        }
    }

}
