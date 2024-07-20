package com.example.skeenmobile;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NewLoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    FragmentManager fragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_newlogin);
        fragManager = getSupportFragmentManager();
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = auth.getCurrentUser();
        if(user == null) {
            int page = getIntent().getIntExtra("page", 1);
            if (page == 1)
                fragManager.beginTransaction()
                        .add(R.id.fragment_container, new SignupFragment(auth, user), "findSignupFrag")
                        .commit();
            else if (page == 2)
                fragManager.beginTransaction()
                        .add(R.id.fragment_container, new LoginFragment(auth), "findLoginFrag")
                        .commit();
            else
                Log.e("NewLoginAct: Invalid 'page' extra in intent!", Integer.toString(page));
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            //intent.putExtra(<var> , <val>);
            startActivity(intent);
        }
    }
}