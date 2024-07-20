package com.example.skeenmobile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AiLobby extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final String TAG = "_AI";
    Uri imageUri;
    Button Send2Db,SelectImg;
    ImageView poza;
    TextView textBox;
    FirebaseAuth auth;
    FirebaseStorage storage;
    CustomVisionPredictionClient predictor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_lobby);
        Send2Db = findViewById(R.id.send2db);
        SelectImg = findViewById(R.id.selectimg);
        poza = findViewById(R.id.poza);
        textBox = findViewById(R.id.textBox);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        String Endpoint = "https://testresource-training.cognitiveservices.azure.com/";
        String PredKey = "2be5d2efa82b47af837bbd5ce593a1c9";
        predictor = CustomVisionPredictionManager.authenticate("https://{Endpoint}/customvision/v3.0/prediction/", PredKey).withEndpoint(Endpoint);

        Toolbar toolbarPhoto = findViewById(R.id.toolbar1) ;
        setSupportActionBar(toolbarPhoto);
        getSupportActionBar().setTitle("Scanează. Trimite. Tratează.");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SelectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    takeAPic();
                }
            }
        });

        Send2Db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else {
                        takeAPic();
                    }
                    Toast.makeText(AiLobby.this, "Trebuie sa aveti o poza selectata!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(AiLobby.this, "Imaginea a fost trimisa cu succes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendToAi(byte[] byteArray) {
        Log.d(TAG, "entered Send2Ai");
        String pid = "fa35787b-6a0f-469a-a00a-f0a134c65332";
        ImagePrediction results = predictor.predictions().classifyImage(UUID.fromString(pid), "Iteration7", byteArray, null);
        Log.d(TAG, "did the predictions thing");

        String text = "Diagnosticul dumneavostra este:\n";
        int i=0;
        for (Prediction prediction: results.predictions()) {
            if(i>2)
                break;
            text = text.concat(String.format("\t%s: %.2f%%\n", prediction.tagName(), prediction.probability() * 100.0f));
            i++;
        }
        textBox.setText(text);
    }

    private void UploadToDatabase(byte[] byteArray) {
        Uri fileUri = imageUri;
        textBox.setText("Se asteapta evaluarea...");
        Log.d(TAG, "done w/ string construction1");
        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "done w/ string construction2");
        //String file = fileUri.getPath().substring(fileUri.getPath().lastIndexOf('/')+1);
        Log.d(TAG, "done w/ string construction3");
        String path = String.format("%s/new/%s", uid, "img.jpg"); // TODO: what the fuck is wrong with file uri?! (ignore me if u != Raul)
        Log.d(TAG, "done w/ string construction4");
        StorageReference fileRef = storage.getReference().child(path);

        Log.d(TAG, "Uploading to db");
        UploadTask uploadTask = fileRef.putBytes(byteArray);
        Log.d(TAG, "upload status?");
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "not uploaded to db");
                Toast.makeText(AiLobby.this, "Imaginea nu a putut fi trimisa!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "uploaded to db");
                SendToAi(byteArray);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeAPic();
            } else {
                Toast.makeText(this, "Permisiune respinsa!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takeAPic() {
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent2, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                poza.setImageBitmap(bitmap);
                UploadToDatabase(null);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                imageUri = Uri.parse("using byte[]");
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Log.d(TAG, "extracted byte array from bitmap");

                poza.setImageBitmap(bitmap);
                UploadToDatabase(byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
