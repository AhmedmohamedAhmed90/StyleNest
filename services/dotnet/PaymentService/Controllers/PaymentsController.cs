using System.ComponentModel.DataAnnotations;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PaymentService.Rabbit;
using Microsoft.Extensions.Logging;

namespace PaymentService.Controllers;

[ApiController]
[Route("api/payments")] 
[Authorize]
public class PaymentsController : ControllerBase
{
    private readonly PaymentEventPublisher _publisher;
    private readonly ILogger<PaymentsController> _logger;

    public PaymentsController(PaymentEventPublisher publisher, ILogger<PaymentsController> logger)
    {
        _publisher = publisher;
        _logger = logger;
    }

    public record PaymentRequest(
        [Required] string OrderId,
        [Required, Range(0.01, double.MaxValue)] decimal Amount,
        [Required, StringLength(19, MinimumLength = 12)] string CardNumber,
        [Required, StringLength(4, MinimumLength = 3)] string Cvv,
        [Required] string CardHolderName
    );

    [HttpPost]
    public async Task<ActionResult> Process([FromBody] PaymentRequest req)
    {
        var userId = Request.Headers["X-USER-ID"].FirstOrDefault() ?? User.FindFirstValue("userId") ?? string.Empty;
        if (string.IsNullOrWhiteSpace(userId)) return Unauthorized();

        // Simulate payment processing (Luhn check + simple CVV length)
        bool ok = IsValidCard(req.CardNumber) && (req.Cvv.Length is 3 or 4);
        var paymentId = Guid.NewGuid().ToString("N");

        var evt = new
        {
            orderId = req.OrderId,
            userId,
            amount = req.Amount,
            status = ok ? "SUCCESS" : "FAILURE",
            timestamp = DateTime.UtcNow
        };
        _logger.LogInformation("Processing payment for order {OrderId} by user {UserId}: status={Status}", req.OrderId, userId, ok ? "SUCCESS" : "FAILURE");
        if (ok) _publisher.PublishSuccess(evt); else _publisher.PublishFailure(evt);

        // Note: OrderService can subscribe to payment.exchange to set status PAID.
        // Alternatively, expose a secure endpoint in OrderService to update status and call it here.

        return Ok(new { paymentId, status = ok ? "Succeeded" : "Failed" });
    }

    private static bool IsValidCard(string card)
    {
        if (string.IsNullOrWhiteSpace(card)) return false;
        var digits = new string(card.Where(char.IsDigit).ToArray());
        if (digits.Length < 12 || digits.Length > 19) return false;

        // Luhn algorithm
        int sum = 0; bool alt = false;
        for (int i = digits.Length - 1; i >= 0; i--)
        {
            int n = digits[i] - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            sum += n; alt = !alt;
        }
        return sum % 10 == 0;
    }
}
