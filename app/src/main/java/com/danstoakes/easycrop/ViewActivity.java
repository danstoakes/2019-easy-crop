package com.danstoakes.easycrop;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class ViewActivity extends AppCompatActivity implements View.OnClickListener {

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        ImageButton shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(this);

        if (getIntent().hasExtra("croppedImage")) {
            imageUri = Uri.parse(getIntent().getStringExtra("croppedImage"));

            ImageView imageView = findViewById(R.id.croppedImage);
            imageView.setImageURI(imageUri);
            imageView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        switch (viewID) {
            case R.id.saveButton: {
                if (requestStoragePermission()) {
                    saveImage();
                }
                break;
            }
            case R.id.shareButton: {
                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Intent.EXTRA_STREAM, imageUri)
                        .setType("image/png");

                startActivity(Intent.createChooser(intent, "Share image via"));
                break;
            }
            case R.id.croppedImage : {
                // Toast.makeText(this, "Selected the image", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    private boolean requestStoragePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                File externalDirectory = Environment.getExternalStorageDirectory();
                File subDirectory = new File(externalDirectory.toString() + "/Pictures/Cropped");

                if (!subDirectory.exists()) {
                    if (!subDirectory.mkdir()) {
                        Toast.makeText(this, "There was a problem finding a storage location for saving.", Toast.LENGTH_LONG).show();
                    }
                }
                saveImage();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Permission denied")
                            .setMessage("Without this permission, EasyCrop is unable to write to storage on this device. This access is required to allow cropped images to be saved.\n\nAre you sure you want to deny this permission?")
                            .setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Re-try", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestStoragePermission();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
                }
            }
        }
    }

    private void saveImage () {
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdCardDirectory.toString() + "/Pictures/Cropped");

        if (!subDirectory.exists()) {
            if (!subDirectory.mkdir()) {
                Toast.makeText(this, "Could not save to camera roll", Toast.LENGTH_LONG).show();
            }
        }

        if (subDirectory.exists()) {
            Date date = new Date();
            try {
                File image = new File(subDirectory, "/image_" + date.getTime() + ".png");
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                OutputStream outputStream = new FileOutputStream(image);
                byte[] bytes = new byte[1024];

                int length;

                try {
                    while ((length = inputStream.read(bytes)) > 0) {
                        outputStream.write(bytes, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MediaScannerConnection.scanFile(this, new String[]{image.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }

                });

                Toast.makeText(this, "Saved to camera roll", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
