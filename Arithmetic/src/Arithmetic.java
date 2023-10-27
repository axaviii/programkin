public class Arithmetic {
    private int a;
    private int b;

    public Arithmetic(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int sumNumber() {
        return a + b;
    }

    public int multiplicationNumber() {

        return a * b;
    }

    public int maxNumber() {

        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    public int minNumber() {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }

}
