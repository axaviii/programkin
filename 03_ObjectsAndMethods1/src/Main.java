public class Main {

    public static void main(String[] args) {
        Basket vasybasket = new Basket();
        vasybasket.add("Milk", 80);
        vasybasket.add("Хлеб",40,3,1.6);
        vasybasket.add("Коньяк",500,3,3);
        vasybasket.getTotalWeight();
        vasybasket.getTotalPrice();
        vasybasket.print("Корзина васи");


    }
}
