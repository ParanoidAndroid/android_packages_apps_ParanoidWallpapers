/*
 * Copyright (C) 2012 ParanoidAndroid Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package com.paranoid.ParanoidWallpapers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import java.io.IOException;

public class SolidColor extends Activity {
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        final CharSequence[] items = getResources().getStringArray(R.array.colors);
        final Context mContext = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.pick_color));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                int color = Color.BLACK;
                switch(item){
                    case 0:
                        color = Color.parseColor("#CC0000");
                        break;
                    case 1:
                        color = Color.parseColor("#669900");
                        break;
                    case 2:
                        color = Color.parseColor("#0099CC");
                        break;
                    case 3:
                        color = Color.parseColor("#9933CC");
                        break;
                    case 4:
                        color = Color.parseColor("#FF8800");
                        break;
                    case 5:
                        color = Color.WHITE;
                        break;
                }
                
                try {
                    WallpaperManager wm = WallpaperManager.getInstance(mContext);
                    
                    // Create 1x1 bitmap to store the color
                    Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

                    // Make a canvas with which we can draw to the bitmap
                    Canvas canvas = new Canvas(bmp);

                    // Fill with color and save
                    canvas.drawColor(color);
                    canvas.save();

                    wm.setBitmap(bmp);
                    bmp.recycle();
                } catch (IOException e) {
                    // oh lord!
                }
                
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}