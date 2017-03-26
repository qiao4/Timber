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

package com.naman14.timber.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.naman14.timber.R;
import com.naman14.timber.models.Song;
import com.squareup.okhttp.internal.io.FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Helpers {
    static String tag = "encode";

    public static void showAbout(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if(prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft, "dialog_about");
    }

    public static String getATEKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    public static class AboutDialog extends DialogFragment {

        String urlgooglelus = "https://plus.google.com/u/0/+NamanDwivedi14";
        String urlcommunity = "https://plus.google.com/communities/111029425713454201429";
        String urltwitter = "https://twitter.com/naman1405";
        String urlgithub = "https://github.com/naman14";
        String urlsource = "https://github.com/naman14/Timber/issues";

        public AboutDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout aboutBodyView = (LinearLayout) layoutInflater.inflate(R.layout.layout_about_dialog, null);

            TextView appversion = (TextView) aboutBodyView.findViewById(R.id.app_version_name);

            TextView googleplus = (TextView) aboutBodyView.findViewById(R.id.googleplus);
            TextView twitter = (TextView) aboutBodyView.findViewById(R.id.twitter);
            TextView github = (TextView) aboutBodyView.findViewById(R.id.github);
            TextView source = (TextView) aboutBodyView.findViewById(R.id.source);
            TextView community = (TextView) aboutBodyView.findViewById(R.id.feature_request);

            TextView dismiss = (TextView) aboutBodyView.findViewById(R.id.dismiss_dialog);
            dismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            googleplus.setPaintFlags(googleplus.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            twitter.setPaintFlags(twitter.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            github.setPaintFlags(github.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            googleplus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urlgooglelus));
                    startActivity(i);
                }

            });
            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urltwitter));
                    startActivity(i);
                }

            });
            github.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urlgithub));
                    startActivity(i);
                }

            });
            source.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urlsource));
                    startActivity(i);
                }
            });
            community.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(urlcommunity));
                    startActivity(i);
                }
            });
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName;
                int versionCode = pInfo.versionCode;
                appversion.setText("Timber " + version);
            } catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return new AlertDialog.Builder(getActivity())
                    .setView(aboutBodyView)
                    .create();
        }

    }

    public static void showCursorInfo(Cursor c) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/cursor.txt"));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        if(pw == null) return;

        try {
            if(c != null && c.moveToFirst()) {
                pw.append(String.format("-------------%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS-------------", Calendar.getInstance()));
                pw.append("\r\n");
                do {

                    for(int i = 0; i < c.getColumnCount(); i++) {
                        if(c.getColumnName(i).equals("title")) {
                            pw.append(new String(c.getString(i).getBytes("shift_jis"), "utf-8"));//shift_jis
                            pw.append("\r\n");
                        }
                        pw.append(c.getString(i));
                        pw.append("\r\n");
                    }
                    pw.append("******************************************************");
                    pw.append("\r\n");
                } while(c.moveToNext());
                pw.append("-------------end--------------");
                pw.append("\r\n");
            }
        } catch(Exception e) {//UnsupportedEncoding
            e.printStackTrace();
        } finally {
            pw.flush();
            pw.close();
        }
    }

    public static void showSongList(ArrayList<Song> songs) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/songs.txt"));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        if(pw == null) return;

        try {
            pw.append(String.format("-------------%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS-------------", Calendar.getInstance()));
            pw.append("\r\n");
            for(Song s : songs) {
                pw.append(s.title + " | " + new File(s.path).getName());
                pw.append("\r\n");

                //

                Mp3File mp3file = new Mp3File(s.path);
                Log.e(tag, s.path);
                if(mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1 = mp3file.getId3v1Tag();
                    Log.e(tag, Thread.currentThread().getId() + "---idv3---" + id3v1.getTitle());
                }
                if(mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2 = mp3file.getId3v2Tag();
                    Log.e(tag, Thread.currentThread().getId() + "---idv3---" + id3v2.getTitle());
                }
                //
            }
            pw.append("-------------end--------------");
            pw.append("\r\n");
        } catch(Exception e) {//UnsupportedEncoding
            e.printStackTrace();
        } finally {
            pw.flush();
            pw.close();
        }
    }

    /**
     * if string is messycode return true
     *
     * @param str
     * @return if string is messycode return true
     */
    public static boolean isMessyCode(String str) {
        Log.e(tag, Thread.currentThread().getId() + "--" + str);
        try {
            String pattern = String.format("[^\u4e00-\u9fd5\u0000-\u017f\u3040-\u30fF]{%d,}", str.length() / 2);
            Pattern p = Pattern.compile(pattern);
            return p.matcher(str).matches();
        } catch(PatternSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String convertMessyCode(String str) {
        String[] codes = {"shift_jis", "big5", "gbk"};
        Log.e(tag, Thread.currentThread().getId() + "--" + str);
        for(String code : codes) {
            try {
                String encodeResult = new String(str.getBytes(code), "utf-8");
                Log.e(tag, Thread.currentThread().getId() + "--" + encodeResult);
                if(!isMessyCode(encodeResult)) return encodeResult;
            } catch(Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return str;
    }

}
