package com.example.spycamera.Activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spycamera.R;
import com.example.spycamera.Utilities.Helper;
import com.example.spycamera.Utilities.SPHelper;

public class SettingActivity extends AppCompatActivity {

    RelativeLayout settingLay;
    ImageView iv_back;
    TextView txtTitleFloating, titlePhoto;
    LinearLayout videoLay, photoLay;
    EditText etVideoDuration, etPhoto, etPhotoBuffer;
    Button btnSave;
    boolean isVideo = false;
    String cameraPreference = "FRONT";
    String videoPreference = "FRONT";
    String photoChoice = "0";
    RadioButton radioDuration, radioCount, radioFront, radioBack, radioFrontVideo, radioBackVideo;
    RadioGroup radioGroup;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().hide();
        initUI();
    }

    private void initUI() {
        context = this;

        settingLay = findViewById(R.id.settingLay);
        iv_back = findViewById(R.id.iv_back);
        txtTitleFloating = findViewById(R.id.txtTitleFloating);
        videoLay = findViewById(R.id.videoLay);
        photoLay = findViewById(R.id.photoLay);
        etVideoDuration = findViewById(R.id.etVideoDuration);
        etPhoto = findViewById(R.id.etPhoto);
        etPhotoBuffer = findViewById(R.id.etPhotoBuffer);
        titlePhoto = findViewById(R.id.titlePhoto);
        radioGroup = findViewById(R.id.radioGroup);
        btnSave = findViewById(R.id.btnSave);
        radioDuration = findViewById(R.id.radioDuration);
        radioCount = findViewById(R.id.radioCount);
        radioFront = findViewById(R.id.radioFront);
        radioBack = findViewById(R.id.radioBack);
        radioFrontVideo = findViewById(R.id.radioFrontVideo);
        radioBackVideo = findViewById(R.id.radioBackVideo);

        iv_back.setOnClickListener(view -> finish());

        isVideo = getIntent().getBooleanExtra("ISVIDEO", false);

        if (isVideo) {
            videoLay.setVisibility(View.VISIBLE);
            photoLay.setVisibility(View.GONE);
        } else {
            videoLay.setVisibility(View.GONE);
            photoLay.setVisibility(View.VISIBLE);
        }

        radioDuration.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                titlePhoto.setText("Photo Duration");
                etPhoto.setHint("Enter Duration In Minutes");
                photoChoice = "0";
            } else {
                titlePhoto.setText("Photo Count");
                etPhoto.setHint("Enter Photo Count");
                photoChoice = "1";
            }
        });

        radioFront.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                cameraPreference = "FRONT";
            } else {
                cameraPreference = "BACK";
            }
        });

        radioFrontVideo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                videoPreference = "FRONT";
            } else {
                videoPreference = "BACK";
            }
        });
        setData();

        btnSave.setOnClickListener(view -> {
            if (isVideo) {
                saveVideoSetting();
            } else {
                savePhotoSetting();
            }
        });
    }

    private void setData() {
        if (!isVideo) {
            etPhoto.setText(SPHelper.getPhotoChoiceInput(context));
            etPhotoBuffer.setText(SPHelper.getPhotoBuffer(context));
            if (SPHelper.getCameraChoice(context).equals("FRONT")) {
                cameraPreference = "FRONT";
                radioFront.performClick();
            } else {
                cameraPreference = "BACK";
                radioBack.performClick();
            }
            if (SPHelper.getPhotoChoice(context).equals("0")) {
                radioDuration.performClick();
            } else {
                radioCount.performClick();
            }
        } else {
            etVideoDuration.setText(SPHelper.getVideoDuration(context));
            if (SPHelper.getVideoChoice(context).equals("FRONT")) {
                videoPreference = "FRONT";
                radioFrontVideo.performClick();
            } else {
                videoPreference = "BACK";
                radioBackVideo.performClick();
            }
        }
    }

    private void savePhotoSetting() {
        if (isValidate()) {
            SPHelper.addPhotoSettings(context, cameraPreference, photoChoice
                    , etPhoto.getText().toString(), etPhotoBuffer.getText().toString());
            Helper.setSnackBar(settingLay, "Setting Saved Successfully.");
        }
    }

    private void saveVideoSetting() {
        if (isValidateVideo()) {
            SPHelper.addVideoSettings(context, videoPreference, etVideoDuration.getText().toString());
            Helper.setSnackBar(settingLay, "Setting Saved Successfully.");
        }
    }

    private boolean isValidate() {
        if (etPhoto.getText().toString().isEmpty()) {
            Helper.setSnackBar(settingLay, "Enter Photo Count.");
            return false;
        }
        if (etPhotoBuffer.getText().toString().isEmpty()) {
            Helper.setSnackBar(settingLay, "Enter Photo Buffer Time.");
            return false;
        }
        if (Integer.parseInt(etPhoto.getText().toString()) == 0) {
            Helper.setSnackBar(settingLay, "Photo Count Cannot Be Zero.");
            return false;
        }
        if (Integer.parseInt(etPhotoBuffer.getText().toString()) < 10) {
            Helper.setSnackBar(settingLay, "Minimum Buffer Of 10 Second Is Required.");
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidateVideo() {
        if (etVideoDuration.getText().toString().isEmpty()) {
            Helper.setSnackBar(settingLay, "Enter Video Duration.");
            return false;
        }
        if (Integer.parseInt(etVideoDuration.getText().toString()) == 0) {
            Helper.setSnackBar(settingLay, "Video Duration Cannot Be 0.");
            return false;
        } else {
            return true;
        }
    }
}