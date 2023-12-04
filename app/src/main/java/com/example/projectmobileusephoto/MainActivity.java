package com.example.projectmobileusephoto;

import androidx.annotation.NonNull;
import android.Manifest;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projectmobileusephoto.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
Button selectBtn , predicBtn , captureBtn;
Bitmap bitmap;
TextView result;
ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
//        String[] labels = new String[0];
//        int cnt = 0 ;
//        try {
//            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
//            String line= bufferedReader.readLine();
//            while (line!=null){
//                labels[cnt]= line ;
//                cnt++;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        selectBtn = findViewById(R.id.selectBtn);
        predicBtn = findViewById(R.id.predicBtn);
        captureBtn = findViewById(R.id.captureBtn);
        imageView= findViewById(R.id.imageView);
       result = findViewById(R.id.result);
       selectBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent();
               intent.setAction(Intent.ACTION_GET_CONTENT);
               intent.setType("image/*");
               startActivityForResult(intent, 10);
           }
       });
       captureBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
               startActivityForResult(intent,12);
           }
       });
       predicBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               try {
                   MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(MainActivity.this);

                   // Creates inputs for reference.
                   TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
                   bitmap = Bitmap.createScaledBitmap(bitmap,224,224,true);

                   inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmap).getBuffer());

                   // Runs model inference and gets result.
                   MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                   TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    result.setText(getMax(outputFeature0.getFloatArray())+"");
                   // Releases model resources if no longer used.
                   model.close();
               } catch (IOException e) {
                   // TODO Handle the exception
               }

           }
       });

    }

    int getMax(float[] array ){
        int max= 0 ;
        for (int i =0 ; i<array.length;i++){
            if (array[i]>array[max]){
                max = i ;
            }
        }
        return max;
    }

    private static final int REQUEST_CAMERA_PERMISSION = 100;

// ... (other code in your activity)

    // Request camera permissions if not granted
    void getPermission() {
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 11);
        }
       }
    }



    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 11) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                this.getPermission();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ( requestCode==10){
            Uri uri=data.getData();
            try{
                bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
               imageView.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        } else if (requestCode==12) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}