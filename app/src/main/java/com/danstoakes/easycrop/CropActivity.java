package com.danstoakes.easycrop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The main activity for the cropping aspect of the application.
 */
public class CropActivity extends AppCompatActivity implements View.OnClickListener
{
    private CropView cropView;

    /**
     * The first method called by the class, which handles setting up the UI, touch events,
     * and any listeners which need to be set up.
     * @param savedInstanceState - bundle object which contains previous saved attributes.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the view of the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        // set the fullscreen layout flags
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // initialise a BitmapHandler object
        BitmapHandler bitmapHandler = new BitmapHandler();
        // locate, initialise, and set up listeners for the main CropView
        cropView = findViewById(R.id.cropView);
        cropView.initialise(getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels, bitmapHandler);
        cropView.setOnTouchListener(new View.OnTouchListener()
        {
            /**
             * Touch handler for the crop view. Determines if a touch is valid
             * and hides the UI/draws crop elements if so.
             * @param v - the View object being touched.
             * @param event - the event being performed through the touch.
             * @return boolean - whether or not the event is consumed.
             */
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // if the user doesn't start touching at an edge (for gesture navigation)
                if (event.getX() > 50 && event.getX() < (getDisplayMetrics().widthPixels - 50))
                {
                    switch (event.getAction())
                    {
                        // hide or show the UI elements depending on whether the uses touches down or not
                        case MotionEvent.ACTION_DOWN:
                            handleUIElements(View.INVISIBLE);
                            break;
                        case MotionEvent.ACTION_UP:
                            handleUIElements(View.VISIBLE);
                            break;
                    }
                    // handle any cropping actions in the CropView object
                    cropView.handleMotion(event);
                    return true;
                }
                return false;
            }
        });
        // get the bitmap from the image which was passed as an uri from MainActivity
        Bitmap imageBitmap = getBitmapFromImage();
        if (imageBitmap != null)
        {
            // set the bitmap in its raw/unmoved form in BitmapHandler
            bitmapHandler.setUnscaledBitmap(imageBitmap);
            // scale the bitmap so that it can be centered/represented on the canvas
            Bitmap scaledBitmap = BitmapHandler.getAsScaledBitmap(
                    imageBitmap, getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels);
            cropView.setBitmap(scaledBitmap);
        } else
        {
            Toast.makeText(CropActivity.this,
                    "There was a problem loading the selected image.", Toast.LENGTH_LONG).show();
        }
        // set the click listeners for the UI buttons
        ImageButton cropButton = findViewById(R.id.cropButton);
        cropButton.setOnClickListener(this);

        ImageButton flipButton = findViewById(R.id.flipButton);
        flipButton.setOnClickListener(this);

        ImageButton rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(this);

        ImageButton lassoButton = findViewById(R.id.lassoButton);
        lassoButton.setOnClickListener(this);
        // load and display a dialog prompting the user to choose a crop method
        CropTypeDialog dialog = new CropTypeDialog(CropActivity.this);
        dialog.setOnDialogOptionSelectedListener(new CropTypeDialog.CropTypeOptionSelectedListener ()
        {
            /**
             * Callback method which sets the current crop method.
             * @param cropType - the crop type/method chosen by the user.
             */
            @Override
            public void onCropTypeOptionSelected(int cropType)
            {
                // set the crop type
                cropView.setCropType(cropType);
                // switch between Freehand and Lasso crop
                if (cropType == CropView.CROP_FREEHAND)
                    lassoButton.setVisibility(View.VISIBLE);
            }
        });
        dialog.show();
    }

    /**
     * Hides or displays the UI buttons within the application depending on the input argument.
     * @param showType - whether to display or hide the view elements.
     */
    private void handleUIElements (int showType)
    {
        // get the view elements as a ViewGroup
        ViewGroup viewGroup = findViewById(R.id.container);
        // loop through each element
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            View view = viewGroup.getChildAt(i);
            // if the view is not the main CropView object
            if (view.getId() != R.id.cropView)
            {
                // if the user is not using the classic crop
                if (cropView.getCropType() != 1 && view.getId() != R.id.lassoButton)
                    view.setVisibility(showType);
            }
        }
    }

    /**
     * Returns the rotation matrix for an input image.
     * @param uri - the uri of the input image.
     * @return Matrix - the rotation matrix for the image.
     */
    private Matrix getRotationMatrix (Uri uri)
    {
        int orientation = 0;
        // initialise a cursor object with the desired flag
        Cursor cursor = this.getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.ORIENTATION},
                null, null, null);
        if (cursor != null)
        {
            if (cursor.getCount() != 1)
            {
                // orientation doesn't change
                orientation = 1;
            } else
            {
                // image orientation needs to change
                cursor.moveToFirst();
                orientation = cursor.getInt(0);
            }
            cursor.close();
        }

        if (orientation > 0)
        {
            // create a matrix object and apply the rotation
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            return matrix;
        }
        return null;
    }

    /**
     * Returns a Bitmap representation of an input image (via an intent) using its uri.
     * @return Bitmap - the representation of the image.
     */
    private Bitmap getBitmapFromImage()
    {
        // if the intent is not null, i.e., an image was sent
        if (getIntent().getStringExtra("imageUri") != null)
        {
            try
            {
                // get the uri of the image and create a bitmap using it
                Uri uri = Uri.parse(getIntent().getStringExtra("imageUri"));
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                // rotate the bitmap if necessary and return it
                imageBitmap = Bitmap.createBitmap(
                        imageBitmap, 0, 0,
                        imageBitmap.getWidth(), imageBitmap.getHeight(),
                        getRotationMatrix(uri), true);
                return imageBitmap;
            } catch(IOException e)
            {
                Log.w("APP_ERROR", "There was an error retrieving the bitmap from the input image.");
            }
        }
        return null;
    }

    /**
     * Returns the DisplayMetrics for the application, i.e., the width and height.
     * @return DisplayMetrics - the metrics for the application.
     */
    private DisplayMetrics getDisplayMetrics()
    {
        return getResources().getDisplayMetrics();
    }

    /**
     * Returns the Uri within storage for a Bitmap object.
     * @param image - the bitmap to retrieve the Uri for.
     * @return Uri - the Uri of the bitmap.
     */
    private Uri getUri(Bitmap image)
    {
        // access the cache image folder
        File imagesFolder = new File(getCacheDir(), "images");
        try
        {
            // check that the directory exists/create it
            boolean created = false;
            if (!imagesFolder.exists())
                created = imagesFolder.mkdirs();
            // if the directory is valid
            if (imagesFolder.exists() || created)
            {
                // create a temporary file
                File file = new File(imagesFolder, "shared_image.png");
                // write the file to storage
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                // retrieve the Uri from the file written to temporary storage
                return FileProvider.getUriForFile(this, "com.danstoakes.fileprovider", file);
            }
        } catch (IOException e)
        {
            Log.d("tag", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return null;
    }

    /**
     * Click handler for the application which performs actions depending on the view which was clicked.
     * @param v - the view which was clicked
     */
    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        if (viewID == R.id.cropButton)
        {
            // crop the image using the crop method selected by the user
            Bitmap croppedBitmap;
            if (cropView.getCropType() == CropView.CROP_CLASSIC)
            {
                croppedBitmap = cropView.cropBitmap();
            } else
            {
                croppedBitmap = cropView.cropBitmapFreehand(
                        getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels);
            }
            // ensure that the cropped image is not erroneous
            if (croppedBitmap == null)
            {
                Toast.makeText(CropActivity.this, "Please select an area to crop", Toast.LENGTH_LONG).show();
            } else
            {
                // generate the uri for the bitmap so that it can be transferred to ViewActivity
                Uri imageUri = getUri(croppedBitmap);
                if (imageUri != null)
                {
                    // create a new intent with the Uri of the cropped image
                    Intent editImageActivityIntent = new Intent(CropActivity.this, ViewActivity.class)
                            .putExtra("croppedImage", imageUri.toString());
                    startActivity(editImageActivityIntent);
                }
            }
        } else if (viewID == R.id.rotateButton)
        {
            // rotate the bitmap 90 degrees clockwise
            cropView.rotateBitmap(getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels);
        } else if (viewID == R.id.flipButton)
        {
            // mirror the bitmap in the x/y axis depending on the photo layout
            cropView.flipBitmap(getDisplayMetrics().widthPixels, getDisplayMetrics().heightPixels);
        } else if (viewID == R.id.lassoButton)
        {
            // alter the UI button to represent whether the lasso is selected or not
            int backgroundResourceId = R.drawable.button_focus;
            if (cropView.hasLassoCropActive())
                backgroundResourceId = R.drawable.button_nofocus;
            // set the selected/unselected colour and set crop type to lasso.
            ImageButton lasso = findViewById(R.id.lassoButton);
            lasso.setBackgroundResource(backgroundResourceId);
            cropView.setCropType(CropView.CROP_LASSO);
            cropView.clearCanvas();
        }
    }

    /**
     * Handles the actions performed if the user presses the back button/gestures back.
     */
    @Override
    public void onBackPressed()
    {
        // if a crop outline has been drawn, clear the canvas
        if (cropView.hasPath())
        {
            cropView.clearCanvas();
        } else
        {
            // perform normal operations, i.e., move back to MainActivity
            super.onBackPressed();
        }
    }
}