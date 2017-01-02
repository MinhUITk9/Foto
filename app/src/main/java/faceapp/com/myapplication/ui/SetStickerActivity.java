package faceapp.com.myapplication.ui;

/**
 * Created by Admin on 12/3/2016.
 */

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import faceapp.com.myapplication.R;
import faceapp.com.myapplication.helper.ImageHelper;
import faceapp.com.myapplication.helper.LogHelper;
import faceapp.com.myapplication.helper.FaceApp;
import faceapp.com.myapplication.log.DetectionLogActivity;

import faceapp.com.myapplication.utils.FileUtils;
import faceapp.com.myapplication.view.BubbleInputDialog;
import faceapp.com.myapplication.view.BubbleTextView;
import faceapp.com.myapplication.view.StickerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetStickerActivity extends AppCompatActivity {
    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The URI of the image selected to detect.
    public Uri mImageUri;
    private Uri imageUri;

    // The image selected to detect.
    private Bitmap mBitmap;
    ProgressDialog mProgressDialog;

    //Bubble input box
    private BubbleInputDialog mBubbleInputDialog;

    //The sticker that is currently in edit mode
    private StickerView mCurrentView;

    //The currently active bubble
    private BubbleTextView mCurrentEditTextView;

    //Stores a list of stickers
    private ArrayList<View> mViews;

    private RelativeLayout mContentRootView;

    private FloatingActionsMenu mMultipleActions;

    private View mAddSticker;

    private View mAddBubble;

    ImageView imagesavesticker;

    public String uri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sticker);
        mContentRootView = (RelativeLayout) findViewById(R.id.rl_content_root);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));

        imagesavesticker=(ImageView)findViewById(R.id.imagesticker);



        mMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        mAddSticker = findViewById(R.id.action_add_sticker);

        mAddSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStickerView();
                mMultipleActions.collapse();
            }
        });

        mViews = new ArrayList<>();
        mBubbleInputDialog = new BubbleInputDialog(this);
        mBubbleInputDialog.setCompleteCallBack(new BubbleInputDialog.CompleteCallBack() {
            @Override
            public void onComplete(View bubbleTextView, String str) {
                ((BubbleTextView) bubbleTextView).setText(str);
            }
        });


    }


    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }



    // Add a log item.
    private void addLog(String log) {
        LogHelper.addDetectionLog(log);
    }
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }
    // Called when image selection is done.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.imagesticker);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        addLog("Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());
                    }
                    // Clear the information panel.
                    setInfo("");
                }
                break;
            default:
                break;
        }
    }

    public void saveToInternalStorageStickerView(View view){
        /*
     * Save bitmap to MediaStore
     */
        Bitmap bitmap = Bitmap.createBitmap(mContentRootView.getWidth(),
                mContentRootView.getHeight()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mContentRootView.draw(canvas);

        String iamgePath = FileUtils.saveBitmapToLocal(bitmap, this);
        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra("image", iamgePath);
        startActivity(intent);



    };



    //Add Emoticons
    private void addStickerView() {
        final StickerView stickerView = new StickerView(this);
        stickerView.setImageResource(R.mipmap.stickerdino);
        stickerView.setOperationListener(new StickerView.OperationListener() {
            @Override
            public void onDeleteClick() {
                mViews.remove(stickerView);
                mContentRootView.removeView(stickerView);
            }

            @Override
            public void onEdit(StickerView stickerView) {
                if (mCurrentEditTextView != null) {
                    mCurrentEditTextView.setInEdit(false);
                }
                mCurrentView.setInEdit(false);
                mCurrentView = stickerView;
                mCurrentView.setInEdit(true);
            }

            @Override
            public void onTop(StickerView stickerView) {
                int position = mViews.indexOf(stickerView);
                if (position == mViews.size() - 1) {
                    return;
                }
                StickerView stickerTemp = (StickerView) mViews.remove(position);
                mViews.add(mViews.size(), stickerTemp);
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mContentRootView.addView(stickerView, lp);
        mViews.add(stickerView);
        setCurrentEdit(stickerView);
    }
    /**
     *
     Sets the sticker that is currently in edit mode
     */
    private void setCurrentEdit(StickerView stickerView) {
        if (mCurrentView != null) {
            mCurrentView.setInEdit(false);
        }
        if (mCurrentEditTextView != null) {
            mCurrentEditTextView.setInEdit(false);
        }
        mCurrentView = stickerView;
        stickerView.setInEdit(true);
    }


}
