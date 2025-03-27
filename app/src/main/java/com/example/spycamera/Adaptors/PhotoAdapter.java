package com.example.spycamera.Adaptors;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spycamera.R;
import com.example.spycamera.Utilities.Helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private final Context context;
    private File[] fileArrayList;
    ArrayList<Bitmap> bitmapArrayList;
    RefreshList list;

    public PhotoAdapter(File[] fileArrayList, ArrayList<Bitmap> bitmapArrayList, Context context, RefreshList list) {
        this.fileArrayList = fileArrayList;
        this.bitmapArrayList = bitmapArrayList;
        this.context = context;
        this.list = list;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setValues(File[] fileArrayList, ArrayList<Bitmap> bitmapArrayList) {
        try {
            if (fileArrayList != null) {
                this.fileArrayList = fileArrayList;
                if (bitmapArrayList != null) {
                    this.bitmapArrayList = bitmapArrayList;
                }
                notifyDataSetChanged();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @NonNull
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.photo_content, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolder holder, int position) {
        try {
            if (bitmapArrayList != null && bitmapArrayList.size() > position) {
                holder.img.setImageBitmap(bitmapArrayList.get(position));
            }
            holder.img.setOnClickListener(view -> viewGallery(fileArrayList[position]));
            holder.img_delete.setOnClickListener(view -> {
                Helper.showProgressDialog(context);
                deleteImage(fileArrayList[position]);
            });
            holder.img_share.setOnClickListener(view -> {
                Helper.shareFile(context, fileArrayList[position]);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return fileArrayList.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img, img_delete, img_share;

        public ViewHolder(View view) {
            super(view);
            this.img = view.findViewById(R.id.img);
            this.img_delete = view.findViewById(R.id.img_delete);
            this.img_share = view.findViewById(R.id.img_share);
        }
    }

    private void viewGallery(File file) {

        Uri mImageCaptureUri = FileProvider.getUriForFile(
                context,
                context.getApplicationContext()
                        .getPackageName() + ".provider", file);

        Intent view = new Intent();
        view.setAction(Intent.ACTION_VIEW);
        view.setData(mImageCaptureUri);
        List<ResolveInfo> resInfoList =
                context.getPackageManager()
                        .queryIntentActivities(view, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        view.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(mImageCaptureUri, "image/*");
        context.startActivity(intent);
    }

    private void deleteImage(File file) {
        File fdelete = new File(file.getAbsolutePath());
        if (fdelete.exists()) {
            try {
                if (fdelete.getCanonicalFile().delete()) {
                    list.refreshUI();
                } else {
                    System.out.println("file not Deleted :" + String.valueOf(file));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Helper.dismissProgressDialog();
    }


    public interface RefreshList {
        void refreshUI();
    }
}
