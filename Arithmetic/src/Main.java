import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println(" Введите два числа: ");
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();


        Arithmetic arithmetic = new Arithmetic(a, b);
        System.out.println("Сумма чисел:");
        System.out.println(arithmetic.sumNumber());
        System.out.println("Произведение чисел:");
        System.out.println(arithmetic.multiplicationNumber());
        System.out.println("Миниальное из двух чисел:");
        System.out.println(arithmetic.minNumber());
        System.out.println("Максимальное из двух чисел:");
        System.out.println(arithmetic.maxNumber());


    }
}