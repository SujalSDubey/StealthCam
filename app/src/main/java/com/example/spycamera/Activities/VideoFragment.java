package com.example.spycamera.Activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
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
import com.example.spycamera.Adaptors.VideoAdapter;
import com.example.spycamera.R;

import java.io.File;
import java.util.ArrayList;

public class VideoFragment extends Fragment {

    RelativeLayout videoLay;
    RecyclerView videoRc;
    TextView txtNoData;
    File[] allFiles;

    private File videoFolder;
    private VideoAdapter videoAdapter;
    VideoAdapter.RefreshList refreshList = () -> setAdaptor();

    public VideoFragment(File videoFolder) {
        this.videoFolder = videoFolder;
    }

    public VideoFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_video, container, false);
        initUI(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    private void initUI(View root) {
        videoLay = root.findViewById(R.id.videoLay);
        videoRc = root.findViewById(R.id.videoRc);
        txtNoData = root.findViewById(R.id.txtNoData);
        setAdaptor();
    }

    public void refreshAdapter() {
        if (videoAdapter != null) {
            if (videoFolder == null) {
                ContextWrapper wrapper = new ContextWrapper(getContext());
                videoFolder = wrapper.getDir("Videos", Context.MODE_PRIVATE);
            }
            allFiles = videoFolder.listFiles((dir, name) -> (name.endsWith(".mp4")));
            videoAdapter.setValues(allFiles);
        }
    }

    private void setAdaptor() {
        allFiles = null;
        if (videoFolder == null) {
            ContextWrapper wrapper = new ContextWrapper(getContext());
            videoFolder = wrapper.getDir("Videos", Context.MODE_PRIVATE);
        }
        allFiles = videoFolder.listFiles((dir, name) -> (name.endsWith(".mp4")));

        if (allFiles != null && allFiles.length != 0) {
            txtNoData.setVisibility(View.GONE);
            videoRc.setVisibility(View.VISIBLE);
            videoAdapter = new VideoAdapter(allFiles, getContext(), refreshList);
            videoRc.setHasFixedSize(true);
            videoRc.setLayoutManager(new GridLayoutManager(getContext(), 2));
            videoRc.setAdapter(videoAdapter);
        } else {
            txtNoData.setVisibility(View.VISIBLE);
            videoRc.setVisibility(View.GONE);
        }
    }
}