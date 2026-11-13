package spinloki.TreasureHunt.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Suite implements Runnable {
    public interface Body {
        void run() throws Exception;
    }

    private final List<String> results = new ArrayList<>();
    private int failed;

    protected abstract void tests() throws Exception;

    @Override
    public void run() {
        World.prepare();
        try {
            tests();
        } catch (Throwable t) {
            record("FAIL", getClass().getSimpleName() + " setup", describe(t));
        }
        System.out.println("[TH-TEST] " + getClass().getSimpleName() + ": "
                + (results.size() - failed) + " passed, " + failed + " failed");
        String out = System.getProperty("th.test.results");
        if (out == null) return;
        try (PrintWriter w = new PrintWriter(new FileWriter(out, true))) {
            for (String line : results) w.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void test(String name, Body body) {
        try {
            body.run();
            record("PASS", name, null);
        } catch (Throwable t) {
            record("FAIL", name, describe(t));
        }
    }

    private void record(String status, String name, String detail) {
        if ("FAIL".equals(status)) failed++;
        String line = status + " " + getClass().getSimpleName() + " > " + name
                + (detail == null ? "" : "\n" + detail.indent(4).stripTrailing());
        results.add(line);
        System.out.println("[TH-TEST] " + line);
    }

    private static String describe(Throwable t) {
        if (t instanceof AssertionError && t.getCause() == null) return t.getMessage();
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String[] lines = sw.toString().split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, 25); i++) sb.append(lines[i]).append('\n');
        return sb.toString();
    }

    public static void check(boolean condition, String message, Object... args) {
        if (!condition) throw new AssertionError(String.format(message, args));
    }

    public static void eq(Object expected, Object actual, String what) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(what + ": expected " + expected + " but was " + actual);
        }
    }

    public static <T> T notNull(T value, String what) {
        if (value == null) throw new AssertionError(what + " was null");
        return value;
    }
}
