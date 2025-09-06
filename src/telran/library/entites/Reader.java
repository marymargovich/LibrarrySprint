package telran.library.entites;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Reader implements Serializable {
    private int readerId;
    private String name;
    private String phone;
    private LocalDate birthday;

    public Reader() {
    }

    public Reader(int readerId, String name, String phone, LocalDate birthday) {
        if( readerId>0)
            this.readerId = readerId;
        this.name = name;
        this.phone = phone;
        this.birthday = birthday;
    }

    public int getReaderId() {
        return readerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reader reader = (Reader) o;
        return readerId == reader.readerId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(readerId);
    }

    @Override
    public String toString() {
        final String RESET      = "\u001B[0m";
        final String BRIGHTBLUE = "\u001B[94m";

        return "Reader: " +
                BRIGHTBLUE + "readerId " + RESET + getReaderId() +
                ", " + BRIGHTBLUE + "name "     + RESET + getName() +
                ", " + BRIGHTBLUE + "phone "    + RESET + getPhone() +
                ", " + BRIGHTBLUE + "birthday " + RESET + getBirthday() +
                "\n";
    }
}
