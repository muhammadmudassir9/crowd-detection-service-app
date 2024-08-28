package com.example.crowddetectionservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.crowddetectionservice.database.InventoryDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final int PERMISSION_REQUEST_CODE = 3;

    private ImageView imageView1;
    private ImageView imageView3;
    private ImageView imageView5;

    public static String apiBaseUrl = "https://a62d-39-51-209-183.ngrok-free.app/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = findViewById(R.id.imageView1);
        ImageView imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView5 = findViewById(R.id.imageView5);

        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }

    public void takePicture(View view) {
        Toast.makeText(this, "Take Picture clicked", Toast.LENGTH_SHORT).show();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void recordVideo(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void openInventory(View view) {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
    }

    public void openServer(View view) {
        Intent intent = new Intent(MainActivity.this, CrowdListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String date = getCurrentDate();
            String time = getCurrentTime();
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView1.setImageBitmap(imageBitmap);
                showSaveOrSendDialog(imageBitmap, "image", date, time);
            } else if (requestCode == REQUEST_VIDEO_CAPTURE) {
                Uri videoUri = data.getData();
                showSaveOrSendDialog(videoUri, "video", date, time);
            }
        }
    }

    private void showSaveOrSendDialog(final Bitmap imageBitmap, final String type, final String date, final String time) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Save or Send");
        builder.setMessage("Do you want to save the image locally or send it to the server?");
        builder.setPositiveButton("Save", (dialog, which) -> saveImageOrVideoLocally(imageBitmap, type, date, time));
        builder.setNegativeButton("Send", (dialog, which) -> sendImageOrVideoToServer(imageBitmap, type, date, time));
        builder.show();
    }

    private void showSaveOrSendDialog(final Uri videoUri, final String type, final String date, final String time) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Save or Send");
        builder.setMessage("Do you want to save the video locally or send it to the server?");
        builder.setPositiveButton("Save", (dialog, which) -> saveImageOrVideoLocally(videoUri, type, date, time));
        builder.setNegativeButton("Send", (dialog, which) -> sendImageOrVideoToServer(videoUri, type, date, time));
        builder.show();
    }

    private void saveImageOrVideoLocally(Bitmap imageBitmap, String type, String date, String time) {
        String directoryPath = getFilesDir() + "/" + date + "/";
        File file = new File(directoryPath + "image_" + time + ".png"); // Use timestamp in filename
        Objects.requireNonNull(file.getParentFile()).mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            addRecordToInventoryDatabase(file.getAbsolutePath(), type, date, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImageOrVideoLocally(Uri videoUri, String type, String date, String time) {
        String directoryPath = getFilesDir() + "/" + date + "/";
        File file = new File(directoryPath + "video_" + time + ".mp4"); // Use timestamp in filename
        Objects.requireNonNull(file.getParentFile()).mkdirs();
        try (InputStream is = getContentResolver().openInputStream(videoUri);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            addRecordToInventoryDatabase(file.getAbsolutePath(), type, date, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImageOrVideoToServer(Bitmap imageBitmap, String type, String date, String time) {
        File file = new File(getCacheDir(), "image_" + time + ".png"); // Ensure file extension matches
        try (FileOutputStream fos = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(new Date());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/png"))) // PNG MIME type
                .addFormDataPart("date", date)
                .addFormDataPart("time", time)
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/crowd/")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            }
        });
    }

    private void sendImageOrVideoToServer(Uri videoUri, String type, String date, String time) {
        File file = new File(getCacheDir(), "video_" + time + ".mp4"); // Ensure file extension matches
        try (InputStream is = getContentResolver().openInputStream(videoUri);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(new Date());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("video/mp4"))) // MP4 MIME type
                .addFormDataPart("date", date)
                .addFormDataPart("time", time)
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/crowd/")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        if (response.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            }
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void addRecordToInventoryDatabase(String filePath, String fileType, String date, String time) {
        InventoryDatabase inventoryDatabase = new InventoryDatabase(this);
        inventoryDatabase.addRecord(filePath, fileType, date, time);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera functionality
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } else {
                // Permission denied, display error message
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
