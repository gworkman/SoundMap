package edu.osu.sphs.soundmap.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import edu.osu.sphs.soundmap.R;
import edu.osu.sphs.soundmap.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, OnCompleteListener {

    private Context context;
    private TextView title;
    private Button loginButton, createButton, resetButton, backButton;
    private TextInputLayout emailLayout, passLayout, retypeLayout;
    private TextInputEditText email, pass, retype;
    private State state;
    private FirebaseAuth auth;


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loginButton = view.findViewById(R.id.login_button);
        createButton = view.findViewById(R.id.create_account_button);
        resetButton = view.findViewById(R.id.reset_account_button);
        backButton = view.findViewById(R.id.back_button);

        loginButton.setOnClickListener(this);
        createButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        email = view.findViewById(R.id.username_input);
        pass = view.findViewById(R.id.password_input);
        retype = view.findViewById(R.id.retype_password_input);
        title = view.findViewById(R.id.title_text);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        retype.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                retypeLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailLayout = view.findViewById(R.id.username_layout);
        passLayout = view.findViewById(R.id.password_layout);
        retypeLayout = view.findViewById(R.id.retype_password_layout);

        state = State.STATE_LOGIN;
        context = getContext();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_button:
                state = State.STATE_LOGIN;
                title.setText(R.string.login);
                backButton.setVisibility(View.GONE);
                createButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
                passLayout.setVisibility(View.VISIBLE);
                retypeLayout.setVisibility(View.GONE);
                break;

            case R.id.reset_account_button:
                state = State.STATE_RESET;
                title.setText(R.string.reset_password);
                backButton.setVisibility(View.VISIBLE);
                createButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);
                passLayout.setVisibility(View.GONE);
                break;

            case R.id.create_account_button:
                state = State.STATE_CREATE;
                title.setText(R.string.create_an_account);
                backButton.setVisibility(View.VISIBLE);
                createButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);
                retypeLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.login_button:
                String emailString = email.getText().toString();
                String passString = pass.getText().toString();
                String retypeString = retype.getText().toString();
                if (state.validate(emailString, passString, retypeString)) {
                    state.process(this.auth, this, email.getText().toString(), pass.getText().toString(), retype.getText().toString());
                } else {
                    switch (state) {
                        case STATE_LOGIN:
                            if (!state.validEmail(emailString))
                                emailLayout.setError("Not a valid email address");
                            if (!state.validPass(passString))
                                passLayout.setError("Password must be at least 8 letters and numbers");
                            break;
                        case STATE_RESET:
                            emailLayout.setError("Not a valid email address");
                            break;
                        case STATE_CREATE:
                            if (!state.validEmail(emailString)) {
                                emailLayout.setError("Not a valid email address");
                            } else if (!state.validPass(passString)) {
                                passLayout.setError("Password must be 8 or more characters");
                            } else {
                                retypeLayout.setError("Passwords do not match");
                            }
                    }
                }
                break;
        }
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (!task.isSuccessful()) {
            ((MainActivity) getActivity()).setErrorMessage(task.getException().getMessage());
        }
    }

    private enum State {

        STATE_LOGIN {
            // email first, then password
            boolean validate(String... inputs) {
                return validEmail(inputs[0]) && validPass(inputs[1]);
            }

            boolean process(FirebaseAuth auth, OnCompleteListener listener, String... inputs) {
                auth.signInWithEmailAndPassword(inputs[0], inputs[1]).addOnCompleteListener(listener);
                return true;
            }
        },
        STATE_RESET {
            // email
            boolean validate(String... inputs) {
                return validEmail(inputs[0]);
            }

            boolean process(FirebaseAuth auth, OnCompleteListener listener, String... inputs) {
                auth.sendPasswordResetEmail(inputs[0]).addOnCompleteListener(listener);
                return true;
            }
        },
        STATE_CREATE {
            // email, then password, then retype
            boolean validate(String... inputs) {
                return validEmail(inputs[0]) && validPass(inputs[1]) && inputs[1].equals(inputs[2]);
            }

            boolean process(FirebaseAuth auth, OnCompleteListener listener, String... inputs) {
                auth.createUserWithEmailAndPassword(inputs[0], inputs[1]).addOnCompleteListener(listener);
                return true;
            }
        };

        boolean validate(String... inputs) {
            return false;
        }

        boolean process(FirebaseAuth auth, OnCompleteListener listener, String... inputs) {
            return false;
        }

        boolean validEmail(String email) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }

        boolean validPass(String pass) {
            return pass.length() > 7 && pass.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        }
    }

}
