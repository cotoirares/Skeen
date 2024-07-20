package com.example.skeenmobile;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;

public class LoginFragment extends Fragment {
    private NewLoginActivity activity;
    private FirebaseAuth auth;
    private TextInputLayout regEmail, regPassword;
    private Button forgot;

    public LoginFragment() {}
    public LoginFragment(FirebaseAuth actAuth) {
        auth = actAuth;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewLoginActivity) getActivity();

        regEmail = activity.findViewById(R.id.email);
        regPassword = activity.findViewById(R.id.password);
        Button loginBtn = activity.findViewById(R.id.Login_btn);
        forgot = activity.findViewById(R.id.forgot_pass);
        loginBtn.setOnClickListener(v -> loginUser());
        Button regToSigninBtn = activity.findViewById(R.id.register);
        regToSigninBtn.setOnClickListener(view -> {
                if (activity.getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    Log.d("BacstackCount", Integer.toString(activity.getSupportFragmentManager().getBackStackEntryCount()));
                    activity.getSupportFragmentManager().popBackStackImmediate();
                }
                else {
                    SignupFragment signupFrag= new SignupFragment(activity.auth, activity.user);
                    activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, signupFrag, "findSignupFrag")
                        .addToBackStack(null)
                        .commit();
                }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Resetezi parola ?");
                passwordResetDialog.setMessage("Introduceti adresa de mail");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Link-ul de resetare a fost trimis cu succes !", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "A aparut o eroare ! Va rugam incercati mai tarziu " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Nu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordResetDialog.create().show();
            }
        });

    }

    private void loginUser() {
        String password = regPassword.getEditText().getText().toString();
        String email = regEmail.getEditText().getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");

                            for (Fragment frag : activity.getSupportFragmentManager().getFragments()) { // remove all fragments and move on
                                activity.getSupportFragmentManager().beginTransaction().remove(frag).commit();
                            }
                            Intent intent = new Intent(activity, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}