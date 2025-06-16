package me.rtx4090.upload;

import me.rtx4090.Main;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Uploader {

    public static String uploadFile(File file) { // this class is AI generated
        Main.info("A file is being uploaded: " + file.getName());

        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)  // 注意：欄位名稱一定要是 file
                .build();

        Request request = new Request.Builder()
                .url("https://temp.sh/upload")  // 正確的上傳端點
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string().trim();  // 回傳的就是可分享連結
            } else {
                throw new IOException("Upload failed: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
