package edu.tacoma.uw.gossamer_client_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LoginTest {


    /** SharedPreferences object used to set and check login status. */
    private SharedPreferences sharedPrefs;

    /** Test rule for administering instrumentation testing across SignInActivity. */
    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule = new ActivityTestRule<>(SignInActivity.class);

    /** Before activity tests, ensure that we're logged out via sharedpreferences. */
    @Before
    public void goToLoginScreen() {

        sharedPrefs = mActivityRule.getActivity().getSharedPreferences(mActivityRule.getActivity().getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);
        sharedPrefs.edit().putBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false).commit();

        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);

    }

    /** Test successful login using existing test credentials. */
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

        //Use shared preferences to see if we logged in (expected: true)
        assertTrue(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /** Test that login fails with incorrect login credentials (existing email and bad login).
     * Failure is determined by server response. */
    @Test
    public void testLoginBadCredentials() {

        String email = "test@uw.edu";
        String password = "IncorrectPassword";

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.pwd_edit_text)).perform(typeText(password));

        Espresso.onView(ViewMatchers.withId(R.id.login_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.login_button)).perform(click());

        //Use shared preferences to see if we logged in (expected: false)
        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));
    }

    /**
     * Test that login fails when a bad email is given (Fails to match expected pattern).
     * Failure is determined by client conditions.
     */
    @Test
    public void testLoginInvalidEmail() {

        String email = "testuw.edu";
        String password = "test";
        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.pwd_edit_text)).perform(typeText(password));

        Espresso.onView(ViewMatchers.withId(R.id.login_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.login_button)).perform(click());

        //Use shared preferences to see if we logged in (expected: false)
        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /**
     * Test a successful registration, after navigating to the registration window, using a random-generated
     * email; note that there is a non-zero chance of failure, given that the test does not access the server.
     *
     * Chance of duplicate values should be highly improbable; with even somewhat manual clearing of server values,
     * chance of false failures (duplicate values vs problems in registration protocol) should be easily manageable.
     */
    @Test
    public void testRegisterSuccess() {

        //Generate two random numbers for email generation
        int rand1 = new Random().nextInt(100000);
        int rand2 = new Random().nextInt(100000);

        String username = "Test User #" + rand1;
        String email = "test" + rand1 + "@uw" + rand2 + ".edu";
        String password = "test";

        onView(withId(R.id.register_text_view)).perform(click());


        onView(withId(R.id.username_edit_text)).perform(typeText(username));
        Espresso.onView(ViewMatchers.withId(R.id.register_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.password_edit_text)).perform(typeText(password));
        onView(withId(R.id.confirm_pwd_edit_text)).perform(typeText(password));

        onView(withId(R.id.registration_button)).perform(click());

        assertTrue(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /**
     * Test that registration fails when given an email address that's already been taken.
     * (Uses test account test@uw.edu)
     */
    @Test
    public void testRegisterTakenEmail() {

        String username = "Test User";
        String email = "test@uw.edu"; //The taken email
        String password = "test";

        onView(withId(R.id.register_text_view)).perform(click());


        onView(withId(R.id.username_edit_text)).perform(typeText(username));
        Espresso.onView(ViewMatchers.withId(R.id.register_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.password_edit_text)).perform(typeText(password));
        onView(withId(R.id.confirm_pwd_edit_text)).perform(typeText(password));

        onView(withId(R.id.registration_button)).perform(click());

        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /**
     *  Test that registration fails when given an invalid email address.
     */
    @Test
    public void testRegisterBadEmail() {

        String username = "Test User";
        String email = "testuw.edu"; //The bad email
        String password = "test";

        onView(withId(R.id.register_text_view)).perform(click());


        onView(withId(R.id.username_edit_text)).perform(typeText(username));
        Espresso.onView(ViewMatchers.withId(R.id.register_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.password_edit_text)).perform(typeText(password));
        onView(withId(R.id.confirm_pwd_edit_text)).perform(typeText(password));

        onView(withId(R.id.registration_button)).perform(click());

        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /**
     * Tests that registration fails when obscured password fields do not match.
     */
    @Test
    public void testRegisterMismatchedPassword() {

        String username = "Test User";
        String email = "test@uw.edu"; //The taken email
        String password = "test";
        String passwordMismatch = "taste";

        onView(withId(R.id.register_text_view)).perform(click());


        onView(withId(R.id.username_edit_text)).perform(typeText(username));
        Espresso.onView(ViewMatchers.withId(R.id.register_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.password_edit_text)).perform(typeText(password));
        onView(withId(R.id.confirm_pwd_edit_text)).perform(typeText(passwordMismatch));

        onView(withId(R.id.registration_button)).perform(click());

        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

    /**
     * Tests that registration fails when a displayname is not given; including a displayname that only
     * consists of whitespace.
     */
    @Test
    public void testRegisterNoDisplayName() {

        String username = " "; //Even with whitespace, the username should hopefully still qualify as 'empty';
        // this check is made explicitly in the code.
        String email = "test@uw.edu"; //The taken email
        String password = "test";

        onView(withId(R.id.register_text_view)).perform(click());


        onView(withId(R.id.username_edit_text)).perform(typeText(username));
        Espresso.onView(ViewMatchers.withId(R.id.register_layout)).perform(ViewActions.swipeUp());

        onView(withId(R.id.email_edit_text)).perform(typeText(email));
        onView(withId(R.id.password_edit_text)).perform(typeText(password));
        onView(withId(R.id.confirm_pwd_edit_text)).perform(typeText(password));

        onView(withId(R.id.registration_button)).perform(click());

        assertFalse(sharedPrefs.getBoolean(mActivityRule.getActivity().getString(R.string.LOGGEDIN), false));

    }

}
