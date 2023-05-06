public class Main {
    public static void main(String[] args) {
        long beforeTime = System.currentTimeMillis();
        while (true) {
            long afterTime = System.currentTimeMillis();
            if ((afterTime - beforeTime) / 1000 > 10) break;
        }
    }
}
