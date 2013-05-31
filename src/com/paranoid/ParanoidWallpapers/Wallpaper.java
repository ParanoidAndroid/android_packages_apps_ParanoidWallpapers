/*
 * Copyright (C) 2012 ParanoidAndroid Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package com.paranoid.ParanoidWallpapers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;

@SuppressLint("ParserError")
public class Wallpaper extends FragmentActivity {
   
    /**
     * Menu item used for "Apply" button on actionbar.
     */
    private static final int MENU_APPLY = Menu.FIRST;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * The {@link WallpaperManager} used to set wallpaper.
     */
    WallpaperManager mWallpaperManager;
    
    /**
     * The {@link Integer} that stores current fragment selected.
     */
    int mCurrentFragment;
    
    /**
     * The {@link ArrayList} that will host the wallpapers resource ID's.
     */
    ArrayList <Integer> mWallpapers = new ArrayList<Integer>();
    
    /**
     * The {@link String[]} that will store wallpaper name.
     */
    String[] mWallpaperInfo;
    
    /**
     * The {@link Context} to be used by the app.
     */
    Context mContext;

    /**
     * The {@link Boolean} that stores if current item is customizable.
     */
    boolean mCustomizable = true;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mContext = this;

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener(){
            public void onPageSelected(int position) {
                mCurrentFragment = position;

                // Check if item is customizable
                mCustomizable = (mCurrentFragment == 0);
                Wallpaper.this.invalidateOptionsMenu();
            }
        });
        
        mWallpapers.clear();

        final Resources resources = getResources();
        final String packageName = getPackageName();

        fetchWallpapers(resources, packageName, R.array.wallpapers);
        mWallpaperInfo = resources.getStringArray(R.array.info);
        mWallpaperManager = WallpaperManager.getInstance(mContext);
        
        mSectionsPagerAdapter.notifyDataSetChanged(); 
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private WallpaperFragment mCurrent;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new WallpaperFragment();
            Bundle args = new Bundle();
            args.putInt(WallpaperFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            mCurrent = (WallpaperFragment) fragment;
            return fragment;
        }

        public WallpaperFragment getCurrentFragment() {
            return mCurrent;
        }

        @Override
        public int getCount() {
            return mWallpapers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mWallpaperInfo[position];
        }
    }

    public class WallpaperFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            Bundle args = getArguments();
            LinearLayout holder = new LinearLayout(mContext);
            holder.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            ImageView imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setImageResource(mWallpapers.get(args.getInt(ARG_SECTION_NUMBER)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.addView(imageView);
            return holder;
        }
    }

    private void fetchWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                mWallpapers.add(res);
            }
        }
    }
    
    private class WallpaperLoader extends AsyncTask<Integer, Void, Boolean> {
        ProgressDialog mDialog;
        Bitmap mBitmap;

        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }
        
        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                try {
                    mWallpaperManager.setBitmap(mBitmap);
                } catch (IOException e) {
                    // If we crash, we will probably have a null bitmap
                    // return before recycling to avoid exception
                    throw new NullPointerException();
                }
                
                // Help GC
                mBitmap.recycle();
                
                return true;
            } catch (OutOfMemoryError e) {
                return false;
            } catch(NullPointerException e){
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            mDialog.dismiss();
            finish();
        }
        
        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(mContext, null, getString(R.string.applying));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final WallpaperLoader loader = new WallpaperLoader();
        switch (item.getItemId()) {
            case MENU_APPLY:
                if(mCustomizable) {
                    final CharSequence[] items = getResources()
                            .getStringArray(R.array.customize_colors);
                    int resId = mWallpapers.get(mCurrentFragment);
                    Drawable d = mContext.getResources()
                            .getDrawable(resId);
                    final Bitmap stockBitmap = ((BitmapDrawable)d)
                            .getBitmap();

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.pick_color));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            int color = Color.BLACK;
                            boolean setColor = true;
                            switch(item){
                                case 0:
                                    color = Color.RED;
                                    break;
                                case 1:
                                    color = Color.GREEN;
                                    break;
                                case 2:
                                    color = Color.BLUE;
                                    break;
                                case 3:
                                    setColor = false;
                                    break;
                            }
                            
                            loader.setBitmap(setColor ?
                                    getColoredBitmap(stockBitmap, color) :
                                    stockBitmap);
                            loader.execute();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setCancelable(false);
                    alert.show();
                } else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                            mWallpapers.get(mCurrentFragment));
                    loader.setBitmap(bitmap);
                    loader.execute();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getColoredBitmap(Bitmap src, int color){
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap dest = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
         
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, Mode.OVERLAY)); 
        canvas.drawBitmap(src, 0, 0, paint);
         
        return dest;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, MENU_APPLY, 0, R.string.action_apply)
                 .setIcon(android.R.drawable.ic_menu_set_as)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return super.onCreateOptionsMenu(menu);
    }
}
