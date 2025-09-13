using CartService.Models;

namespace CartService.Services;

public interface ICartService
{
    Task<Cart> GetOrCreateAsync(string userId);
    Task<Cart> AddOrUpdateItemAsync(string userId, string productId, int quantity, decimal price);
    Task<Cart> AddOrUpdateItemsAsync(string userId, IEnumerable<CartItem> items);
    Task<Cart> RemoveItemAsync(string userId, string productId);
    Task ClearAsync(string userId);
    Task<Cart> CheckoutAsync(string userId);
    Task SetPaymentConfirmedAsync(string userId);
}

public class CartServiceImpl : ICartService
{
    private readonly ICartRepository _repo;
    private readonly TimeSpan? _ttlDefault;
    private readonly TimeSpan? _ttlOrderPlaced;
    private readonly TimeSpan? _ttlPaymentConfirmed;
    private readonly ICheckoutPublisher _publisher;

    public CartServiceImpl(
        ICartRepository repo,
        IConfiguration cfg,
        ICheckoutPublisher publisher)
    {
        _repo = repo;
        _publisher = publisher;
        var ttlSec = cfg.GetSection("CartTtlSeconds");
        _ttlDefault = SecondsToTtl(ttlSec.GetValue<int>("Default"));
        _ttlOrderPlaced = SecondsToTtl(ttlSec.GetValue<int>("OrderPlaced"));
        _ttlPaymentConfirmed = SecondsToTtl(ttlSec.GetValue<int>("PaymentConfirmed"));
    }

    private static TimeSpan? SecondsToTtl(int seconds)
    {
        if (seconds < 0) return null; // indefinite
        return TimeSpan.FromSeconds(seconds);
    }

    public async Task<Cart> GetOrCreateAsync(string userId)
    {
        var cart = await _repo.GetAsync(userId) ?? new Cart { UserId = userId };
        if (cart.Items == null) cart.Items = new List<CartItem>();
        // Refresh default TTL on read (optional), comment if undesired
        await _repo.UpdateTtlAsync(userId, _ttlDefault);
        return cart;
    }

    public async Task<Cart> AddOrUpdateItemAsync(string userId, string productId, int quantity, decimal price)
    {
        var cart = await _repo.GetAsync(userId) ?? new Cart { UserId = userId };
        var existing = cart.Items.FirstOrDefault(i => i.ProductId == productId);
        if (existing == null)
        {
            cart.Items.Add(new CartItem { ProductId = productId, Quantity = quantity, Price = price });
        }
        else
        {
            existing.Quantity = quantity;
            existing.Price = price;
        }
        await _repo.SetAsync(cart, _ttlDefault);
        return cart;
    }

    public async Task<Cart> RemoveItemAsync(string userId, string productId)
    {
        var cart = await _repo.GetAsync(userId) ?? new Cart { UserId = userId };
        cart.Items.RemoveAll(i => i.ProductId == productId);
        await _repo.SetAsync(cart, _ttlDefault);
        return cart;
    }

    public async Task<Cart> AddOrUpdateItemsAsync(string userId, IEnumerable<CartItem> items)
    {
        var cart = await _repo.GetAsync(userId) ?? new Cart { UserId = userId };
        foreach (var incoming in items)
        {
            if (incoming == null || string.IsNullOrWhiteSpace(incoming.ProductId) || incoming.Quantity <= 0)
                continue;
            var existing = cart.Items.FirstOrDefault(i => i.ProductId == incoming.ProductId);
            if (existing == null)
            {
                cart.Items.Add(new CartItem
                {
                    ProductId = incoming.ProductId,
                    Quantity = incoming.Quantity,
                    Price = incoming.Price
                });
            }
            else
            {
                existing.Quantity = incoming.Quantity;
                existing.Price = incoming.Price;
            }
        }
        await _repo.SetAsync(cart, _ttlDefault);
        return cart;
    }

    public async Task ClearAsync(string userId)
    {
        await _repo.DeleteAsync(userId);
    }

    public async Task<Cart> CheckoutAsync(string userId)
    {
        var cart = await _repo.GetAsync(userId) ?? new Cart { UserId = userId };
        // publish checkout event
        await _publisher.PublishCheckoutAsync(cart);
        // extend TTL to orderPlaced
        await _repo.UpdateTtlAsync(userId, _ttlOrderPlaced);
        return cart;
    }

    public async Task SetPaymentConfirmedAsync(string userId)
    {
        await _repo.UpdateTtlAsync(userId, _ttlPaymentConfirmed); // null -> persist (no expiry)
    }
}
