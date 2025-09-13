using System.Threading.Tasks;
using CartService.Models;

namespace CartService.Services;

public interface ICartRepository
{
    Task<Cart?> GetAsync(string userId);
    Task SetAsync(Cart cart, TimeSpan? ttl);
    Task<bool> DeleteAsync(string userId);
    Task<bool> UpdateTtlAsync(string userId, TimeSpan? ttl);
}

