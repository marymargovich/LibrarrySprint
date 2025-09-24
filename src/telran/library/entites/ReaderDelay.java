package telran.library.entites;

import java.util.Objects;

public class ReaderDelay {
    private Reader reader;
    private  int delay;


    public ReaderDelay() {
    }

    public ReaderDelay(Reader reader, int delay) {
        this.reader = reader;
        this.delay = delay;
    }

    public Reader getReader() {
        return reader;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReaderDelay that = (ReaderDelay) o;
        return delay == that.delay && Objects.equals(reader, that.reader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reader, delay);
    }

    @Override
    public String toString() {
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";

        return "ReaderDelay: " +
                CYAN + "delay " + RESET + getDelay() + ", " +
                CYAN + "reader " + RESET + getReader();
    }
}
