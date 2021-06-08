package com.danstoakes.easycrop;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends AppCompatActivity implements View.OnClickListener {

    private Bitmap mBitmap;
    private CropView mCropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mCropView = findViewById(R.id.cropView);

        mCropView.initialise(getDisplayMetrics());

        Uri imageUri;

        if (getIntent().getStringExtra("imageUri") != null) {
            imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));

            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                imageBitmap = Bitmap.createBitmap(
                        imageBitmap, 0, 0,
                        imageBitmap.getWidth(), imageBitmap.getHeight(),
                        rotateBitmap(imageUri), true);

                mBitmap = imageBitmap;

                float ratio = Math.min(
                        (float) getDisplayMetrics().widthPixels / imageBitmap.getWidth(),
                        (float) getDisplayMetrics().heightPixels / imageBitmap.getHeight());

                int width = Math.round(ratio * imageBitmap.getWidth());
                int height = Math.round(ratio * imageBitmap.getHeight());

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true);

                mCropView.setBitmap(scaledBitmap);
            } catch (FileNotFoundException e) {
                Log.w("APP_ERROR", "" + e.getMessage());
            } catch (IOException e) {
                Log.w("APP_ERROR", "" + e.getMessage());
            }
        }

        ImageButton cropButton = findViewById(R.id.cropButton);
        cropButton.setOnClickListener(this);

        ImageButton flipButton = findViewById(R.id.flipButton);
        flipButton.setOnClickListener(this);

        ImageButton rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(this);

        final ImageButton lassoButton = findViewById(R.id.lassoButton);
        lassoButton.setOnClickListener(this);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_dialog);
        dialog.setCancelable(false);
        ImageButton classicCrop = dialog.findViewById(R.id.crosshairCropButton);
        classicCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.setCropType(1);
                dialog.dismiss();
            }
        });
        ImageButton freehandCrop = dialog.findViewById(R.id.freehandCropButton);
        freehandCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.setCropType(2);
                lassoButton.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();

        mCropView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        handleCustomUIElements(View.INVISIBLE);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        handleCustomUIElements(View.VISIBLE);
                        break;
                    }
                }
                mCropView.handleMotion(event);

                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();

        switch (viewID) {
            case R.id.cropButton: {
                switch (mCropView.getCropType()) {
                    case 1: {
                        Bitmap croppedBitmap = mCropView.cropBitmap();

                        Intent editImageActivityIntent = new Intent(CropActivity.this, ViewActivity.class)
                                .putExtra("croppedImage", getURI(croppedBitmap).toString());

                        startActivity(editImageActivityIntent);
                        break;
                    }
                    case 2: {
                        if (mCropView.getPath() != null) {
                            Bitmap bitmap = Bitmap.createBitmap(mCropView.getWidth(), mCropView.getHeight(), mBitmap.getConfig());

                            Canvas canvas = new Canvas(bitmap);

                            Path path = mCropView.getPath();

                            float ratio = Math.min(
                                    (float) getDisplayMetrics().widthPixels / mBitmap.getWidth(),
                                    (float) getDisplayMetrics().heightPixels / mBitmap.getHeight());

                            int width = Math.round(ratio * mBitmap.getWidth());
                            int height = Math.round(ratio * mBitmap.getHeight());

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true);

                            Paint paint = new Paint();
                            canvas.drawPath(path, paint);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                            canvas.drawBitmap(scaledBitmap, 0, 0, paint);

                            Region region = new Region();
                            Region clip = new Region(0, 0, bitmap.getWidth(), bitmap.getHeight());
                            region.setPath(path, clip);
                            Rect bounds = region.getBounds();
                            Bitmap croppedBitmap =
                                    Bitmap.createBitmap(bitmap, bounds.left, bounds.top,
                                            bounds.width(), bounds.height());

                            Intent editImageActivityIntent = new Intent(CropActivity.this, ViewActivity.class)
                                    .putExtra("croppedImage", getURI(croppedBitmap).toString());

                            startActivity(editImageActivityIntent);
                        }
                        break;
                    }
                }
                break;
            }
            case R.id.flipButton: {
                mCropView.clearBitmap();

                int width = mBitmap.getWidth();
                int height = mBitmap.getHeight();

                Bitmap imageBitmap = Bitmap.createBitmap(
                        mBitmap, 0, 0, width, height,
                        getFlipMatrix(width / 2, height / 2, mBitmap), true);

                mBitmap = imageBitmap;

                float ratio = Math.min(
                        (float) getDisplayMetrics().widthPixels / imageBitmap.getWidth(),
                        (float) getDisplayMetrics().heightPixels / imageBitmap.getHeight());

                int scaleWidth = Math.round(imageBitmap.getWidth() * ratio);
                int scaleHeight = Math.round(imageBitmap.getHeight() * ratio);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        imageBitmap, scaleWidth, scaleHeight, true);

                mCropView.setBitmap(scaledBitmap);

                break;
            }
            case R.id.rotateButton: {
                mCropView.clearBitmap();

                Bitmap imageBitmap = Bitmap.createBitmap(
                        mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), getRotateMatrix(), true);

                mBitmap = imageBitmap;

                float ratio = Math.min(
                        (float) getDisplayMetrics().widthPixels / imageBitmap.getWidth(),
                        (float) getDisplayMetrics().heightPixels / imageBitmap.getHeight());

                int width = Math.round(imageBitmap.getWidth() * ratio);
                int height = Math.round(imageBitmap.getHeight() * ratio);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        imageBitmap, width, height, true);

                mCropView.setBitmap(scaledBitmap);

                break;
            }
            case R.id.lassoButton: {
                ImageButton lasso = findViewById(R.id.lassoButton);

                boolean lassoSelected = mCropView.getLassoCrop();

                if (lassoSelected)
                {
                    lasso.setBackgroundResource(R.drawable.button_nofocus);
                } else {
                    lasso.setBackgroundResource(R.drawable.button_focus);
                }
                mCropView.setLassoCrop();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mCropView.getPath() != null) {
            mCropView.clearCanvas();
        } else {
            super.onBackPressed();
        }
    }

    public String createImageFromBitmap (Bitmap bitmap) {
        String fileName = "croppedImage";

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            FileOutputStream fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private Uri getURI(Bitmap image) {
        File imagesFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.danstoakes.fileprovider", file);

        } catch (IOException e) {
            Log.d("tag", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

    private Matrix getRotateMatrix() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        return matrix;
    }

    private Matrix getFlipMatrix(int centreX, int centreY, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        if (bitmap.getHeight() > bitmap.getWidth()) {
            matrix.postScale(-1, 1, centreX, centreY);
        } else {
            matrix.postScale(1, -1, centreX, centreY);
        }

        return matrix;
    }

    private Matrix rotateBitmap(Uri imageUri) {
        int orientation = 0;

        // ExifInterface.TAG_DATETIME_DIGITIZED

        Cursor cursor = this.getContentResolver().query(
                imageUri,
                new String[] {MediaStore.Images.ImageColumns.ORIENTATION},
                null, null, null);

        if (cursor != null) {
            if (cursor.getCount() != 1) {
                orientation = 1;
            } else {
                cursor.moveToFirst();
                orientation = cursor.getInt(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            return matrix;
        }
        return null;
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Log.w("APP_INFO", "" + displayMetrics);

        return displayMetrics;
    }

    private void handleCustomUIElements (int showType) {
        ViewGroup viewGroup = findViewById(R.id.container);

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);

            if (view.getId() != R.id.cropView) {
                if (mCropView.getCropType() != 1 && view.getId() != R.id.lassoButton)
                {
                    view.setVisibility(showType);
                }
            }
        }
    }
}