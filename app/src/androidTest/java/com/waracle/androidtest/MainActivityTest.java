package com.waracle.androidtest;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.waracle.androidtest.TestUtils.matchToolbarTitle;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void actionBar_hasAppName() {
        CharSequence title = InstrumentationRegistry.getTargetContext()
                .getString(R.string.app_name);
        matchToolbarTitle(title)
                .check(matches(isDisplayed()));
    }

    @Test
    public void cakeList_isVisible() {
        onView(withId(R.id.rv_cakes))
                .check(matches(isDisplayed()));
    }

    @Test
    public void firstItemOnCakeList_isClickable() {
        onView(withId(R.id.rv_cakes))
                .perform(actionOnItemAtPosition(0, click()));
    }
}
