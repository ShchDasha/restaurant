import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Waiter implements Runnable
{
    private final int id;
    private final BlockingQueue<Order> kitchenQueue;
    private final BlockingQueue<CompletedOrder> completedOrders;
    private volatile boolean isRunning = true;
    private int ordersServed = 0;

    private final BlockingQueue<CompletedOrder> personalCompletedQueue = new LinkedBlockingQueue<>();

    public Waiter(int id, BlockingQueue<Order> kitchenQueue, BlockingQueue<CompletedOrder> completedOrders)
    {
        this.id = id;
        this.kitchenQueue = kitchenQueue;
        this.completedOrders = completedOrders;
    }

    @Override
    public void run()
    {
        System.out.printf("The waiter %d has started work %n", id);

        while (isRunning)
        {
            try
            {
                Order newOrder = generateOrder();
                System.out.printf("The waiter %d accepted %s%n", id, newOrder);
                kitchenQueue.put(newOrder);
                System.out.printf("The waiter %d handed %s to the kitchen %n", id, newOrder);
                waitForOrderCompletion();
                deliverOrder();
                ++ordersServed;
                TimeUnit.MILLISECONDS.sleep(500 + (int)(Math.random() * 1000));
            }
            catch (InterruptedException e)
            {
                System.out.printf("The waiter %d was interrupted %n", id);
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.printf("The waiter %d has finished his work %n", id);
    }

    private Order generateOrder()
    {
        String[] dishes = {"Pizza", "Pasta", "Steak", "Salad", "Soup", "Dessert"};
        String randomDish = dishes[(int)(Math.random() * dishes.length)];
        return new Order(randomDish, id);
    }

    private void waitForOrderCompletion() throws InterruptedException
    {
        while (true)
        {
            CompletedOrder completed = completedOrders.take();
            if (completed.getOrder().getWaiterId() == id)
            {
                personalCompletedQueue.put(completed);
                System.out.printf("The waiter %d received %s%n", id, completed);
                break;
            }
            else
            {
                completedOrders.put(completed);
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }
    }

    private void deliverOrder() throws InterruptedException
    {
        CompletedOrder completed = personalCompletedQueue.take();
        System.out.printf("The waiter %d delivered to the customer: %s%n", id, completed);
    }

    public void stop() { isRunning = false; }

    public int getOrdersServed() { return ordersServed; }
}