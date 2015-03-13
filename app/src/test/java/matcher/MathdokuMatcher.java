package matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class MathdokuMatcher {
    public static Matcher notSameInstance(final Object expected) {
        if (expected == null) {
            throw new IllegalStateException("Expected value of Matcher notSameInstance cannot be null");
        }
        return new BaseMatcher() {
            protected Object theExpected = expected;

            public boolean matches(Object object) {
                return theExpected != object;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("not same instance as " + theExpected.toString());
            }
        };
    }
}
