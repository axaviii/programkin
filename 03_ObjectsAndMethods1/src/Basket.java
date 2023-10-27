public class Basket {

    private static int count = 0;
    private String items = "";
    private int totalPrice = 0;
    private double totalWeight = 0; // Добавляем переменную для общей массы
    private int limit;

    public Basket() {
        increaseCount(1);
        items = "Список товаров:";
        this.limit = 1000000;
    }

    public Basket(int limit) {
        this();
        this.limit = limit;
    }

    public Basket(String items, int totalPrice) {
        this();
        this.items = this.items + items;
        this.totalPrice = totalPrice;
    }

    public static int getCount() {
        return count;
    }

    public static void increaseCount(int count) {
        Basket.count = Basket.count + count;
    }

    public void add(String name, int price) {
        add(name, price, 1);
    }

    public void add(String name, int price, int count) {
        add(name, price, count, 0); // Вызываем метод с весом по умолчанию (0)
    }

    public void add(String name, int price, int count, double weight) {
        boolean error = false;
        if (contains(name)) {
            error = true;
        }

        if (totalPrice + count * price >= limit) {
            error = true;
        }

        if (error) {
            System.out.println("Error occured :(");
            return;
        }

        items = items + "\n" + name + " - " +
                count + " шт. - " + price + " руб.";

        totalPrice = totalPrice + count * price;
        totalWeight = totalWeight + weight; // Добавляем вес к общей массе
    }

    public void clear() {
        items = "";
        totalPrice = 0;
        totalWeight = 0; // Сбрасываем общий вес при очистке корзины
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public double getTotalWeight() {

        return totalWeight; // Возвращаем общую массу
    }

    public boolean contains(String name) {
        return items.contains(name);
    }

    public void print(String title) {
        System.out.println(title);
        if (items.isEmpty()) {
            System.out.println("Корзина пуста");
        } else {
            System.out.println(items);
            System.out.println("Общая масса в корзине: " + getTotalWeight() + " кг");
            System.out.println("Общая стоимость товаров: " + getTotalPrice() + " руб.");
        }
    }
}
