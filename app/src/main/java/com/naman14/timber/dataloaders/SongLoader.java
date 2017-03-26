/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.naman14.timber.dataloaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.naman14.timber.models.Song;
import com.naman14.timber.utils.Helpers;
import com.naman14.timber.utils.PreferencesUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongLoader {

    private static final long[] sEmptyList = new long[0];

    public static ArrayList<Song> getSongsByID3ForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if((cursor != null) && (cursor.moveToFirst()))
            do {
                Mp3File mp3file = null;
                ID3v1 id3v1 = null;
                try {
                    mp3file = new Mp3File(cursor.getString(8));
                    if(mp3file.hasId3v2Tag()) {
                        id3v1 = mp3file.getId3v2Tag();
                    } else if(mp3file.hasId3v1Tag()) {
                        id3v1 = mp3file.getId3v1Tag();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }

                if(id3v1 == null) return arrayList;
                Song s = new Song(cursor.getLong(0), cursor.getLong(7), cursor.getInt(6), id3v1.getTitle(), id3v1.getArtist()
                , id3v1.getAlbum(), cursor.getInt(4), cursor.getInt(5));

                arrayList.add(s);
            }
            while(cursor.moveToNext());
        if(cursor != null)
            cursor.close();
        return arrayList;
    }

    public static ArrayList<Song> getSongsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if((cursor != null) && (cursor.moveToFirst()))
            do {
                long id = cursor.getLong(0);
                String title = Helpers.isMessyCode(cursor.getString(1)) ? Helpers.convertMessyCode(cursor.getString(1)) : cursor.getString(1);
                //cursor.getString(1);
                String artist = Helpers.isMessyCode(cursor.getString(2)) ? Helpers.convertMessyCode(cursor.getString(1)) : cursor.getString(2);
                        //cursor.getString(2);
                if(title.equals("02 諢帙�縺翫⊂縺医※縺�∪縺吶°"))
                {
//                    title = new File(cursor.getString(8)).getName();
                    title = Helpers.convertMessyCode(cursor.getString(1));
                    artist = Helpers.convertMessyCode(cursor.getString(2));
                }
                String album = cursor.getString(3);
                int duration = cursor.getInt(4);
                int trackNumber = cursor.getInt(5);
                long artistId = cursor.getInt(6);
                long albumId = cursor.getLong(7);

                Song s = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber);
                s.path = cursor.getString(8);
                arrayList.add(s);
            }
            while(cursor.moveToNext());
        if(cursor != null)
            cursor.close();
        return arrayList;
    }

    public static Song getSongForCursor(Cursor cursor) {
        Song song = new Song();
        if((cursor != null) && (cursor.moveToFirst())) {
            long id = cursor.getLong(0);
            String title = cursor.getString(1);
            String artist = cursor.getString(2);
            String album = cursor.getString(3);
            int duration = cursor.getInt(4);
            int trackNumber = cursor.getInt(5);
            long artistId = cursor.getInt(6);
            long albumId = cursor.getLong(7);

            song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber);
        }

        if(cursor != null)
            cursor.close();
        return song;
    }

    public static final long[] getSongListForCursor(Cursor cursor) {
        if(cursor == null) {
            return sEmptyList;
        }
        final int len = cursor.getCount();
        final long[] list = new long[len];
        cursor.moveToFirst();
        int columnIndex = -1;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch(final IllegalArgumentException notaplaylist) {
            columnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
        for(int i = 0; i < len; i++) {
            list[i] = cursor.getLong(columnIndex);
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;
        return list;
    }

    public static Song getSongFromPath(String songPath, Context context) {
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA;
        String[] selectionArgs = {songPath};
        String[] projection = new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"};
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);

        if(cursor != null && cursor.getCount() > 0) {
            Song song = getSongForCursor(cursor);
            cursor.close();
            return song;
        } else return new Song();
    }

    public static ArrayList<Song> getAllSongs(Context context) {
        return getSongsByID3ForCursor(makeSongCursor(context, null, null));
    }

    public static long[] getSongListInFolder(Context context, String path) {
        String[] whereArgs = new String[]{path + "%"};
        return getSongListForCursor(makeSongCursor(context, MediaStore.Audio.Media.DATA + " LIKE ?", whereArgs, null));
    }

    public static Song getSongForID(Context context, long id) {
        return getSongForCursor(makeSongCursor(context, "_id=" + String.valueOf(id), null));
    }

    public static List<Song> searchSongs(Context context, String searchString, int limit) {
        ArrayList<Song> result = getSongsForCursor(makeSongCursor(context, "title LIKE ?", new String[]{searchString + "%"}));
        if(result.size() < limit) {
            result.addAll(getSongsForCursor(makeSongCursor(context, "title LIKE ?", new String[]{"%_" + searchString + "%"})));
        }
        return result.size() < limit ? result : result.subList(0, limit);
    }


    public static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder);
    }

    private static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString, String sortOrder) {
        String selectionStatement = "is_music=1 AND title != ''";

        if(!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }
        //return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, selectionStatement, paramArrayOfString, sortOrder);
        return context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"
                , MediaStore.Audio.Media.DATA}
                , selectionStatement, paramArrayOfString, sortOrder);
    }


}
