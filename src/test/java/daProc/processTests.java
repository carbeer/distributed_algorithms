package daProc;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class processTests {

    @Test
    public void test_getPeers() {
        File membershipPath = new File(System.getProperty("user.dir") + "/src/test/java/daProc/membership.txt");
        ArrayList<Process.Peer> expected = new ArrayList<Process.Peer>();
        expected.add(new Process.Peer("127.100.0.1", 11001));
        ArrayList<Process.Peer> result = Process.getPeers(membershipPath, 2);

        Assert.assertEquals(result.size(), expected.size());
        for (int i = 0; i < result.size(); i++) {
            Assert.assertTrue(expected.get(i).equals(result.get(i)));
        }
    }

    @Test void test_readMembership() {
        File membershipPath = new File(System.getProperty("user.dir") + "/src/test/java/daProc/membership.txt");
        String[] splitted = Process.readMembership(membershipPath, 2);

        String[] expected = {"2", "127.8.9.6", "10052"};
        Assert.assertEquals(splitted, expected);
    }
}
