package com.danstoakes.easycrop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import androidx.annotation.NonNull;

public class CropTypeDialog extends Dialog implements View.OnClickListener
{
    private CropTypeOptionSelectedListener listener;

    public CropTypeDialog(@NonNull Context context)
    {
        super(context);
        setOwnerActivity ((Activity) context);
        // define the window and set the background as transparent to allow for rounded corners
        Window window = super.getWindow ();
        if (window != null)
            window.setBackgroundDrawable (new ColorDrawable(Color.TRANSPARENT));
        // set the resource to be used for the dialog
        super.setContentView (R.layout.activity_crop_type_dialog);
        super.setCancelable (false);
        // Initialise and set click listeners for the buttons
        ImageButton buttonClassicCrop = findViewById(R.id.crosshairCropButton);
        buttonClassicCrop.setOnClickListener(this);

        ImageButton buttonFreehandCrop = findViewById(R.id.freehandCropButton);
        buttonFreehandCrop.setOnClickListener(this);
    }

    /**
     * Sets the listener for the dialog box.
     * @param listener - the listener for options being selected.
     */
    public void setOnDialogOptionSelectedListener (CropTypeOptionSelectedListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View v)
    {
        int viewID = v.getId();
        // prompt the listener depending on the option selected
        if (viewID == R.id.crosshairCropButton)
        {
            listener.onCropTypeOptionSelected(CropView.CROP_CLASSIC);
            dismiss();
        } else if (viewID == R.id.freehandCropButton)
        {
            // should also display the lasso button
            listener.onCropTypeOptionSelected(CropView.CROP_FREEHAND);
            dismiss();
        }
    }

    /**
     * Interface which handles callbacks when dialog options are selected.
     */
    public interface CropTypeOptionSelectedListener
    {
        void onCropTypeOptionSelected (int cropType);
    }
}
