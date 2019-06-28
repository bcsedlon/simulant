package cz.growmat.android.simulant;

public class DataHolder {
    private float parX0, parX1, parY0, parY1, k, d;
    public float x;
    public String name, unit;

    public float getX0() {return parX0;};
    public float getX1() {return parX1;};
    public float getY0() {return parY0;};
    public float getY1() {return parY1;};
    public float getK() {return k;};
    public float getD() {return d;};

    public float getY(float x) {
        this.x = x;
        return getY();
    }
    public float getY() {
         return x * k + d;
    }

    private void calc() {
        k = (parY1 - parY0) / (parX1 - parX0);
        d = parY1 - k * parX1;
    }

    public void setX0(float f) {
        parX0 = f;
        calc();
    };
    public void setX1(float f) {
        parX1 = f;
        calc();
    };
    public void setY0(float f) {
        parY0 = f;
        calc();
    };
    public void setY1(float f) {
        parY1 = f;
        calc();
    };

    private double data[] = new double[8] ;
    public double getData(int i) {return data[i];}
    public void setData(int i, double data) {this.data[i] = data;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
