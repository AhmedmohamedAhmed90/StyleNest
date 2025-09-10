namespace CartService.Models
{
    public class CartItem
    {
        public required string ProductId { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }
}
