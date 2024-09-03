package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageOperations {
    public static Bitmap base64ToBitmap(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapUtils.getResizedBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        bitmap = BitmapUtils.getResizedBitmap(bitmap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap resourceToBitmap(Context context, int resource) {
        return BitmapUtils.getResizedBitmap(BitmapFactory.decodeResource(context.getResources(), resource));
    }

    public static Bitmap uriToBitmap(Context context, Uri uri) throws IOException {
        return BitmapUtils.getResizedBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri));
    }
}
