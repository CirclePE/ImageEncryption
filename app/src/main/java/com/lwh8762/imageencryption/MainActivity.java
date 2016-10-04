package com.lwh8762.imageencryption;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int NULL = 0;
    private static final int ENCRYPTION = 1;
    private static final int DECRYPTION = 2;
    private static final int REQUEST_CODE = 1231;

    private ProgressDialog progressDialog = null;

    private int inputedKey = 0;

    private int active = NULL;

    private String path = null;

    private EncryptionManager encManager = null;
    private ImageView imgView = null;
    private LinearLayout keyInputLayout = null;
    private EditText keyInput = null;
    private Button selectBtn = null;
    private Button saveBtn = null;
    private Button encBtn= null;
    private Button decBtn= null;
    private Button applyBtn = null;

    private MyThread myThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("기다려 주세요...");

        encManager = new EncryptionManager();
        encManager.setOnProgressChangedListener(new EncryptionManager.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                progressDialog.setProgress(progress);
            }
        });

        imgView = (ImageView) findViewById(R.id.imgView);
        keyInputLayout = (LinearLayout) findViewById(R.id.keyInputLayout);
        keyInput = (EditText) findViewById(R.id.keyInput);
        selectBtn = (Button) findViewById(R.id.selectBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        encBtn = (Button) findViewById(R.id.encBtn);
        decBtn = (Button) findViewById(R.id.decBtn);
        applyBtn = (Button) findViewById(R.id.applyBtn);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE);

            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(encManager.getBitmap()!=null)
                    saveImage();
            }
        });
        encBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptionImg();
            }
        });
        decBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decryptionImg();
            }
        });
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    inputedKey = Integer.parseInt(keyInput.getText().toString());
                }catch (Exception e) {
                    Toast.makeText(MainActivity.this,"올바른 값을 입력해 주세요.",Toast.LENGTH_LONG).show();
                    return;
                }
                myThread.setKey(inputedKey);
                if(active==ENCRYPTION) {
                    if (encManager.getBitmap()==null) {
                        Toast.makeText(MainActivity.this,"이미지를 설정해 주세요.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    myThread.encryption();
                }else if(active==DECRYPTION) {
                    if (encManager.getBitmap()==null) {
                        Toast.makeText(MainActivity.this,"이미지를 설정해 주세요.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    myThread.decryption();
                }
                keyInputLayout.setVisibility(View.INVISIBLE);
                progressDialog.show();
            }
        });

        myThread = new MyThread();
        myThread.start();

        int color = Color.WHITE;
        Log.e("C1", "" + color);
        color += 2140000000;
        Log.e("C2", "" + color);
        color -= 2140000000;
        Log.e("C3", "" + color);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE&&data!=null) {
            Cursor c = getContentResolver().query(data.getData(), null, null, null, null);
            c.moveToNext();
            path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            selectImage(path);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(keyInputLayout.getVisibility()==View.VISIBLE) {
                keyInputLayout.setVisibility(View.INVISIBLE);
            }else {
                finish();
            }
        }

        return false;
    }


    @Override
    protected void onDestroy() {
        myThread.interrupt();
        super.onDestroy();
    }

    private void encryptionImg() {
        active = ENCRYPTION;
        keyInputLayout.setVisibility(View.VISIBLE);
    }

    private void decryptionImg() {
        active = DECRYPTION;
        keyInputLayout.setVisibility(View.VISIBLE);
    }

    private void selectImage(String path) {
        encManager.setBitmap(BitmapFactory.decodeFile(path));
        this.path += ".png";
        imgView.setImageBitmap(encManager.getBitmap());
    }

    private void saveImage() {
        try {
            String format = path.substring(path.lastIndexOf(".")+1, path.length());
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            encManager.getBitmap().compress(Bitmap.CompressFormat.PNG,100,fos);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
            Toast.makeText(MainActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    class MyThread extends Thread {
        private int key = 0;
        private int state = 0;

        @Override
        public void run() {
            super.run();
            try {
                while(true) {
                    Thread.sleep(1);
                    if(state==1) {
                        encManager.encryption(key);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageBitmap(encManager.getBitmap());
                                progressDialog.hide();
                                progressDialog.setProgress(0);
                            }
                        });
                        state = 0;
                    }else if(state==2) {
                        encManager.decryption(key);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageBitmap(encManager.getBitmap());
                                progressDialog.hide();
                                progressDialog.setProgress(0);
                            }
                        });
                        state = 0;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setKey(int key) {
            this.key = key;
        }

        public void encryption() {
            state = 1;
        }

        public void decryption() {
            state = 2;
        }
    }
}
