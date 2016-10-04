package com.lwh8762.imageencryption;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Random;

/**
 * Created by W on 2016-09-25.
 */
public class EncryptionManager {

    private long max = 0;
    private Bitmap bitmap = null;
    private OnProgressChangedListener listener = null;


    public EncryptionManager(Bitmap bitmap) {
        if(bitmap!=null)
            this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    public EncryptionManager() {
        this(null);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        max = bitmap.getWidth() * bitmap.getHeight();
        Log.e("C", "" + bitmap.getPixel(5,5));
    }

    public void encryption(int key) {
        int height = bitmap.getHeight();
        if(bitmap==null) return;
        Random random = new Random(key);
        Log.e("C", "" + bitmap.getPixel(5,5));
        for(int x = 0;x < bitmap.getWidth();x ++) {
            for(int y = 0;y < height;y ++) {
                bitmap.setPixel(x,y,bitmap.getPixel(x,y) + random.nextInt(2147483647));
                if(listener!=null)
                    listener.onProgressChanged((int) ((x*height + y)*100/max));
            }
        }
        Log.e("C", "" + bitmap.getPixel(5,5));
    }

    public void decryption(int key) {
        int height = bitmap.getHeight();
        if(bitmap==null) return;
        Random random = new Random(key);
        for(int x = 0;x < bitmap.getWidth();x ++) {
            for(int y = 0;y < height;y ++) {
                bitmap.setPixel(x,y,bitmap.getPixel(x,y) - random.nextInt(2147483647));
                if(listener!=null)
                    listener.onProgressChanged((int) ((x*height + y)*100/max));
            }
        }
        Log.e("C", "" + bitmap.getPixel(5,5));
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        this.listener = listener;
    }

    public interface OnProgressChangedListener {
        public void onProgressChanged(int progress);
    }
}
