import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Example extends Asynox {
    @Test("Test range from 0 to 10, inclusive")
    public static void testRangeInclusive() {
        int[] result = range(0, 10, 2, true);
        assert result.length == 6 : "Expected length 6 for inclusive range.";
        assert result[0] == 0 : "First element should be 0.";
        assert result[5] == 10 : "Last element should be 10.";
    }

    @Test("Test range from 0 to 10, exclusive")
    public static void testRangeExclusive() {
        int[] result = range(0, 10, 2);
        assert result.length == 5 : "Expected length 5 for exclusive range.";
        assert result[0] == 0 : "First element should be 0.";
        assert result[4] == 8 : "Last element should be 8.";
    }

    @Test("Test range with negative step")
    public static void testRangeNegativeStep() {
        int[] result = range(10, 0, -2);
        assert result.length == 5 : "Expected length 5 for range with negative step.";
        assert result[0] == 10 : "First element should be 10.";
        assert result[4] == 2 : "Last element should be 2.";
    }

    @Test("Test range with descending order")
    public static void testRangeDescending() {
        int[] result = range(10, 0, -1, true);
        assert result.length == 11 : "Expected length 11 for inclusive descending range.";
        assert result[0] == 10 : "First element should be 10.";
        assert result[10] == 0 : "Last element should be 0.";
    }

    @Test("Test range with an invalid step")
    public static void testRangeInvalidStep() {
        int[] result = range(0, 10, 0);
        assert result.length == 0 : "Expected empty array for invalid step.";
    }

    @Test("Test IO files")
    public static void testIOFile() throws InterruptedException, ExecutionException, IOException {
        String filePath = ".\\file.txt";
        String writtenText = "Some text";

        writeFile(filePath, writtenText.getBytes(), true);
        assert writtenText.equals(new String(readFile(filePath))) : "Data was not written and read correctly";

        rm(filePath);
        boolean fileExists = false;
        for (String file : tree("."))
            if (file.equals(filePath))
                fileExists = true;

        assert !fileExists : "File was not removed";
    }

    @Test("Test math methods")
    public static void testMath() {
        assert Math.pow(10, 2) == fastPow(10, 2) : "Inconsistent output";
        assert Math.pow(-2, 3) == fastPow(-2, 3) : "Inconsistent output";
        assert Math.pow(2, -3) == fastPow(2, -3) : "Inconsistent output";
        assert (1 / Math.pow(0.5, 3)) == (1 / fastPow(0.5, 3)) : "Inconsistent output";
    }

    public static void main(String[] args) {
        start();
        test(Example.class);
        shutdown();
    }
}