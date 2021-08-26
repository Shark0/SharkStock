package person.shark.stock.util;

public class StringUtil {
    public static boolean isEmpty(String string) {
        return (string == null || string.trim().length() == 0);
    }
}
