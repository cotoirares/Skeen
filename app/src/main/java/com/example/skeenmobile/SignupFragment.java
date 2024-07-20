package com.example.skeenmobile;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupFragment extends Fragment {
    private NewLoginActivity activity;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private Button regToLoginBtn, regBtn;
    private TextInputLayout regName, regEmail, regPhoneNo, regPassword;

    public SignupFragment() {}
    public SignupFragment(FirebaseAuth actAuth, FirebaseUser actUser) {
        auth = actAuth;
        user = actUser;
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewLoginActivity) getActivity();

        regName = activity.findViewById(R.id.name);
        regEmail = activity.findViewById(R.id.email);
        regPhoneNo = activity.findViewById(R.id.phoneNo);
        regPassword = activity.findViewById(R.id.password);

        regBtn = activity.findViewById(R.id.akiec);
        regBtn.setOnClickListener(v -> registerUser());
        regToLoginBtn = activity.findViewById(R.id.signin);
        regToLoginBtn.setOnClickListener(view -> {
            LoginFragment loginFrag= new LoginFragment(auth);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, loginFrag, "findLoginFrag")
                    .addToBackStack(null)
                    .commit();
        });
    }

    private Boolean validateName(String name) {
        if (name.isEmpty()) {
            regName.setError("Acest camp trebuie completat !");
            return false;
        }
        return true;
    }

    private Boolean validateEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (email.isEmpty()) {
            regEmail.setError("Acest camp trebuie completat !");
            return false;
        } else if (!email.matches(emailPattern)) {
            regEmail.setError("Alege o adresa de email valida !");
            return false;
        }
        return true;
    }

    private Boolean validatePhoneNo(String phone) {
        if (phone.isEmpty()) {
            regPhoneNo.setError("Acest camp trebuie completat !");
            return false;
        }
        return true;
    }

    private Boolean validatePassword(String pass) {
        String passwordVal = "^" +
                "(?=.*[a-zA-Z])" +
                "(?=.*[@#$%^&+=!?])" +
                "(?=\\S+$)" +
                ".{4,}" +
                "$";
        if (pass.isEmpty()) {
            regPassword.setError("Acest camp trebuie completat !");
            return false;
        } else if (!pass.matches(passwordVal)) {
            regPassword.setError("Alege o parola mai puternica !");
            return false;
        }
        return true;
    }

    public void registerUser() {
        String name=regName.getEditText().getText().toString();
        String email=regEmail.getEditText().getText().toString();
        final String phoneNo=regPhoneNo.getEditText().getText().toString();
        String password=regPassword.getEditText().getText().toString();
        if (validateName(name) ||
            validateEmail(email) ||
            validatePhoneNo(phoneNo) ||
            validatePassword(password)) {

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("AUTH", "createUserWithEmail:success");
                                user = auth.getCurrentUser();
                                PhoneVerifFragment phoneVerifFrag = new PhoneVerifFragment(auth, user, phoneNo);
                                activity.getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, phoneVerifFrag, "findPhoneVerifFrag")
                                        .addToBackStack(null)
                                        .commit();
                            }
                            else {
                                Log.w("AUTH", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}