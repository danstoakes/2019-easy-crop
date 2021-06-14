package com.danstoakes.easycrop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * The activity responsible for loading the cropped image, as well as providing export functionality.
 */
public class ViewActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String DIRECTORY_PATH = "/Pictures/Cropped";
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;

    private Uri imageUri;

    /**
     * The first method called by the application, which handles setting up the UI, intents,
     * and any listeners which need to be set up.
     * @param savedInstanceState - bundle object which contains previous saved attributes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the view of the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        // get the Uri of the cropped image
        imageUri = getCroppedImageUri();
        if (imageUri != null)
        {
            // load the image to be displayed
            ImageView imageView = findViewById(R.id.croppedImage);
            imageView.setImageURI(imageUri);
        } else
        {
            Toast.makeText(ViewActivity.this,
                    "There was a problem loading the cropped image.", Toast.LENGTH_LONG).show();
        }
        // assign listeners to the save and share buttons
        ImageButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        ImageButton shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(this);
    }

    /**
     * Returns the Uri of the cropped image which was passed via an intent.
     * @return - the Uri of the cropped image.
     */
    private Uri getCroppedImageUri ()
    {
        // return the Uri if it was passed to the Activity
        if (getIntent().hasExtra("croppedImage"))
            return Uri.parse(getIntent().getStringExtra("croppedImage"));

        return null;
    }

    /**
     * Click handler for the application which performs actions depending on the view which was clicked.
     * @param v - the view which was clicked
     */
    @Override
    public void onClick(View v)
    {
        int viewID = v.getId();

        if (viewID == R.id.saveButton)
        {
            // check for save permissions
            checkForPermissions();
        } else if (viewID == R.id.shareButton)
        {
            // start a new share intent and attach the Uri of the image
            Intent intent = new Intent(Intent.ACTION_SEND)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Intent.EXTRA_STREAM, imageUri)
                    .setType("image/png");
            // start the share activity
            startActivity(Intent.createChooser(intent, "Share image via"));
        }
    }

    /**
     * Requests storage permission from the user
     */
    private void requestStoragePermission ()
    {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // request the permission to write to storage
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Checks whether permissions are accepted. If not, permissions are asked for, otherwise the buttons
     * and any saving functionality are disabled.
     */
    private void checkForPermissions()
    {
        int permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_DENIED)
        {
            // if the user has not granted permission
            boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (shouldShowRationale)
            {
                // display a rationale for saving (why the permission is needed)
                StorageRationaleDialog dialog = new StorageRationaleDialog(ViewActivity.this);
                dialog.setOnStorageRationaleOptionSelectedListener(new StorageRationaleDialog.StorageRationaleOptionSelectedListener()
                {
                    /**
                     * Callback method which handles whether a user accepted the permission or not.
                     * @param allow - whether the permission is accepted or not.
                     */
                    @Override
                    public void onStorageRationaleOptionSelected(boolean allow)
                    {
                        // if the user accepts the storage permission in the dialog, request it officially
                        if (allow)
                            requestStoragePermission();
                    }
                });
                dialog.show();
            } else
            {
                // request the permission to write to storage
                requestStoragePermission();
            }
        } else
        {
            // save/share the image as permission already granted
            exportImage();
        }
    }

    /**
     * Callback method which handles how a user responded to a permission request.
     * @param requestCode - the code for the requested permission.
     * @param permissions - the permissions.
     * @param grantResults - the results for each permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if the permission is for writing to storage
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE)
        {
            // if the results are not empty and have been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // save/share the drawing as an image
                exportImage();
            } else
            {
                // disable the buttons and save/share functionality
                findViewById(R.id.saveButton).setEnabled(false);
                findViewById(R.id.saveButton).setAlpha(0.5f);
                findViewById(R.id.shareButton).setEnabled(false);
                findViewById(R.id.shareButton).setAlpha(0.5f);
            }
        }
    }

    /**
     * Exports the canvas drawing as an image.
     */
    private void exportImage ()
    {
        // initialise the directories for saving
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdCardDirectory.toString() + DIRECTORY_PATH);
        // create the directory if it does not exist
        if (!subDirectory.exists())
        {
            if (!subDirectory.mkdir())
                Toast.makeText(this, "Could not save to camera roll", Toast.LENGTH_LONG).show();
        }

        if (subDirectory.exists())
        {
            Date date = new Date();
            try
            {
                // create a new file to save the image into
                File image = new File(subDirectory, "/image_" + date.getTime() + ".png");
                // write the image to the file using an InputStream
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                OutputStream outputStream = new FileOutputStream(image);
                byte[] bytes = new byte[1024];

                int length;
                try
                {
                    // loop through image and write to OutputStream
                    while ((length = inputStream.read(bytes)) > 0)
                        outputStream.write(bytes, 0, length);
                    // close the streams
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                // scan the directory to cache the new file
                MediaScannerConnection.scanFile(this, new String[]{image.toString()}, null, null);
                Toast.makeText(this, "Saved to camera roll", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e)
            {
                Log.w("APP_INFO", "" + e.getMessage());
            }
        }
    }
}