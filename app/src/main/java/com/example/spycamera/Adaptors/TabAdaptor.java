package com.example.spycamera.Adaptors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.spycamera.Activities.PhotoFragment;
import com.example.spycamera.Activities.VideoFragment;

import java.io.File;

public class TabAdaptor extends FragmentPagerAdapter {

    int totalTabs;
    File imageFolder, videoFolder;

    public TabAdaptor(FragmentManager fm, int totalTabs, File imageFolder, File videoFolder) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.totalTabs = totalTabs;
        this.imageFolder = imageFolder;
        this.videoFolder = videoFolder;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new VideoFragment(videoFolder);
        }
        return new PhotoFragment(imageFolder);
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
