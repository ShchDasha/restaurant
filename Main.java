import java.util.concurrent.*;

public class Main
{
    private static final int WAITERS_COUNT = 4;
    private static final int CHEFS_COUNT = 2;
    private static final int KITCHEN_QUEUE = 10;
    private static final int COMPLETED_QUEUE = 20;
    private static final int SIMULATION_SECONDS = 30;

    public static void main(String[] args)
    {
        System.out.println("'THE THREAD'");
        System.out.printf("Waiters: %d; chefs: %d%n", WAITERS_COUNT, CHEFS_COUNT);
        System.out.printf("Working time: %d seconds %n%n", SIMULATION_SECONDS);

        BlockingQueue<Order> kitchenQueue = new LinkedBlockingQueue<>(KITCHEN_QUEUE);
        BlockingQueue<CompletedOrder> completedOrders = new LinkedBlockingQueue<>(COMPLETED_QUEUE);

        ExecutorService waitersExecutor = Executors.newFixedThreadPool(WAITERS_COUNT);
        Waiter[] waiters = new Waiter[WAITERS_COUNT];

        for (int i=0; i < WAITERS_COUNT; ++i)
        {
            waiters[i] = new Waiter(i + 1, kitchenQueue, completedOrders);
            waitersExecutor.submit(waiters[i]);
        }

        ExecutorService chefsExecutor = Executors.newFixedThreadPool(CHEFS_COUNT);
        Chef[] chefs = new Chef[CHEFS_COUNT];

        for (int i=0; i < CHEFS_COUNT; ++i)
        {
            chefs[i] = new Chef(i + 1, kitchenQueue, completedOrders);
            chefsExecutor.submit(chefs[i]);
        }

        startQueueMonitor(kitchenQueue, completedOrders);

        try { TimeUnit.SECONDS.sleep(SIMULATION_SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        System.out.println("\nCLOSING OF THE RESTAURANT");
        for (Waiter waiter : waiters) waiter.stop();
        waitersExecutor.shutdownNow();
        for (Chef chef : chefs) chef.stop();
        chefsExecutor.shutdownNow();
        try
        {
            if (!waitersExecutor.awaitTermination(5, TimeUnit.SECONDS)) System.out.println("Warning: not all waiter flows ended correctly");
            if (!chefsExecutor.awaitTermination(5, TimeUnit.SECONDS)) System.out.println("Warning: not all chef flows ended correctly");
        }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        printStatistics(waiters, chefs, kitchenQueue, completedOrders);
        System.out.println("\nThe restaurant is closed. Come back again!");
    }

    private static void startQueueMonitor(BlockingQueue<Order> kitchenQueue, BlockingQueue<CompletedOrder> completedOrders)
    {
        Thread monitorThread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    TimeUnit.SECONDS.sleep(5);
                    System.out.printf("[MONITOR] Queue in the kitchen: %d/%d | " + "Ready orders: %d/%d%n", kitchenQueue.size(), KITCHEN_QUEUE, completedOrders.size(), COMPLETED_QUEUE);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private static void printStatistics(Waiter[] waiters, Chef[] chefs, BlockingQueue<Order> kitchenQueue, BlockingQueue<CompletedOrder> completedOrders)
    {
        System.out.println("\nJOB STATISTICS");
        int totalServed = 0;
        int totalCooked = 0;
        for (Waiter waiter : waiters)
        {
            System.out.printf("The number of orders served by the waiter %d: %d%n", waiter.getOrdersServed() > 0 ? 1 : 0, waiter.getOrdersServed());
            totalServed += waiter.getOrdersServed();
        }
        for (Chef chef : chefs)
        {
            System.out.printf("The number of orders that the cook has prepared %d: %d%n", chef.getOrdersCooked() > 0 ? 1 : 0, chef.getOrdersCooked());
            totalCooked += chef.getOrdersCooked();
        }
        System.out.printf("\nServed: %d; cooked: %d%n", totalServed, totalCooked);
        System.out.printf("Left in the kitchen queue: %d%n", kitchenQueue.size());
        System.out.printf("There are still outstanding completed orders: %d%n", completedOrders.size());
    }
}