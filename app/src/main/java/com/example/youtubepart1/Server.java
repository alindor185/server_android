package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

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
        try {
            return new JSONObject(post("users", jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        try {
            return new JSONObject(post("tokens", jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void editVideo(Video video) {
        patchWithAuth("users/"+video.getUploaderId()+"/videos/"+video.get_id(), getVideoJson(video).toString(), UserToken.token);
    }

    public void deleteVideo(Video video) {
        deleteWithAuth("/users/"+video.getUploaderId()+"/videos/"+video.get_id(), "", UserToken.token);
    }

    public void incrementVideoViews(Video video) {
        patch("/users/"+video.getUploaderId()+"/videos/"+video.get_id()+"/views", "");
    }

    public JSONObject updateProfile(User user) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", user.email);
            jsonObject.put("username", user.userName);
            jsonObject.put("password", BCrypt.hashpw(user.password, BCrypt.gensalt()));
            jsonObject.put("profilePicture", user.image);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            return new JSONObject(patchWithAuth("/users/"+UserToken.id, jsonObject.toString(), UserToken.token));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteProfile(User user) {
        deleteWithAuth("/users/"+UserToken.id, "", UserToken.token);
    }

    public JSONArray getVideos() {
        try {
            String videosString = null;
            while (videosString == null)
                videosString = get("videos", "");
            return new JSONArray(videosString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getVideoJson(Video video) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", video.getTitle());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(new File(video.getFilePath())));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);
            retriever.release();
            jsonObject.put("duration", timeInMillisec/1000);
            List<Video.Comment> comments = video.getCommentsList();
            for (Video.Comment comment : comments)
                comment.setProfilePic("");
            jsonObject.put("comments", new JSONArray(new Gson().toJson(comments)));
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("username", video.getUserName());
            jsonObject2.put("profilePicture", "data:image/png;base64,"+video.getProfilePic());
            jsonObject2.put("id", UserToken.id);
            jsonObject.put("uploader", jsonObject2);
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    public JSONObject createVideo(Video video) {
        File videoFile = new File(video.getFilePath());

        JSONObject jsonObject = getVideoJson(video);

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

    public JSONObject addComment(Video.Comment comment, String videoId) {
        try {
            return new JSONObject(postWithAuth("/videos/"+videoId+"/comments", new Gson().toJson(comment), UserToken.token));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject editComment(Video.Comment comment, String videoId) {
        try {
            return new JSONObject(patchWithAuth("/videos/"+videoId+"/comments/"+comment.get_id(), new Gson().toJson(comment), UserToken.token));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject deleteComment(Video.Comment comment, String videoId) {
        try {
            return new JSONObject(deleteWithAuth("/videos/"+videoId+"/comments/"+comment.get_id(), new Gson().toJson(comment), UserToken.token));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static String post(String subURL, String requestBodyData) {
        return sendRequestToServer(subURL, requestBodyData, null, "POST");
    }

    private static String get(String subURL, String requestBodyData) {
        return sendRequestToServer(subURL, requestBodyData, null, "GET");
    }

    private static String deleteWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "DELETE");
    }

    private static String patch(String subURL, String requestBodyData) {
        return sendRequestToServer(subURL, requestBodyData, null, "PATCH");
    }

    private static String patchWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "PATCH");
    }

    private static String getWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "GET");
    }

    private static String postWithAuth(String subURL, String requestBodyData, String token) {
        return sendRequestToServer(subURL, requestBodyData, token, "POST");
    }

    private static String sendRequestToServer(String subURL, String requestBodyData, String authorizationToken, String method) {
        try {
            URL url = new URL("http://10.0.2.2:5000/api/"+subURL);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod(method);
            if (method.equals("POST") || method.equals("PATCH"))
                httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setRequestProperty("charset", "utf-8");
            if (authorizationToken != null)
                httpUrlConnection.setRequestProperty("Authorization", "Bearer "+authorizationToken);
            if (method.equals("POST") || method.equals("PATCH"))
                writeBodyData(httpUrlConnection, requestBodyData);
            StringBuilder builder = getStringBuilder(httpUrlConnection);
            return builder.toString();
        } catch (IOException e) {
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

    private static void writeBodyData(HttpURLConnection httpUrlConnection, String requestBodyData) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(httpUrlConnection.getOutputStream());
        dataOutputStream.writeBytes(requestBodyData);
    }

    @NonNull
    private static StringBuilder getStringBuilder(HttpURLConnection httpUrlConnection) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String stringLine;
        while ((stringLine = bufferedReader.readLine()) != null) {
            System.out.println(stringLine);
            builder.append(stringLine).append("\n");
        }
        return builder;
    }
}
