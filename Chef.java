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
                Order order = kitchenQueue.take();
                if (order.getDishName().equals("STOP")) break;
                System.out.printf("The chef %d started cooking: %s%n", id, order);
                cookOrder(order);
                CompletedOrder completedOrder = new CompletedOrder(order);
                completedOrders.put(completedOrder);
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

    private void cookOrder(Order order) throws InterruptedException
    {
        int cookingTime;
        switch (order.getDishName())
        {
            case "Steak":
                cookingTime = 2000;
                break;
            case "Pizza":
                cookingTime = 1500;
                break;
            case "Pasta":
                cookingTime = 1100;
                break;
            case "Soup":
                cookingTime = 800;
                break;
            case "Salad":
                cookingTime = 500;
                break;
            case "Dessert":
                cookingTime = 300;
                break;
            default: cookingTime = 1000;
        }
        TimeUnit.MILLISECONDS.sleep(cookingTime);
    }

    public void stop()
    {
        isRunning = false;
        try { kitchenQueue.put(new Order("STOP", -1)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public int getOrdersCooked() { return ordersCooked; }
}