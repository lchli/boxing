package com.bilibili.boxing.model.task.impl;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.bilibili.boxing.model.callback.IMediaTaskCallback;
import com.bilibili.boxing.model.entity.impl.AudioMedia;
import com.bilibili.boxing.model.entity.impl.VideoMedia;
import com.bilibili.boxing.model.task.IMediaTask;
import com.bilibili.boxing.utils.BoxingExecutor;

import java.util.ArrayList;
import java.util.List;

@WorkerThread
public class AudioTask implements IMediaTask<AudioMedia> {

    private final static String[] MEDIA_COL = new String[]{
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
    };


    @Override
    public void load(final ContentResolver cr, final int page, String id, final IMediaTaskCallback<AudioMedia> callback) {
        loadVideos(cr, page, callback);
    }

    private void loadVideos(ContentResolver cr, int page, @NonNull final IMediaTaskCallback<AudioMedia> callback) {
        final List<AudioMedia> videoMedias = new ArrayList<>();
        final Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MEDIA_COL, null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " desc" + " LIMIT " + page * IMediaTask.PAGE_LIMIT + " , " + IMediaTask.PAGE_LIMIT);
        try {
            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getCount();
                do {
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
                    String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                    String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    AudioMedia video = new AudioMedia.Builder(id, data).setTitle(title).setDuration(duration)
                            .setSize(size).setDataTaken(date).setMimeType(type).build();
                    videoMedias.add(video);

                } while (!cursor.isLast() && cursor.moveToNext());
                postMedias(callback, videoMedias, count);
            } else {
                postMedias(callback, videoMedias, 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    private void postMedias(@NonNull final IMediaTaskCallback<AudioMedia> callback,
                            final List<AudioMedia> videoMedias, final int count) {

        BoxingExecutor.getInstance().runUI(new Runnable() {
            @Override
            public void run() {
                callback.postMedia(videoMedias, count);
            }
        });
    }
}