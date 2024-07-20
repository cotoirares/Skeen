package com.example.skeenmobile;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class PhoneVerifFragment extends Fragment {
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthCredential credentials;
    private NewLoginActivity activity;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private String phone, verifId;
    private TextInputLayout codeInput;

    public PhoneVerifFragment() {}
    public PhoneVerifFragment(FirebaseAuth actAuth, FirebaseUser actUser, String phoneNo) {
        auth = actAuth;
        user = actUser;
        phone = phoneNo;
        OverrideCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_phone_verif, container, false);
    }

    public void ValidatePhoneNo() {
        if (credentials == null) {
            String code = codeInput.getEditText().getText().toString();
            credentials = PhoneAuthProvider.getCredential(verifId, code);
        }
        user.linkWithCredential(credentials)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("PhoneVerif", "linkWithCredential:success");
                            user = task.getResult().getUser();

                            for (Fragment frag : activity.getSupportFragmentManager().getFragments()) { // remove all fragments and move on
                                activity.getSupportFragmentManager().beginTransaction().remove(frag).commit();
                            }
                            Intent intent = new Intent(activity, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Log.w("PhoneVerif", "linkWithCredential:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (NewLoginActivity) getActivity();
        codeInput = activity.findViewById(R.id.verification_code_entered_by_user);
        activity.findViewById(R.id.verify_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePhoneNo();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+4" + phone, 60, TimeUnit.SECONDS, activity, callbacks);
    }

    private void OverrideCallbacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("PhoneVerif", "onVerificationCompleted:" + credential);
                credentials = credential;
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("PhoneVerif", "onVerificationFailed", e);
                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("PhoneVerif", "onCodeSent:" + verificationId);
                verifId = verificationId;
                //PhoneAuthProvider.ForceResendingToken mResendToken = token;
            }
        };
    }
}