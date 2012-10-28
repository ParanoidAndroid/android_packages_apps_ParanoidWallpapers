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
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
     * Menu item used for "Apply" button on actionbar
     */
    private static final int MENU_APPLY = Menu.FIRST;
    
    /**
     * The {@link Integer} that stores current fragment selected
     */
    private int mCurrentFragment;
    
    /**
     * The {@link ArrayList} that will host the wallpapers resource ID's
     */
    static ArrayList <Integer> sWallpapers = new ArrayList<Integer>();
    
    /**
     * The {@link String[]} that will store wallpaper name
     */
    String[] mWallpaperInfo;
    
    /**
     * The {@link Context} to be used by the app
     */
    static Context mContext;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener(){
            public void onPageSelected(int position) {
                mCurrentFragment = position;
            }
        });
        
        sWallpapers.clear();

        final Resources resources = getResources();
        final String packageName = getApplication().getPackageName();

        fetchWallpapers(resources, packageName, R.array.wallpapers);
        mWallpaperInfo = resources.getStringArray(R.array.info);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new WallpaperFragment();
            Bundle args = new Bundle();
            args.putInt(WallpaperFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return sWallpapers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mWallpaperInfo[position];
        }
    }

    public static class WallpaperFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            mContext = getActivity();
            Bundle args = getArguments();
            LinearLayout holder = new LinearLayout(mContext);
            holder.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            ImageView img = new ImageView(mContext);
            img.setLayoutParams(new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            img.setImageResource(sWallpapers.get(args.getInt(ARG_SECTION_NUMBER)));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.addView(img);
            return holder;
        }
    }

    private void fetchWallpapers(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                sWallpapers.add(res);
            }
        }
    }
    
    class WallpaperLoader extends AsyncTask<Integer, Void, Boolean> {
        BitmapFactory.Options mOptions;
        ProgressDialog mDialog;

        WallpaperLoader() {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
        
        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                Bitmap b = BitmapFactory.decodeResource(getResources(),
                        sWallpapers.get(params[0]), mOptions);
                
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
                try {
                    wallpaperManager.setBitmap(b);
                } catch (IOException e) {
                    // If we crash, we will probably have a null bitmap
                    // return before recycling to avoid exception
                    throw new NullPointerException();
                }
                
                // Help GC
                b.recycle();
                
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_APPLY:
                new WallpaperLoader().execute(mCurrentFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, MENU_APPLY, 0, R.string.action_apply)
                 .setIcon(android.R.drawable.ic_menu_set_as)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return super.onCreateOptionsMenu(menu);
    }
}
