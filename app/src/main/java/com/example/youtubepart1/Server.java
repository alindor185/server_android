package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

public class Server {
    private Context context;
    public Server(Context context) {
        this.context = context;
    }

    public JSONObject register(String email, String userName, String password, String imageBase64) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("username", userName);
            jsonObject.put("password", password);
            jsonObject.put("profilePicture", imageBase64);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return post("users", jsonObject.toString());
    }

    /** returns the authorization token too **/
    public JSONObject login(String userName, String password) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", userName);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return post("tokens", jsonObject.toString());
    }

    public JSONObject editLike(String userName) {
        String requestBodyData = "";
        return post("tokens/"+userName+"/posts/:pid/like", requestBodyData);
    }

    public JSONObject getUser(String userName, String token) {
        String requestBodyData = "";
        return getWithAuth("tokens/"+userName, requestBodyData, token);
    }

    public JSONObject createVideo(Video video) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", video.getTitle());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            if (video.getIsFile())
                retriever.setDataSource(context, Uri.fromFile(new File(video.getUrl())));
            else
                retriever.setDataSource(context, Uri.parse(video.getUrl()));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);
            retriever.release();
            jsonObject.put("duration", timeInMillisec/1000);
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("username", video.getUserName());
            jsonObject2.put("profilePicture", "data:image/png;base64,"+video.getProfilePic());
            jsonObject2.put("id", UserToken.id);
            jsonObject.put("uploader", jsonObject2);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        File videoFile = null;
        if (!video.getIsFile()) {
            try {
                Bitmap bitmap = BitmapUtils.getResizedBitmap(ImageOperations.uriToBitmap(context, Uri.parse(video.getUrl())));
                File tmp = new File(context.getExternalFilesDir(null), System.currentTimeMillis()+".jpg");
                FileOutputStream fos = new FileOutputStream(tmp);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                videoFile = tmp;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else
            videoFile = new File(video.getUrl());

        File thumbnailFile = new File(context.getExternalFilesDir(null), System.currentTimeMillis()+"2.jpg");
        try {
            Bitmap bitmap = ImageOperations.base64ToBitmap(video.getThumbnail());
            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return sendFormData("users/"+UserToken.id+"/videos", jsonObject, UserToken.token, videoFile, "videoFile", thumbnailFile, "thumbnail");
    }

    private static JSONObject post(String subURL, String requestBodyData) {
        return sendRequestToServer(subURL, requestBodyData, null, "POST");
    }

    private static JSONObject get(String subURL, String requestBodyData) {
        return sendRequestToServer(subURL, requestBodyData, null, "GET");
    }

    private static JSONObject getWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "GET");
    }

    private static JSONObject postWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "POST");
    }

    private static JSONObject sendRequestToServer(String subURL, String requestBodyData, String authorizationToken, String method) {
        try {
            URL url = new URL("http://10.0.2.2:5000/api/"+subURL);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod(method);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setRequestProperty("charset", "utf-8");
            if (authorizationToken != null)
                httpUrlConnection.setRequestProperty("Authorization", "Bearer "+authorizationToken);
            StringBuilder builder = getStringBuilder(requestBodyData, httpUrlConnection);
            return new JSONObject(builder.toString());
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject sendFormData(String subURL, JSONObject requestBodyData, String authorizationToken, File file1, String name1, File file2, String name2) {
        try {
            final HttpPost httpPost = new HttpPost("http://10.0.2.2:5000/api/" + subURL);
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (Iterator<String> it = requestBodyData.keys(); it.hasNext(); ) {
                String key = it.next();
                builder.addTextBody(key, requestBodyData.get(key).toString());
            }
            builder.addBinaryBody(
                    name1, file1, ContentType.create("video/mp4"), file1.getName());
            builder.addBinaryBody(
                    name2, file2, ContentType.IMAGE_JPEG, file2.getName());
            final HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);
            httpPost.addHeader("Authorization", "Bearer "+authorizationToken);
            CloseableHttpClient client = HttpClients.createDefault();
            try {
                JSONObject response = client.execute(httpPost, new HttpClientResponseHandler<JSONObject>() {
                    @Override
                    public JSONObject handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                        try {
                            return new JSONObject(EntityUtils.toString(response.getEntity()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } finally {
                            response.close();
                        }
                    }
                });
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private static StringBuilder getStringBuilder(String requestBodyData, HttpURLConnection httpUrlConnection) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(httpUrlConnection.getOutputStream());
        dataOutputStream.writeBytes(requestBodyData);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String stringLine;
        while ((stringLine = bufferedReader.readLine()) != null) {
            builder.append(stringLine).append("\n");
        }
        return builder;
    }

    private static void sendFile(HttpURLConnection connection, File file, String name) {
        try {
            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
            writer.append("--").append(Long.toHexString(System.currentTimeMillis())).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\""+name+"\"; filename=\"").append(file.getName()).append("\"").append("\r\n");
            writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getName())).append("\r\n");
            writer.append("Content-Transfer-Encoding: binary").append("\r\n");
            writer.append("\r\n").flush();
            Files.copy(file.toPath(), output);
            output.flush(); // Important before continuing with writer!
            writer.append("\r\n").flush(); // CRLF is important! It indicates end of boundary.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
