using System.Security.Claims;
using CartService.Models;
using CartService.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace CartService.Controllers;

[ApiController]
[Route("api/cart")]
[Authorize]
public class CartController : ControllerBase
{
    private readonly ICartService _svc;

    public CartController(ICartService svc)
    {
        _svc = svc;
    }

    private string? GetUserId()
    {
        // Prefer X-USER-ID (from gateway), else JWT claim "userId"
        if (Request.Headers.TryGetValue("X-USER-ID", out var headerUser))
            return headerUser.ToString();
        return User.FindFirstValue("userId") ?? User.FindFirstValue(ClaimTypes.NameIdentifier);
    }

    [HttpGet]
    public async Task<ActionResult<Cart>> Get()
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        var cart = await _svc.GetOrCreateAsync(userId);
        return Ok(cart);
    }

    public record UpsertItemDto(string ProductId, int Quantity, decimal Price);

    [HttpPost("items")]
    public async Task<ActionResult<Cart>> UpsertItem([FromBody] UpsertItemDto dto)
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        if (dto == null || string.IsNullOrWhiteSpace(dto.ProductId) || dto.Quantity <= 0)
            return BadRequest("Invalid item");
        var cart = await _svc.AddOrUpdateItemAsync(userId, dto.ProductId, dto.Quantity, dto.Price);
        return Ok(cart);
    }

    [HttpPost("items/bulk")]
    public async Task<ActionResult<Cart>> UpsertItemsBulk([FromBody] List<UpsertItemDto> items)
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        if (items == null || items.Count == 0) return BadRequest("No items provided");

        var mapped = items
            .Where(i => i != null && !string.IsNullOrWhiteSpace(i.ProductId) && i.Quantity > 0)
            .Select(i => new CartItem { ProductId = i.ProductId, Quantity = i.Quantity, Price = i.Price })
            .ToList();

        if (mapped.Count == 0) return BadRequest("All items invalid");

        var cart = await _svc.AddOrUpdateItemsAsync(userId, mapped);
        return Ok(cart);
    }

    [HttpPut("items/{productId}")]
    public async Task<ActionResult<Cart>> UpdateQuantity(string productId, [FromBody] int quantity)
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        if (quantity <= 0) return BadRequest("Quantity must be > 0");
        var cart = await _svc.AddOrUpdateItemAsync(userId, productId, quantity, 0);
        return Ok(cart);
    }

    [HttpDelete("items/{productId}")]
    public async Task<ActionResult<Cart>> Remove(string productId)
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        var cart = await _svc.RemoveItemAsync(userId, productId);
        return Ok(cart);
    }

    [HttpPost("clear")]
    public async Task<IActionResult> Clear()
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        await _svc.ClearAsync(userId);
        return NoContent();
    }

    [HttpPost("checkout")]
    public async Task<ActionResult<Cart>> Checkout()
    {
        var userId = GetUserId();
        if (string.IsNullOrEmpty(userId)) return Unauthorized();
        var cart = await _svc.CheckoutAsync(userId);
        return Ok(cart);
    }
}
