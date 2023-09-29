import java.util.Scanner;


public class Main {
    public static void main(String args[]) {

        Scanner sc = new Scanner(System.in);

        try {
            String input = sc.nextLine();

            String result = calc(input);
            System.out.println("Ответ: " + result);
        }catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
              }
        finally {
            sc.close();
        }

    }

     static String calc(String input) {
        String[] parts = input.split(" "); //разбиваем массив с помощью регуляроного выражения пробел
        if (parts.length != 3)  //если длина массива не равна 3, нам нужно что вводилось с клавиатуры: число-символ-число
        {
            throw new IllegalArgumentException("Неправильный формат выражения.");
        }
        int operand1;
        int operand2;
        char operator;

        try {
            operand1 = Integer.parseInt(parts[0]); //преобразует символ в цифру
            operand2 = Integer.parseInt(parts[2]);
            operator = parts[1].charAt(0); // метод charAt(index) - возвращает символ из массива строки

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Операнды должны быть целыми чмслами");
        }
        if ((operand1 < 1 || operand1 > 10) || (operand2 < 1 || operand2 > 10)) {
            throw new IllegalArgumentException("вводимые числа с клавитуры должны быть в диапазоне от 1 до 10 включительно.");
        }
        int result;
        switch (operator) {

            case '+':
                result = operand1 + operand2;
                break;
            case '-':
                result = operand1 - operand2;
                break;
            case '/':
                if (operand2 == 0) {
                    throw new IllegalArgumentException("делить на ноль нельзя");
                }
                result = operand1 / operand2;
                break;
            case '*':
                result = operand1 * operand2;
                break;
            default:
                throw new IllegalArgumentException("Другие операции пока наш калькулятор делать но умеет");

        }
        return String.valueOf(result);
    }

}