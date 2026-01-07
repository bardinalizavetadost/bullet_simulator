public class Time {
    public static double seconds() {
        return System.nanoTime() / 1e9;
    }
}
