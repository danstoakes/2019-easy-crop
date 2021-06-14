package com.danstoakes.easycrop;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity for the application, which is primarily responsible for loading the image to crop.
 */
public class MainActivity extends AppCompatActivity
{
    private static final int ACCESS_EXTERNAL_CONTENT = 1;

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
        setContentView(R.layout.activity_main);
        // define and set the listener for the button which is used for selecting an image
        ImageButton galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Click handler for the button which opens an image selection service.
             * @param v - The view being clicked.
             */
            @Override
            public void onClick(View v)
            {
                // create a new intent for the image selection service
                Intent externalIntent = new Intent(Intent.ACTION_PICK);
                externalIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                // start an activity using the intent result
                startActivityForResult(externalIntent, ACCESS_EXTERNAL_CONTENT);
            }
        });
    }

    /**
     * Callback for when the user completes an action with the image selection service.
     * @param requestCode - the code for the request.
     * @param resultCode - the code for the result of the request.
     * @param data - the data (image) which was selected.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // continue if the result was successful and an image was returned
        if (resultCode == RESULT_OK && data != null)
        {
            // if the code matches the code submitted
            if (requestCode == ACCESS_EXTERNAL_CONTENT)
            {
                // get the image uri from the data object
                Uri imageUri = data.getData();
                if (imageUri != null)
                {
                    // generate a new intent to CropActivity using the image uri
                    Intent cropIntent = new Intent(MainActivity.this, CropActivity.class)
                            .putExtra("imageUri", imageUri.toString());
                    // start the activity
                    startActivity(cropIntent);
                }
            }
        }
    }
}
