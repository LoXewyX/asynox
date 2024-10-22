import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@code Asynox} class provides a collection of utility methods and
 * functionality
 * that simplify various tasks such as file reading/writing, benchmarking, and
 * process
 * execution. This class is designed for ease of use with methods that handle
 * common
 * operations efficiently, including file operations with parallel processing
 * and
 * benchmarking utilities for performance testing.
 *
 * <p>
 * Key features of the {@code Asynox} class include:
 * <ul>
 * <li>File I/O operations with multithreading support for large files.</li>
 * <li>Utility methods for benchmarking and performance comparison.</li>
 * <li>Process management with the ability to start and stop processes.</li>
 * <li>Output queueing system that handles formatted printing of results.</li>
 * </ul>
 *
 * <p>
 * Methods in this class use multi-threading and optimal memory management to
 * ensure efficient handling of large data sets and files. The class is ideal
 * for
 * applications that require high-performance file operations and benchmarking
 * utilities.
 *
 * <p>
 * Usage examples:
 *
 * <pre>
 * {@code
 *
 * // Example of starting code
 * Asynox.start();
 *
 * // Example of reading a file as bytes
 * byte[] fileData = Asynox.readFile("path/to/file");
 *
 * // Example of writing data to a file
 * Asynox.writeFile("path/to/file", fileData);
 *
 * // Example of benchmarking two tasks
 * Asynox.benchmark(() -> {
 *     slowTask();
 * }, () -> {
 *     fastTask();
 * });
 *
 * // Example of shutdown code
 * Asynox.shutdown();
 * </pre>
 *
 * <p>
 * This class cannot be instantiated as it contains only static utility methods.
 * It serves as a general-purpose toolset for a variety of system-level tasks.
 *
 * @author Luis Ruiz Caracuel (LoXewyX)
 * @version 1.0
 */
public class Asynox {
  private static final PrintWriter writer;
  private static final BufferedReader reader;
  private static final StringBuilder sb;
  private static final LinkedBlockingQueue<Command> queue;
  private static final Thread thread;
  private static final ExecutorService exec;
  private static volatile boolean running;

  private enum CommandType { END, PRINT, PRINTLN, RUNNABLE }

  private static class Command {
    CommandType type;
    Object data;

    Command(CommandType type, Object data) {
      this.type = type;
      this.data = data;
    }
  }

  static {
    running = true;
    writer =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
    reader = new BufferedReader(new InputStreamReader(System.in));
    sb = new StringBuilder();
    queue = new LinkedBlockingQueue<>();
    exec = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    thread = new Thread(() -> {
      try {
        while (running) {
          Command command = queue.take();
          switch (command.type) {
            case END:
              running = false;
              break;
            case PRINT:
              writer.print((String) command.data);
              writer.flush();
              break;
            case PRINTLN:
              writer.println((String) command.data);
              writer.flush();
              break;
            case RUNNABLE:
              ((Runnable) command.data).run();
              break;
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    });

    thread.setDaemon(true);
  }

  /**
   * Starts the background thread responsible for handling multithreading
   * operations.
   */
  public static void start() {
    thread.start();
  }

  /**
   * Adds a Runnable task to the command queue for execution in a background
   * thread.
   *
   * This method allows you to submit a piece of code encapsulated in a Runnable
   * that will be executed asynchronously by the dedicated thread managing the
   * command queue. The task will run in the order it was added, ensuring that
   * all tasks are executed sequentially without blocking the main thread.
   *
   * @param fragment The Runnable task to be executed.
   */
  public static void add(Runnable runnable) {
    if (runnable != null) {
      Future<?> future = exec.submit(runnable);
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Waits for the flush thread to complete and shuts down the executor service
   * and closes all processes.
   */
  public static void shutdown() {
    queue.add(new Command(CommandType.END, null));
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    } finally {
      exec.shutdown();
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      writer.close();
    }
  }

  /**
   * Returns the simple class name of the object passed as the argument.
   *
   * @param val Object to determine the type of.
   * @param <T> Type of the object.
   * @return The simple name of the class of the object
   */
  public static <T> String type(T val) {
    if (val == null) return "null";
    return val.getClass().getSimpleName();
  }

  /**
   * Returns the memory identity hash code of the object.
   *
   * @param obj Object whose identity hash code is to be returned.
   * @return Identity hash code of the object.
   */
  public static int memo(Object obj) {
    return System.identityHashCode(obj);
  }

  /**
   * Converts an integer to its hexadecimal string representation.
   *
   * @param n The integer to convert.
   * @return Hexadecimal string representation of the integer.
   */
  public static String hex(int n) {
    return Integer.toHexString(n);
  }

  /**
   * Converts a long to its hexadecimal string representation.
   *
   * @param n The long to convert.
   * @return Hexadecimal string representation of the long.
   */
  public static String hex(Long n) {
    return Long.toHexString(n);
  }
  
  /**
   * Calculates the power of a given base raised to an exponent using an efficient algorithm.
   * This implementation supports both positive and negative exponents. 
   * When the exponent is negative, it computes the reciprocal of the base raised to the positive exponent.
   * 
   * <p>
   * This method handles the special case when the exponent is 2 by directly returning 
   * the square of the base. For other exponents, it uses an iterative approach based on
   * exponentiation by squaring, which has a logarithmic time complexity.
   * </p>
   *
   * @param base The base value to be raised to the power of the exponent. This can be any double value.
   * @param exp The exponent to which the base is raised. This can be any long value (positive or negative).
   * @return The result of base raised to the power of exp. If exp is negative, it returns the reciprocal of the result.
   */
  public static double fastPow(double base, long exp) {
    if (exp < 0) {
      base = 1 / base;
      exp = -exp;
    }

    if (exp == 2)
      return base * base;

    double res = 1;
    while (exp > 0) {
      if ((exp & 1) == 1)
        res *= base;
      exp >>= 1;
      base *= base;
    }

    return res;
  }

  /**
   * Handles printing various types of arguments including arrays, converting
   * them into a string format.
   *
   * @param <T> The type of the argument.
   * @param arg Argument to handle.
   */
  private static <T> void handlePrintArgs(T arg) {
    if (arg == null)
      sb.append("null");
    else if (arg.getClass().isArray()) {
      Class<?> componentType = arg.getClass().getComponentType();
      if (componentType == int.class)
        sb.append(Arrays.toString((int[]) arg));
      else if (componentType == long.class)
        sb.append(Arrays.toString((long[]) arg));
      else if (componentType == double.class)
        sb.append(Arrays.toString((double[]) arg));
      else if (componentType == float.class)
        sb.append(Arrays.toString((float[]) arg));
      else if (componentType == byte.class)
        sb.append(Arrays.toString((byte[]) arg));
      else if (componentType == short.class)
        sb.append(Arrays.toString((short[]) arg));
      else if (componentType == char.class)
        sb.append(Arrays.toString((char[]) arg));
      else if (componentType == boolean.class)
        sb.append(Arrays.toString((boolean[]) arg));
      else
        sb.append(Arrays.deepToString((Object[]) arg));
    } else
      sb.append(arg);
  }

  /**
   * Prints the given arguments without a newline.
   *
   * @param args The arguments to print.
   * @param <T>  The type of the arguments.
   */
  @SafeVarargs
  public static <T> void print(T... args) {
    for (T arg : args) handlePrintArgs(arg);

    queue.add(new Command(CommandType.PRINT, sb.toString()));
    sb.setLength(0);
  }

  /**
   * Prints the given arguments with a newline.
   *
   * @param args The arguments to print.
   * @param <T>  The type of the arguments.
   */
  @SafeVarargs
  public static <T> void println(T... args) {
    for (T arg : args) handlePrintArgs(arg);

    queue.add(new Command(CommandType.PRINTLN, sb.toString()));
    sb.setLength(0);
  }

  /**
   * Reads input from the console and converts it to the specified type.
   *
   * This method reads a line of input from the standard input stream,
   * converting it to the specified type. If the input is empty or null,
   * it returns null. If the specified class type is not supported,
   * it returns the input as a String.
   *
   * @param clazz The Class object representing the type to which the input
   *              should be converted. Supported types include:
   *              <ul>
   *                  <li>Integer.class</li>
   *                  <li>Double.class</li>
   *                  <li>Boolean.class</li>
   *                  <li>Long.class</li>
   *                  <li>Float.class</li>
   *                  <li>Character.class (requires single character input)</li>
   *                  <li>Short.class</li>
   *                  <li>Any other type will be returned as a String.</li>
   *              </ul>
   * @param <T>   The type of the return value.
   * @return      The input converted to the specified type, or null if the
   *              input is empty.
   * @throws IllegalArgumentException if the input cannot be converted to
   *                                   the specified type (e.g., invalid format
   *                                   for numeric types or if a single
   * character is expected but not provided).
   */
  @SuppressWarnings("unchecked")
  public static <T> T input(Class<T> clazz) {
    try {
      String input = reader.readLine();

      if (input == null || input.isEmpty())
        return null;

      if (clazz == Integer.class)
        return (T) Integer.valueOf(input);
      else if (clazz == Double.class)
        return (T) Double.valueOf(input);
      else if (clazz == Boolean.class)
        return (T) Boolean.valueOf(input);
      else if (clazz == Long.class)
        return (T) Long.valueOf(input);
      else if (clazz == Float.class)
        return (T) Float.valueOf(input);
      else if (clazz == Character.class)
        if (input.length() == 1)
          return (T) Character.valueOf(input.charAt(0));
        else
          throw new IllegalArgumentException(
              "Input must be a single character.");
      else if (clazz == Short.class)
        return (T) Short.valueOf(input);
      else
        return (T) input;
    } catch (IOException | IllegalArgumentException e) {
      e.printStackTrace();
      shutdown();
    }

    return null;
  }

  /**
   * Reads input from the console and returns it as a String.
   *
   * This method serves as a convenience method for reading input when
   * no specific type conversion is required. It calls the main input
   * method with String.class, allowing users to read input directly
   * as a String without needing to specify the type.
   *
   * @param <T> The type of the return value, which will be String.
   * @return    The input read from the console as a String.
   */
  @SuppressWarnings("unchecked")
  public static <T> T input() {
    return (T) input(String.class);
  }

  /**
   * Generates a range of integers from {@code from} to {@code to} with a
   * specified step size and inclusion of the upper bound.
   *
   * @param from      Starting number.
   * @param to        Ending number.
   * @param each      Step size.
   * @param inclusive Whether to include the upper bound.
   * @return Array of integers representing the range.
   */
  public static int[] range(int from, int to, int each, boolean inclusive) {
    if (each == 0)
      return new int[0];

    int size = 0;
    boolean isDescending = from > to;
    if (isDescending) {
      if (each > 0)
        return new int[0];
      size = inclusive ? (from - to) / Math.abs(each) + 1
                       : (from - to - 1) / Math.abs(each) + 1;
    } else {
      if (each < 0)
        return new int[0];
      size = inclusive ? (to - from) / each + 1 : (to - from - 1) / each + 1;
    }
    if (size <= 0)
      return new int[0];

    int[] nums = new int[size];
    for (int i = 0, value = from;
         isDescending    ? inclusive ? value >= to : value > to
             : inclusive ? value <= to
                         : value < to;
         i++, value += each)
      nums[i] = value;

    return nums;
  }

  /**
   * Generates a range of integers from {@code from} to {@code to} with a
   * specified step size.
   *
   * @param from      Starting number.
   * @param to        Ending number.
   * @param each      Step size.
   * @return Array of integers representing the range.
   */
  public static int[] range(int from, int to, int each) {
    return range(from, to, each, false);
  }

  /**
   * Generates a range of integers from {@code from} to {@code to} with an
   * inclusion of the upper bound.
   *
   * @param from      Starting number.
   * @param to        Ending number.
   * @param inclusive Whether to include the upper bound.
   * @return Array of integers representing the range.
   */
  public static int[] range(int from, int to, boolean inclusive) {
    return range(from, to, 1, inclusive);
  }

  /**
   * Generates a range of integers from {@code from} to {@code to}.
   *
   * @param from      Starting number.
   * @param to        Ending number.
   * @return Array of integers representing the range.
   */
  public static int[] range(int from, int to) {
    return range(from, to, 1, false);
  }

  /**
   * Generates a range of integers from 0 to {@code to} with an
   * inclusion of the upper bound.
   *
   * @param to        Ending number.
   * @param inclusive Whether to include the upper bound.
   * @return Array of integers representing the range.
   */
  public static int[] range(int to, boolean inclusive) {
    return range(0, to, 1, inclusive);
  }

  /**
   * Generates a range of integers from 0 to {@code to}.
   *
   * @param to Ending number.
   * @return Array of integers representing the range.
   */
  public static int[] range(int to) {
    return range(0, to, 1, false);
  }

  /**
   * Measures the time taken to run a snippet of code.
   *
   * @param snippet The code to measure.
   * @return Time taken in nanoseconds.
   */
  private static long measureTime(Runnable snippet) {
    long start = System.nanoTime();
    snippet.run();
    return System.nanoTime() - start;
  }

  /**
   * Benchmarks two pieces of code and prints the difference in execution time
   * in milliseconds.
   *
   * @param slower The first code to compare.
   * @param faster The second (expected faster) code to compare.
   */
  public static void benchmark(Runnable slower, Runnable faster) {
    long slw = measureTime(slower) / 1000000;
    long fst = measureTime(faster) / 1000000;
    String timeStr =
        String.format("Difference: %dms - %dms = %dms", slw, fst, (slw - fst));
    int width = timeStr.length() + 6;
    StringBuilder border = new StringBuilder(width);
    border.append('+');
    for (int i = 0; i < width - 2; i++) border.append('-');
    border.append('+');

    queue.add(new Command(CommandType.PRINTLN, ""));
    queue.add(new Command(CommandType.PRINTLN, border.toString()));
    queue.add(
        new Command(CommandType.PRINTLN, String.format("|  %s  |", timeStr)));
    queue.add(new Command(CommandType.PRINTLN, border.toString()));
  }

  /**
   * Determines the optimal chunk size for file operations based on file size.
   * The chunk size is calculated based on file size, with larger files having a
   * smaller
   * percentage of the file used for chunking. This helps balance performance
   * for both small and large files.
   *
   * @param fileSize The size of the file in bytes.
   * @return The calculated optimal chunk size, constrained between 256KB and
   *         128MB.
   */
  private static int optimalChunkSize(long fileSize) {
    double scalingFactor;
    if (fileSize < 1073741824L) { // < 1 GB
      scalingFactor = 0.10; // 10%
    } else if (fileSize < 10737418240L) { // < 10 GB
      scalingFactor = 0.05; // 5%
    } else if (fileSize < 53687091200L) { // < 50 GB
      scalingFactor = 0.03; // 3%
    } else if (fileSize < 107374182400L) { // < 100 GB
      scalingFactor = 0.025; // 2.5%
    } else if (fileSize < 536870912000L) { // < 500 GB
      scalingFactor = 0.02; // 2%
    } else {
      scalingFactor = 0.015; // 1.5% for files >= 500 GB
    }
    return (int) Math.max(
        262144, Math.min(134217728, fileSize * scalingFactor));
  }

  /**
   * Task to read a chunk of data to a file at a specific position using
   * `RandomAccessFile`.
   * This is used for parallel file writing in chunks, improving performance by
   * utilizing multiple threads to read different portions concurrently.
   */
  private static class FileReadTask implements Runnable {
    private final String filePath;
    private final long start;
    private final long end;
    private final byte[] fileData;

    /**
     * Constructor for a file read task.
     *
     * @param filePath The path to the file where data will be written.
     * @param data     The data chunk to be written to the file.
     * @param position The position in the file to start writing.
     */
    public FileReadTask(String filePath, long start, long end, byte[] fileData) {
      this.filePath = filePath;
      this.start = start;
      this.end = end;
      this.fileData = fileData;
    }

    @Override
    public void run() {
      try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
        file.seek(start);
        int bufferSize = (int) (end - start);
        if (bufferSize <= 0) return;

        byte[] buffer = new byte[bufferSize];
        file.readFully(buffer);

        System.arraycopy(buffer, 0, fileData, (int) start, bufferSize);
      } catch (IOException e) {
        e.printStackTrace();
        shutdown();
      }
    }
  }

  /**
   * Reads a file concurrently by splitting it into chunks that are read by
   * multiple threads.
   *
   * @param filePath The path of the file to be read.
   * @return The content of the file as a single string.
   * @throws IOException If an I/O error occurs.
   * @throws InterruptedException If the thread execution is interrupted.
   * @throws ExecutionException If an exception occurs while executing the file
   *     read task.
   */
  public static byte[] readFile(String filePath)
    throws IOException, InterruptedException, ExecutionException {
    long fileLength = Paths.get(filePath).toFile().length();
    int chunkSize = optimalChunkSize(fileLength);
    int chunks = (int) Math.ceil((double) fileLength / chunkSize);
    byte[] fileData = new byte[(int) fileLength];
    
    Future<?>[] futures = new Future<?>[chunks];
    
    for (int i = 0; i < chunks; i++) {
      long start = i * chunkSize;
      long end = Math.min(start + chunkSize, fileLength);
      futures[i] = exec.submit(new FileReadTask(filePath, start, end, fileData));
    }

    for (Future<?> future : futures) future.get();
    
    return fileData;
  }

  /**
   * Task to write a chunk of data to a file at a specific position using
   * `RandomAccessFile`.
   * This is used for parallel file reading in chunks, improving performance by
   * utilizing multiple threads to write different portions concurrently.
   */
  private static class FileWriteTask implements Runnable {
    private final String filePath;
    private final byte[] data;
    private final long position;

    /**
     * Constructor for a file write task.
     *
     * @param filePath The path to the file where data will be written.
     * @param data     The data chunk to be written to the file.
     * @param position The position in the file to start writing.
     */
    public FileWriteTask(String filePath, byte[] data, long position) {
      this.filePath = filePath;
      this.data = data;
      this.position = position;
    }

    @Override
    public void run() {
      try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
        file.seek(position);
        file.write(data);
      } catch (IOException e) {
        e.printStackTrace();
        shutdown();
      }
    }
  }

  /**
   * Writes a file concurrently by splitting it into chunks that are read by
   * multiple threads.
   *
   * @param filePath  Path to the file where data will be written.
   * @param content   Byte array representing the content to be written.
   * @param overwrite If true, the file will be overwritten. Otherwise, the
   *                  content will be appended.
   * @throws InterruptedException If the thread is interrupted during execution.
   * @throws ExecutionException   If an error occurs during the execution of the
   *                              file write tasks.
   * @throws IOException          If an I/O error occurs.
   */
  public static void writeFile(
      String filePath, byte[] content, boolean overwrite)
      throws InterruptedException, ExecutionException, IOException {
    long fileLength = overwrite ? 0 : Paths.get(filePath).toFile().length();
    long writeStartPos = overwrite ? 0 : fileLength;
    long totalWriteSize = content.length;

    if (overwrite)
      try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
        file.setLength(0);
      }

    int chunkSize = optimalChunkSize(totalWriteSize);
    int chunks = (int) Math.ceil((double) totalWriteSize / chunkSize);
    Future<?>[] futures = new Future<?>[chunks];

    for (int i = 0; i < chunks; i++) {
      long start = writeStartPos + (i * chunkSize);
      int length = (int) Math.min(chunkSize, totalWriteSize - i * chunkSize);
      byte[] chunkData = new byte[length];
      System.arraycopy(content, i * chunkSize, chunkData, 0, length);
      futures[i] = exec.submit(new FileWriteTask(filePath, chunkData, start));
    }

    for (Future<?> future : futures) future.get();
  }

  /**
   * Writes the provided content to the file. By default, this method appends
   * the content to the existing file.
   *
   * @param filePath Path to the file where data will be written.
   * @param content  Byte array representing the content to be written.
   * @throws InterruptedException If the thread is interrupted during execution.
   * @throws ExecutionException   If an error occurs during the execution of the
   *                              file write tasks.
   * @throws IOException          If an I/O error occurs.
   */
  public static void writeFile(String filePath, byte[] content)
      throws InterruptedException, ExecutionException, IOException {
    writeFile(filePath, content, false);
  }

  /**
   * Creates a directory.
   *
   * @param path Where the directory should be created.
   */
  public static void mkdir(String path) {
    Path p = Paths.get(path);
    try {
      if (!Files.exists(p))
        Files.createDirectory(p);
    } catch (IOException e) {
      e.printStackTrace();
      shutdown();
    }
  }

  /**
   * Removes a file or directory (including non-empty directories).
   *
   * @param pathString File or directory to be removed.
   * @throws IOException If an I/O error occurs.
   */
  public static void rm(String path) throws IOException {
    Path p = Paths.get(path);

    if (Files.exists(p))
      if (Files.isDirectory(p)) {
        Files.walk(p).sorted(Comparator.reverseOrder()).forEach(_p -> {
          try {
            Files.delete(_p);
          } catch (IOException e) {
            e.printStackTrace();
            shutdown();
          }
        });
      } else
        Files.delete(p);
  }

  /**
   * Returns a string array representation of the directory tree starting from
   * the specified directory.
   *
   * @param dir Starting directory.
   * @return String array representation of the directory tree.
   * @throws IOException If an I/O error occurs.
   */
  public static List<String> tree(String path) throws IOException {
    List<String> treeList = new ArrayList<>();
    Files.walk(Paths.get(path)).forEach(p -> treeList.add(p.toString()));

    return treeList;
  }

  /**
   * Annotation for marking test methods.
   *
   * <p>Use this annotation to indicate that a method is a test case that should
   * be executed by the testing framework. Each method marked with this
   * annotation will be invoked during the test execution process.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   *     &#64;Test("Description of the test")
   *     public static void myTest() {
   *         // Test logic here
   *     }
   * </pre>
   *
   * <p>Note: Ensure that you enable assertions by running your program with the
   * {@code -ea} parameter (e.g., {@code java -ea MyTestClass}) to make the
   * assertions within the test methods effective.</p>
   *
   * @param value A string describing the test case.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Test {
    String value();
  }

  /**
   * Executes all methods annotated with the {@link Test} annotation in the
   * specified classes.
   *
   * <p>This method scans the provided classes for any methods annotated with
   * the {@code @Test} annotation and invokes them. If a test method completes
   * successfully, it logs a success message; if it fails, it logs an error
   * message along with the exception details.</p>
   *
   * <p>Example usage:</p>
   * <pre>
   *     test(MyTestClass.class, AnotherTestClass.class);
   * </pre>
   *
   * <p>To ensure proper operation of the tests, it is necessary to run the
   * program with assertions enabled using the {@code java -ea} parameters.</p>
   *
   * @param classes One or more classes containing methods annotated with {@link
   *     Test}.
   */
  public static void test(Class<?>... classes) {
    for (Class<?> testClass : classes) {
      Method[] methods = testClass.getDeclaredMethods();
      for (Method method : methods)
        if (method.isAnnotationPresent(Test.class))
          try {
            method.invoke(null);
            queue.add(new Command(CommandType.PRINTLN,
                sb.append("[DONE] ")
                  .append(method.getName())
                  .append(".")
                  .toString()));
            sb.setLength(0);
          } catch (Exception e) {
            queue.add(new Command(CommandType.PRINTLN,
                sb.append("[FAIL] ")
                  .append(method.getName())
                  .append(": ")
                  .append(e.getMessage())
                  .toString()));
            sb.setLength(0);
          }
    }
  }
}