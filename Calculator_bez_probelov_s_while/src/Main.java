import java.util.Scanner;


public class Main {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String input = sc.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Выход из программы.");
                break; // Завершение программы при вводе "exit"
            }

            try {
                String result = calculate(input);
                System.out.println("Ответ: " + result);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка " + e.getMessage());
            }

            }
        sc.close();
      }


    static String calculate(String input) {
        char operator = findOperator(input);
        String[] operands = input.split("\\" + operator);
        if (operands.length !=2){
            throw new IllegalArgumentException("Неверный формат выражения.");
        }
        int operand1 = Integer.parseInt(operands[0]);
        int operand2 = Integer.parseInt(operands[1]);

        if ((operand1 < 1 || operand1 > 10) || (operand2 < 1 || operand2 > 10)) {
            throw new IllegalArgumentException("Числа должны быть в диапазоне от 1 до 10 включительно.");
        }
        int result;

        switch (operator){
            case '+' : result = operand1 + operand2;
            break;
            case '-' : result = operand1 - operand2;
                break;
            case '/' :
                if(operand2 ==0){throw new IllegalArgumentException("на ноль делить нельзя");}
                    result = operand1 / operand2;
                break;
            case '*' : result = operand1*operand2;
                break;
            default: throw new IllegalArgumentException("Пока калькулятор умеет делать только эти операции");
        }
        return String.valueOf(result);
    }

     static char findOperator(String input) // поиск первого встечного оператора " + - / *
    {
        for(char operator : "+-/*".toCharArray()) // с помощью toCharArray  мы перевоим строку в массив чаров
            {
            if (input.contains(String.valueOf(operator))){
                return operator;
            }

        }
        throw new IllegalArgumentException("Оператор не найден");
    }
}