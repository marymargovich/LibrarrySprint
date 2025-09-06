package telran.library.models;


public abstract class AbstractLibrary implements ILibrary {
    protected int maxPickPeriod;
    protected int minPickPeriod;


    public AbstractLibrary() {
        maxPickPeriod = 30;
        minPickPeriod = 3;
    }


    public int getMaxPickPeriod() {
        return maxPickPeriod;
    }


    public void setMaxPickPeriod(int maxPickPeriod) {
        if (maxPickPeriod > 0)
            this.maxPickPeriod = maxPickPeriod;
    }


    public int getMinPickPeriod() {
        return minPickPeriod;
    }


    public void setMinPickPeriod(int minPickPeriod) {
        if (minPickPeriod > 0)
            this.minPickPeriod = minPickPeriod;
    }


}
