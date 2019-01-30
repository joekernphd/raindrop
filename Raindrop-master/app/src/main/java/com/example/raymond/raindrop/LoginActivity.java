package com.example.raymond.raindrop;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.raymond.raindrop.persistence.users.UserRepository;
import com.example.raymond.raindrop.persistence.users.UserStorable;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private static final UserRepository userRepository = new UserRepository();

    // Google Login API client
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";

    // Holds the type of sign in used
    private SignInType successSignInType = SignInType.NONE;
    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpGoogleSignInButton();
        setUpSignOutButton();
        allowNetworkCommunications();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void allowNetworkCommunications() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void createUserIfNotExists(String userEmail) {
        if(userRepository.read(new UserStorable(userEmail, 0)) == null) {
            userRepository.write(new UserStorable(userEmail, 0));
        }
    }

    private void signOut() {
        if(successSignInType.equals(SignInType.GOOGLE)) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult( @NonNull Status status) {
                            // ...
                        }
                    });
        }
    }

    // Google sign in logic
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        Log.d(TAG, result.getStatus().toString());

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            signInSuccessLogic(result);
        } else {
            // Signed out, show unauthenticated UI.
            updateUiForSignIn(false);
        }
    }

    private void signInSuccessLogic(GoogleSignInResult result) {
        GoogleSignInAccount acct = result.getSignInAccount();
        successSignInType = SignInType.GOOGLE;
        userEmail = acct.getEmail();

        createUserIfNotExists(userEmail);
        updateUiForSignIn(true);
    }

    private void updateUiForSignIn(boolean update) {
        if(update) {
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            intent.putExtra("userEmail", userEmail);// if its string type
            startActivity(intent);
        }
    }

    private void setUpGoogleSignInButton() {
        // Configure sign-in to request the user's ID, email address, and basic profile
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);

        googleSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void setUpSignOutButton() {
        Button signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {

    }

    public enum SignInType {
        GOOGLE("google"),
        FACEBOOK("facebook"),
        NONE("none");

        private String signInType;

        SignInType(String signInType) {
            this.signInType = signInType;
        }

        public String getSignInType() {
            return signInType;
        }

        @Override
        public String toString() {
            return "SignInType{" +
                    "signInType='" + signInType + '\'' +
                    '}';
        }
    }
}

