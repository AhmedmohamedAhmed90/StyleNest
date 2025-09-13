using System.Text.Json;
using CartService.Models;
using StackExchange.Redis;

namespace CartService.Services;

public class RedisCartRepository : ICartRepository
{
    private readonly IDatabase _db;
    private readonly string _keyPrefix = "cart:";
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    public RedisCartRepository(IConnectionMultiplexer mux, int database)
    {
        _db = mux.GetDatabase(database);
    }

    private string Key(string userId) => _keyPrefix + userId;

    public async Task<Cart?> GetAsync(string userId)
    {
        var val = await _db.StringGetAsync(Key(userId));
        if (val.IsNullOrEmpty) return null;
        return JsonSerializer.Deserialize<Cart>(val!, JsonOptions);
    }

    public async Task SetAsync(Cart cart, TimeSpan? ttl)
    {
        var json = JsonSerializer.Serialize(cart, JsonOptions);
        await _db.StringSetAsync(Key(cart.UserId), json, ttl);
    }

    public async Task<bool> DeleteAsync(string userId)
    {
        return await _db.KeyDeleteAsync(Key(userId));
    }

    public async Task<bool> UpdateTtlAsync(string userId, TimeSpan? ttl)
    {
        if (ttl == null)
        {
            // persist (remove expiration)
            return await _db.KeyPersistAsync(Key(userId));
        }
        return await _db.KeyExpireAsync(Key(userId), ttl);
    }
}

