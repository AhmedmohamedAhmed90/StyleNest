using System;
using System.Collections.Generic;

namespace CartService.Models
{
    public class Cart
    {
        public required string UserId { get; set; }
        public List<CartItem> Items { get; set; } = new List<CartItem>();
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
       
    }
}
