package com.example.adminchildplanet;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.util.HashMap;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private EditText catName;
    private Button updateBtn;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private String downloadUrl;
    private ImageView catImage;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        catName= findViewById(R.id.cat_name);
        updateBtn= findViewById(R.id.update_cat);
        catImage= findViewById(R.id.cat_image);

        ///////////////load
        loadingDialog= new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        }
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        ///////////////load


        catImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
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
            catImage.setImageURI(imageUri);
        }
    }

    private void uploadData(){
        loadingDialog.show();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        final StorageReference imageReference = storageReference.child("category").child(imageUri.getLastPathSegment());

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
                            uploadCategoryName();

                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(CategoryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CategoryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void uploadCategoryName(){

        Map<String, Object> map = new HashMap<>();
        map.put("catname", catName.getText().toString());
        map.put("catimage", downloadUrl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference().child("Home").child(catName.getText().toString()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CategoryActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CategoryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
                loadingDialog.dismiss();
            }

        });
    }
}