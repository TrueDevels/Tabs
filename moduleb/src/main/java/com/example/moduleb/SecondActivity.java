package com.example.moduleb;

import android.Manifest;
import android.app.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.taishi.flipprogressdialog.FlipProgressDialog;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.master.glideimageview.GlideImageView;

import es.dmoral.toasty.Toasty;


public class SecondActivity extends Activity {
    private static final String APP_A_URL_TAG = "com.example.moduleb";
    private static final String LINK_TAG = "url";
    String url;
    ImageView image;
    Bundle extras;
    AlertDialog alertD;
    CountDownTimer localTimer;
    String imageURL;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;

    RelativeLayout Alayout;
    AnimationDrawable animationDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);



        Alayout =  findViewById(R.id.ViewImageActivity);
        animationDrawable = (AnimationDrawable) Alayout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();



        if (ActivityCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SecondActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
        }


        Intent intentFromAppA = getIntent();
        if (!checking(intentFromAppA)) {
            DialogWindow();
        } else {
            extras = intentFromAppA.getExtras();
            if(extras.getInt("stat")==2) {
                url =null;
                Toast.makeText(this,"URL is not an image",Toast.LENGTH_LONG).show();
            } else{
              url = intentFromAppA.getStringExtra(LINK_TAG);

                    image =  findViewById(R.id.image);

                    String extension = "";
                    int i = url.lastIndexOf('.');
                    if (i > 0) {
                        extension = url.substring(i + 1);
                    }

                    if(image.getDrawable()==null) {
                        if(extension.equals("gif")){
                            enableStrictMode();
                            GlideImageView glideImageView;
                            glideImageView =  findViewById(R.id.glideImageView);
                            glideImageView.loadImageUrl(url);

                        }else{
                        new UploadImage().execute(url);}

                    }





                if(extras.getString("from").equals("history")){

                    DownloadImage asyncTask = new DownloadImage();
                    asyncTask.setURL(url);
                    asyncTask.execute();
                }


            }
        }
    }

    //=============================
    public void DialogWindow() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SecondActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Oops...");
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertD.dismiss();
                localTimer.cancel();
                finish();

            }
        });
        alertD = alertDialog.create();
        alertD.setCancelable(false);

        localTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                alertD.setMessage("You need to start this app from module A! It will be closed automatically in " + " 00:0" + (l / 1000));
                alertD.show();
                }

            @Override
            public void onFinish() {
                alertD.dismiss();
                finish();
                moveTaskToBack(true);
            }
        }.start();


    }
    public void shutDown_Dialog(){
        if(alertD!=null){
            alertD.dismiss();
            localTimer.cancel();
        }
    }
    private boolean checking(Intent intent) {
        Set<String> ss = intent.getCategories();
        for (String temp : ss) {
            if (temp.equals(APP_A_URL_TAG)) return true;
        }
        return false;
    }


    private class UploadImage extends AsyncTask<String, Void, Bitmap> {
        InputStream input = null;

        List<Integer> imageList = new ArrayList<>();
        FlipProgressDialog flip = new FlipProgressDialog();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            imageList.add(R.drawable.download);
            imageList.add(R.drawable.gal);
            flip.setImageList(imageList);
            flip.setOrientation("rotationY");
            flip.setBackgroundColor(Color.parseColor("#4a4a4a"));
            flip.setBackgroundAlpha(0.2f);
            flip.setCornerRadius(32);
            flip.setDuration(800);
            flip.setCancelable(false);
            flip.show(getFragmentManager(), "");
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            imageURL = URL[0];

            Bitmap bitmap = null;
            try {

                input = new java.net.URL(imageURL).openStream();



                bitmap = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            try{input.close();}
            catch (IOException e){
                Toasty.error(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }



            image.setImageBitmap(result);

            flip.dismiss();
        }
    }


    @Override
    protected void onPause(){
        shutDown_Dialog();
        super.onPause();
        finish();
    }
    @Override
    protected void onStop(){
        shutDown_Dialog();
        super.onStop();
        finish();
    }
    @Override
    protected void onDestroy(){
        shutDown_Dialog();
        super.onDestroy();
        finish();
    }
    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

}




