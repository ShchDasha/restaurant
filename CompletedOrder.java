public class CompletedOrder
{
    private final Order order;
    private final long completionTime;

    public CompletedOrder(Order order)
    {
        this.order = order;
        this.completionTime = System.currentTimeMillis();
    }

    public Order getOrder() { return order; }
    public long getCompletionTime() { return completionTime; }
    public long getCookingTime() { return completionTime - order.getCreationTime(); }

    @Override
    public String toString() { return String.format("ready %s (cooking time: %d ms)", order, getCookingTime()); }
}