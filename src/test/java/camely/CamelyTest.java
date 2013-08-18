package camely;

import org.junit.Test;

import static junit.framework.Assert.assertSame;

public class CamelyTest {

    @Test
    public void testSpelling() throws Exception {
        assertSame("My spelling is better!","Hello Lake!","Hello Lake!");

    }

    @Test
    public void testMyMath() {

        assertSame("My math is not working!", 1 + 1, 4);
    }


}
