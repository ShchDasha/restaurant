public class Order
{
    private static int idCounter = 1; //Статический счётчик для генерации уникальных ID заказов
    private final int id;
    private final String dishName;
    private final int waiterId;
    private final long creationTime; //Время создания заказа

    public Order(String dishName, int waiterId)
    {
        this.id = idCounter++;
        this.dishName = dishName;
        this.waiterId = waiterId;
        this.creationTime = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public String getDishName() { return dishName; }
    public int getWaiterId() { return waiterId; }
    public long getCreationTime() { return creationTime; }

    @Override
    public String toString() { return String.format("order №%d ('%s') from the waiter %d", id, dishName, waiterId); }

}
