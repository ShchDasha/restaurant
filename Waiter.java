import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Waiter implements Runnable
{
    private final int id;
    private final BlockingQueue<Order> kitchenQueue; //Общая очередь на кухню
    private final BlockingQueue<CompletedOrder> completedOrders; //Общая очередь готовых заказов
    private volatile boolean isRunning = true; //volatile для гарантии видимости между потоками
    private int ordersServed = 0;

    private final BlockingQueue<CompletedOrder> personalCompletedQueue = new LinkedBlockingQueue<>(); //Личная очередь готовых заказов для этого официанта

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
                //Генерация нового заказа
                Order newOrder = generateOrder();
                System.out.printf("The waiter %d accepted %s%n", id, newOrder);

                //Отправка заказа на кухню (блокируется если очередь полна)
                kitchenQueue.put(newOrder);
                System.out.printf("The waiter %d handed %s to the kitchen %n", id, newOrder);
                
                waitForOrderCompletion(); //Ожидание готовности заказа
                
                deliverOrder(); //Доставка заказа
                
                ++ordersServed;
                TimeUnit.MILLISECONDS.sleep(500 + (int)(Math.random() * 1000)); //Имитация времени между заказами
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
        return new Order(randomDish, id); //id официанта сохраняется в заказе
    }

    //Алгоритм поиска своего заказа в общей очереди
    private void waitForOrderCompletion() throws InterruptedException
    {
        while (true)
        {
            CompletedOrder completed = completedOrders.take(); //Блокирующее извлечение из общей очереди готовых заказов

            //Принадлежит ли заказ этому официанту?
            if (completed.getOrder().getWaiterId() == id)
            {
                personalCompletedQueue.put(completed); //Забираем свой заказ в личную очередь
                System.out.printf("The waiter %d received %s%n", id, completed);
                break;
            }
            else
            {
                completedOrders.put(completed); //Чужой заказ возвращаем обратно в очередь
                TimeUnit.MILLISECONDS.sleep(10); //Короткая пауза для предотвращения активного ожидания
            }
        }
    }

    private void deliverOrder() throws InterruptedException
    {
        //Извлечение заказа из личной очереди (обычно сразу доступен)
        CompletedOrder completed = personalCompletedQueue.take();
        System.out.printf("The waiter %d delivered to the customer: %s%n", id, completed);
    }

    public void stop() { isRunning = false; }

    public int getOrdersServed() { return ordersServed; }

}
