package com.example.adminchildplanet;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private EditText vn, catName;
    ImageView imageView;
    Button button, update;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private String downloadUrl;
    private Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        vn = findViewById(R.id.veg);
        update= findViewById(R.id.updatesubmit);
        catName = findViewById(R.id.catName);


        ///////////////load
        loadingDialog= new Dialog(PostActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        }
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        ///////////////load


        imageView = (ImageView)findViewById(R.id.imageView);
        button = (Button)findViewById(R.id.buttonLoadPicture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });

    }

    private void uploadData(){
        loadingDialog.show();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        final StorageReference imageReference = storageReference.child("posts").child(imageUri.getLastPathSegment());

        final UploadTask uploadTask = imageReference.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            downloadUrl= task.getResult().toString();
                            Toast.makeText(PostActivity.this, "pk", Toast.LENGTH_SHORT).show();
                            uploadPostName();

                        } else {
                            Toast.makeText(PostActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void uploadPostName() {


        Map<String,Object> map = new HashMap<>();
        map.put("image", downloadUrl );
        map.put("name", vn.getText().toString());


        FirebaseDatabase.getInstance().getReference().child("Home").child(catName.getText().toString())
                .child("con").child(vn.getText().toString())
                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    finish();

                } else {
                    Toast.makeText(PostActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }


    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

}
