package me.rtx4090.download;

import com.github.felipeucelli.javatube.Youtube;
import me.rtx4090.Main;
import me.rtx4090.item.Item;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Downloader {
    public static final Queue<Item> urls = new LinkedList<>();
    private static boolean running = false;
    public static Item runningItem = null;
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
                runningItem = item;
                new File(downloadPath + "\\" + item.id).mkdir();
                switch (item.format) {
                    case "mp3":
                        downloadAudio(item);
                        File convertedMp3 = mp4ToMp3(new File(downloadPath + "\\" + item.id + "\\" + "audio.mp4"), item);
                        item.respondRequester(convertedMp3.toPath());
                        break;
                    case "mp4":
                        downloadVideo(item);
                        downloadAudio(item);
                        File audioOnly = new File(downloadPath + "\\" + item.id + "\\" + "audio.mp4");
                        File videoOnly = new File(downloadPath + "\\" + item.id + "\\" + "video.mp4");
                        File mergedMp4 = mp4Merging(audioOnly, videoOnly, item);
                        item.respondRequester(mergedMp4.toPath());
                        break;
                }
            }
            running = false;
            runningItem = null;
        }).start();
    }

    public static void premiumDl(Item item) {
        new Thread(() -> {
            Main.info("A premium downloader started, processing " + urls.size() + " items.");
            while (!urls.isEmpty()) {
                new File(downloadPath + "\\" + item.id).mkdir();
                switch (item.format) {
                    case "mp3":
                        downloadAudio(item);
                        File convertedMp3 = mp4ToMp3(new File(downloadPath + "\\" + item.id + "\\" + "audio.mp4"), item);
                        item.respondRequester(convertedMp3.toPath());
                        break;
                    case "mp4":
                        downloadVideo(item);
                        downloadAudio(item);
                        File audioOnly = new File(downloadPath + "\\" + item.id + "\\" + "audio.mp4");
                        File videoOnly = new File(downloadPath + "\\" + item.id + "\\" + "video.mp4");
                        File mergedMp4 = mp4Merging(audioOnly, videoOnly, item);
                        item.respondRequester(mergedMp4.toPath());
                        break;
                }
            }
        }).start();
    }

    public static void downloadVideo(Item item) {
        Main.info("Video " + item.title + "/" + item.url + " is now downloading");
        HashMap<String, String> filters = new HashMap<>();
        filters.put("onlyVideo", "true");
        try {
            filters.put("res", item.res);
            new Youtube(item.url).streams().filter(filters).getFirst().download(downloadPath + "\\" + item.id + "\\", "video");
        } catch (Exception e1) {
            // handle exception when no 1080p available
            Main.error("Error occurred at video try one: " + e1.getMessage());
            try {
                new Youtube(item.url).streams().getHighestResolution().download(downloadPath + "\\" + item.id + "\\", "video");
            } catch (Exception e2) {
                Main.error("Error occurred at video try two: " + e2.getMessage());
                item.informRequester("Your download request for " + item.title + " failed. ");
            }
        }

    }

    public static void downloadAudio(Item item) {
        Main.info("Audio of" + item.title + "/" + item.url + "is now downloading");
        HashMap<String, String> filters = new HashMap<>();
        filters.put("onlyAudio", "true");
        try {
            new Youtube(item.url).streams().filter(filters).getOnlyAudio().download(downloadPath + "\\" + item.id + "\\", "audio");
        } catch (Exception e) {
            Main.error("Error occurred at audio try one: " + e.getMessage());
            item.informRequester("Your download request for " + item.title + " failed. ");
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

    private static File mp4Merging(File audioOnly, File videoOnly, Item item) {

        //String[] command = {"ffmpeg", "-y", "-i", mp4File.getPath(), "-f", "mp3", "-vn", mp4File.getParent() + File.separator + "audio.mp3"};
        String[] command = {"ffmpeg", "-i", videoOnly.getPath(), "-i", audioOnly.getPath(), "-c", "copy", "-map", "0:v:0", "-map", "1:a:0", audioOnly.getParent() + File.separator + "cvvideo.mp4"};
        ProcessBuilder pb = new ProcessBuilder(command);

        pb.inheritIO(); // Optional: show FFmpeg output in console

        try {
            Process process = pb.start();
            process.waitFor();
            System.out.println("Conversion successful!");
            return (new File(downloadPath + "\\" + item.id + "\\" + "cvvideo.mp4"));
        } catch (Exception e) {
            Main.error("Error during conversion: " + e.getMessage());
            return null;
        }
    }
}
