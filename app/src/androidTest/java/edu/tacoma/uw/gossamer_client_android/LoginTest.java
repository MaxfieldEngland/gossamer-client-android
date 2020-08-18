package edu.tacoma.uw.gossamer_client_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tacoma.uw.gossamer_client_android.authenticate.LoginFragment;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.PostListActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule = new ActivityTestRule<>(SignInActivity.class);

    @Before
    public void goToLoginScreen() {

        SharedPreferences sharedPreferences = mActivityRule.getActivity().getSharedPreferences(mActivityRule.getActivity().getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false).commit();

        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);

    }

    @Test
    public void testLogin() {

        //Test credentials
        String email = "test2@uw.edu";
        String password = "test2";


        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.pwd_edit_text)).perform(typeText(password));

        Espresso.onView(ViewMatchers.withId(R.id.login_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.login_button)).perform(click());

        onView(ViewMatchers.withId(R.id.post_list_coord_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginBadCredentials() {

        String email = "test@uw.edu";
        String password = "IncorrectPassword";

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.pwd_edit_text)).perform(typeText(password));

        Espresso.onView(ViewMatchers.withId(R.id.login_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.login_button)).perform(click());




    }

}
