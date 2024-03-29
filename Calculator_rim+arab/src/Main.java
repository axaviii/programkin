import java.util.*;

class Calculator {
    public static String calc(String input) {
        // разбиваем input на составные части посредством арифметических символов (+, -, *, /)
        String[] parts = input.split("[\\+\\-\\*\\/]");

        // делаем проверку чтоб у нас было только двечасти массива
        if (parts.length != 2) {
            throw new IllegalArgumentException("арифметическое выражение может состоять тоько из 2 чисел.");
        }

        // парсим каждую часть и убираем пробелы и помещаем значения в операнды
        String operand1 = parts[0].trim();
        String operand2 = parts[1].trim();

        // определяем операнды чтоб были или все арабские или все римские
        boolean isRoman = isRomanNumeral(operand1) && isRomanNumeral(operand2);
        boolean isArabic = isArabicNumeral(operand1) && isArabicNumeral(operand2);



        if (!(isRoman || isArabic)) {
            throw new IllegalArgumentException("Калькулятор может работать только римскими или только с арабскими цифрами ");
        }

        int num1, num2;

        if (isRoman) {
            num1 = romanToArabic(operand1);
            num2 = romanToArabic(operand2);
        } else {
            num1 = Integer.parseInt(operand1);
            num2 = Integer.parseInt(operand2);
        }

         if(!((num1>0 && num1<=10) && (num2>0 && num2<=10)) )
        { throw new IllegalArgumentException("числа должны быть в пределах от 1 до 10");}


        // выполняем арифметическое действие в зависимости от выбранного оператора
        char operator = input.charAt(parts[0].length());
        int result = 0;

        switch (operator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            case '/':
                if (num2 == 0) {
                    throw new ArithmeticException("деление на ноль запрещено.");
                }
                result = num1 / num2;
                break;
            default:
                throw new IllegalArgumentException("можно использовать только таки ариф операции +, -, *, /.");
        }


        //если цифры римские возвращаем результат метода  arabicToRoman т.е. римске цифры, в противном случае арабсике


        if (isRoman && result<1) {throw new IllegalArgumentException("римское число не может быть отрицательным");}

            if (isRoman){
            return arabicToRoman(result);
        } else {
            return String.valueOf(result);
        }


    }
    //Этот метод проверяет, является ли входная строка (input) римским числом.
    // Он делает это, сравнивая входную строку с шаблоном римских чисел c помощью метода matches.
     static boolean isRomanNumeral(String input) {  return input.matches("^[IVXLCDM]+$");
    }
    //Этот метод проверяет, является ли входная строка (input) арабским числом.
    // Он делает это, сравнивая входную строку с шаблоном арабсикх  чисел c помощью метода matches.

    static boolean isArabicNumeral(String input) {
        try {
            int number = Integer.parseInt(input);
            if (number >= 1 && number <= 10) {
                return true;
            } else {
                throw new IllegalArgumentException("Арабские числа должны быть в пределах от 1 до 10.");

            }
        } catch (NumberFormatException e) {
            return false;

        }
    }

    // метод для преобразования римского числа в арабское
     static int romanToArabic(String input) {
        int result = 0;
        int prevValue = 0;

//Мы начинаем цикл, который будет итерироваться по символам строки
// input справа налево, начиная с последнего символа "I".(пример VIII - 4 цикла)
        for (int i = input.length() - 1; i >= 0; i--) {
            char currentChar = input.charAt(i);
            int currentValue = getValueOfRomanCharacter(currentChar);

            if (currentValue < prevValue) {
                result -= currentValue;
            } else {
                result += currentValue;
            }
            prevValue = currentValue;
        }
        return result;
    }
    static int getValueOfRomanCharacter(char romanChar) {
        switch (romanChar) {
            case 'I': return 1;
            case 'V': return 5;
            case 'X': return 10;
            case 'L': return 50;
            case 'C': return 100;
            default: return 0; // неверное риское число
        }
    }


    // метод для преобразования арабского числа в римское
     static String arabicToRoman(int num) {
         if (num <= 0) {
             throw new IllegalArgumentException("Число должно быть положительным и больше нуля");
         }
        StringBuilder romanNumeral = new StringBuilder(); // создаем строку и помещаем ее в romanNumeral

        int[] arabicValues = { 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] romanSymbols = { "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

        for (int i = 0; i < arabicValues.length; i++) {
            while (num >= arabicValues[i]) {
                romanNumeral.append(romanSymbols[i]); // добавляем римскую цифру соответсвующую арабской цифре
                num -= arabicValues[i];    // на каждом круге цикла от заданного числа отнимаем  арабское число из массива
            }
        }

        return romanNumeral.toString();
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("введите арифметическое выражение (наприер, 3+2 или V*III): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                String result = Calculator.calc(input);
                System.out.println("Ответ: " + result);
            } catch (Exception e) {
                System.out.println("Ошибка:  " + e.getMessage());
                break;
            }
        }
        scanner.close();
    }
}
