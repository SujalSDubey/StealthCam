package com.example.spycamera.Activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spycamera.Adaptors.PhotoAdapter;
import com.example.spycamera.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PhotoFragment extends Fragment {

    File[] allFiles;
    RelativeLayout photoLay;
    RecyclerView photoRc;
    TextView txtNoData;

    private File imageFolder;
    private PhotoAdapter photoAdapter;
    PhotoAdapter.RefreshList refreshList = () -> setAdaptor();

    public PhotoFragment(File imageFolder) {
        this.imageFolder = imageFolder;
    }

    public PhotoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_photo, container, false);
        initUI(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    private void initUI(View root) {
        photoLay = root.findViewById(R.id.photoLay);
        photoRc = root.findViewById(R.id.photoRc);
        txtNoData = root.findViewById(R.id.txtNoData);
        setAdaptor();
    }

    public void refreshAdapter() {
        if (photoAdapter != null) {
            ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
            if (imageFolder == null) {
                ContextWrapper wrapper = new ContextWrapper(getContext());
                imageFolder = wrapper.getDir("Images", Context.MODE_PRIVATE);
            }
            allFiles = imageFolder.listFiles((dir, name) -> (name.endsWith(".jpg")
                    || name.endsWith(".jpeg") || name.endsWith(".png")));
            if (allFiles != null && allFiles.length != 0) {
                List<File> fileList = new ArrayList<>();
                for (File allFile : allFiles) {
                    Bitmap resizedBitmap = getResizedBitmap(allFile);
                    if (resizedBitmap != null) {
                        bitmapArrayList.add(resizedBitmap);
                        fileList.add(allFile);
                    }
                }
                allFiles = fileList.toArray(new File[0]);
            } else {
                txtNoData.setVisibility(View.VISIBLE);
                photoRc.setVisibility(View.GONE);
            }
            photoAdapter.setValues(allFiles, bitmapArrayList);
        }
    }

    private void setAdaptor() {
        ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
        if (imageFolder == null) {
            ContextWrapper wrapper = new ContextWrapper(getContext());
            imageFolder = wrapper.getDir("Images", Context.MODE_PRIVATE);
        }
        allFiles = null;
        bitmapArrayList.clear();
        allFiles = imageFolder.listFiles((dir, name) -> (name.endsWith(".jpg")
                || name.endsWith(".jpeg") || name.endsWith(".png")));


        if (allFiles != null && allFiles.length != 0) {
            List<File> fileList = new ArrayList<>();
            for (File allFile : allFiles) {
                Bitmap resizedBitmap = getResizedBitmap(allFile);
                if (resizedBitmap != null) {
                    bitmapArrayList.add(resizedBitmap);
                    fileList.add(allFile);
                }
            }
            allFiles = fileList.toArray(new File[0]);
            txtNoData.setVisibility(View.GONE);
            photoRc.setVisibility(View.VISIBLE);
            photoAdapter = new PhotoAdapter(allFiles, bitmapArrayList, getContext(), refreshList);
            photoRc.setHasFixedSize(true);
            photoRc.setLayoutManager(new GridLayoutManager(getContext(), 2));
            photoRc.setAdapter(photoAdapter);
        } else {
            txtNoData.setVisibility(View.VISIBLE);
            photoRc.setVisibility(View.GONE);
        }
    }

    public Bitmap getResizedBitmap(File file) {
        Bitmap resizedBitmap = null;
        try {

            Bitmap bm = BitmapFactory.decodeFile(String.valueOf(file));
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) 500) / width;
            float scaleHeight = ((float) 500) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            resizedBitmap = Bitmap.createBitmap(
                    bm, 0, 0, width, height, matrix, false);
            bm.recycle();
        } catch (Exception e) {
            resizedBitmap = BitmapFactory.decodeFile(String.valueOf(file));
        }
        return resizedBitmap;
    }

}