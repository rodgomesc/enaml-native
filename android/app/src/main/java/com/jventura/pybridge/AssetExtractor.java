/**
 * Utilities for extracting assets from an APK file.
 *
 * It assumes that the assets are going to be extracted and manipulated
 * in the application data dir. By default, the extracted assets will be
 * located in the '<dataDir>/assets/' folder.
 *
 */

package com.jventura.pybridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class AssetExtractor {

    private final static String TAG = "AssetExtractor";
    private final static int BUFFER = 8192;
    private Context mContext;
    private AssetManager mAssetManager;

    public AssetExtractor(Context context) {
        mContext = context;
        mAssetManager = context.getAssets();
    }

    /**
     * Sets a version for the extracted assets version.
     *
     * @param version: int
     */
    public void setAssetsVersion(int version) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("assetsVersion", version);
        editor.apply();
    }

    /**
     * Returns the version for the extracted assets.
     *
     * @return int
     */
    public int getAssetsVersion() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getInt("assetsVersion", 0);
    }

    /**
     * Returns a list of assets in the APK.
     *
     * @param path: the path in the assets folder.
     * @return the list of assets.
     */
    public List<String> listAssets(String path) {
        List<String> assets = new ArrayList<>();

        try {
            String assetList[] = mAssetManager.list(path);

            if (assetList.length > 0) {
                for (String asset : assetList) {
                    List<String> subAssets = listAssets(path + '/' + asset);
                    assets.addAll(subAssets);
                }
            } else {
                assets.add(path);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return assets;
    }

    /**
     * Returns the path to the assets data dir on the device.
     *
     * @return String with the data dir path.
     */
    public String getAssetsDataDir() {
        String appDataDir = mContext.getApplicationInfo().dataDir;
        return appDataDir + "/assets/";
    }

    /**
     * Copies an asset from the APK to the device.
     *
     * @param src: the source path in the APK.
     * @param dst: the destination path in the device.
     */
    private void copyAssetFile(String src, String dst) {
        File file = new File(dst);
        Log.i(TAG, String.format("Copying %s -> %s", src, dst));

        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            InputStream in = mAssetManager.open(src);
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
            out.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies the assets from the APK to the device.
     *
     * @param path: the source path
     */
    public void copyAssets(String path) {
        for (String asset : listAssets(path)) {
            copyAssetFile(asset, getAssetsDataDir() + asset);
            if (asset.endsWith(".zip")) {
                unzipAsset(getAssetsDataDir() + asset);
            }
        }
    }

    /**
     * Decompresses an asset and removes the source
     *
     * @param asset: the path within the assets folder
     */
    public void unzipAsset(String asset) {
        try {
            Log.d(TAG, "Extracting: " +asset);
            // Extract zip
            String extractDir = (new File(asset)).getParent();
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(asset);
            Enumeration e = zipfile.entries();
            while(e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();


                File destFile = new File(extractDir, entry.getName());
                File destinationParent = destFile.getParentFile();
                //If entry is directory create sub directory on file system
                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    //Log.d(TAG, "Extracting: " +entry);
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(destFile.getAbsolutePath());

                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }

            // Remove source zip
            (new File(asset)).delete();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively deletes the contents of a folder.
     *
     * @param file: the File object.
     */
    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles())
                recursiveDelete(f);
        }

        Log.i(TAG, "Removing " + file.getAbsolutePath());
        file.delete();
    }

    /**
     * Removes recursively the assets from the device.
     *
     * @param path: the path to the assets folder
     */
    public void removeAssets(String path) {
        File file = new File(getAssetsDataDir() + path);
        recursiveDelete(file);
    }

    /**
     * Returns if the path exists in the device assets.
     *
     * @param path: the path to the assets folder
     * @return Boolean
     */
    public Boolean existsAssets(String path) {
        File file = new File(getAssetsDataDir() + path);
        return file.exists();
    }
}
