import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.homepage.MainActivity;
import com.example.homepage.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CartFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCartNavigation() {
        try {
            // Try clicking on bottom navigation item (if it exists)
            onView(withId(R.id.nav_dashboard)).perform(click());
        } catch (Exception e) {
            // If nav_dashboard is missing, fall back to overflow menu
            onView(withContentDescription("More options")).perform(click());
            onView(withText("Cart")).perform(click());
        }
    }
}