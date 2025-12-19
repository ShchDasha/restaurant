import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Chef implements Runnable
{
    private final int id;
    private final BlockingQueue<Order> kitchenQueue;
    private final BlockingQueue<CompletedOrder> completedOrders;
    private volatile boolean isRunning = true;
    private int ordersCooked = 0;

    public Chef(int id, BlockingQueue<Order> kitchenQueue, BlockingQueue<CompletedOrder> completedOrders)
    {
        this.id = id;
        this.kitchenQueue = kitchenQueue;
        this.completedOrders = completedOrders;
    }

    @Override
    public void run()
    {
        System.out.printf("The chef %d has started work %n", id);
        while (isRunning)
        {
            try
            {
                Order order = kitchenQueue.take(); //Блокирующее ожидание заказа из очереди
                if (order.getDishName().equals("STOP")) break; //Проверка сигнального заказа для остановки
                System.out.printf("The chef %d started cooking: %s%n", id, order);
                cookOrder(order); //Приготовление заказа (имитация времени приготовления)
                CompletedOrder completedOrder = new CompletedOrder(order); //Создание готового заказа
                completedOrders.put(completedOrder); //Помещение в очередь готовых заказов (блокируется если очередь полна)
                ++ordersCooked;
                System.out.printf("The chef %d cooked: %s%n", id, completedOrder);
            }
            catch (InterruptedException e)
            {
                System.out.printf("The chef %d was interrupted %n", id);
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.printf("The chef %d has finished his work %n", id);
    }

    //Время приготовления зависит от типа блюда
    private void cookOrder(Order order) throws InterruptedException
    {           
        int cookingTime;
        switch (order.getDishName())
        {
            case "Steak":
                cookingTime = 2000; //2 секунды
                break;
            case "Pizza":
                cookingTime = 1500; //1.5 секунды
                break;
            case "Pasta":
                cookingTime = 1100; //1.1 секунды
                break;
            case "Soup":
                cookingTime = 800; //0.8 секунды
                break;
            case "Salad":
                cookingTime = 500; //0.5 секунды
                break;
            case "Dessert":
                cookingTime = 300; // 0.3 секунды
                break;
            default: cookingTime = 1000; //1 секунда по умолчанию
        }
        TimeUnit.MILLISECONDS.sleep(cookingTime);
    }

    public void stop()
    {
        isRunning = false;
        try { kitchenQueue.put(new Order("STOP", -1)); } //Отправка сигнального заказа для выхода из блокирующего queue.take()
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public int getOrdersCooked() { return ordersCooked; }

}
