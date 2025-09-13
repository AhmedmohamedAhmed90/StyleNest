using System.Text;
using System.Text.Json;
using CartService.Models;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

namespace CartService.Services;

public record RabbitOptions
{
    public string HostName { get; init; } = "localhost";
    public int Port { get; init; } = 5672;
    public string UserName { get; init; } = "guest";
    public string Password { get; init; } = "guest";
    public string Exchange { get; init; } = "order.exchange";
    public string CheckoutRoutingKey { get; init; } = "cart.order.placed";
    public string PaymentExchange { get; init; } = "payment.exchange";
    public string PaymentSuccessRoutingKey { get; init; } = "payment.success";
    public string PaymentSuccessQueue { get; init; } = "cart.payment.success.queue";
}

public interface ICheckoutPublisher
{
    Task PublishCheckoutAsync(Cart cart);
}

public class CheckoutPublisher : ICheckoutPublisher, IDisposable
{
    private readonly RabbitOptions _opt;
    private readonly IConnection? _conn;
    private readonly IModel? _ch;
    private readonly ILogger<CheckoutPublisher> _logger;

    public CheckoutPublisher(IOptions<RabbitOptions> options, ILogger<CheckoutPublisher> logger)
    {
        _opt = options.Value;
        _logger = logger;
        var cf = new ConnectionFactory
        {
            HostName = _opt.HostName,
            Port = _opt.Port,
            UserName = _opt.UserName,
            Password = _opt.Password,
            DispatchConsumersAsync = true
        };
        try
        {
            _conn = cf.CreateConnection("cart-publisher");
            _ch = _conn.CreateModel();
            _ch.ExchangeDeclare(_opt.Exchange, ExchangeType.Topic, durable: true, autoDelete: false);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "RabbitMQ not available for publishing. Events will be skipped.");
        }
    }

    public Task PublishCheckoutAsync(Cart cart)
    {
        try
        {
            if (_ch == null) return Task.CompletedTask;
            var payload = JsonSerializer.SerializeToUtf8Bytes(new
            {
                type = "CART_CHECKOUT",
                userId = cart.UserId,
                items = cart.Items
            });
            var props = _ch.CreateBasicProperties();
            props.ContentType = "application/json";
            props.DeliveryMode = 2;
            _ch.BasicPublish(_opt.Exchange, _opt.CheckoutRoutingKey, props, payload);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to publish checkout event");
        }
        return Task.CompletedTask;
    }

    public void Dispose()
    {
        try { _ch?.Close(); } catch { }
        try { _conn?.Close(); } catch { }
    }
}

public class PaymentSuccessListener : BackgroundService
{
    private readonly RabbitOptions _opt;
    private readonly ILogger<PaymentSuccessListener> _logger;
    private readonly ICartService _cartService;

    private IConnection? _conn;
    private IModel? _ch;

    public PaymentSuccessListener(IOptions<RabbitOptions> options, ILogger<PaymentSuccessListener> logger, ICartService cartService)
    {
        _opt = options.Value;
        _logger = logger;
        _cartService = cartService;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        try
        {
            var cf = new ConnectionFactory
            {
                HostName = _opt.HostName,
                Port = _opt.Port,
                UserName = _opt.UserName,
                Password = _opt.Password,
                DispatchConsumersAsync = true
            };

            _conn = cf.CreateConnection("cart-payment-listener");
            _ch = _conn.CreateModel();
            _ch.ExchangeDeclare(_opt.PaymentExchange, ExchangeType.Topic, durable: true, autoDelete: false);
            _ch.QueueDeclare(_opt.PaymentSuccessQueue, durable: true, exclusive: false, autoDelete: false);
            _ch.QueueBind(_opt.PaymentSuccessQueue, _opt.PaymentExchange, _opt.PaymentSuccessRoutingKey);

            var consumer = new AsyncEventingBasicConsumer(_ch);
            consumer.Received += async (_, ea) =>
            {
                try
                {
                    var json = Encoding.UTF8.GetString(ea.Body.Span);
                    // Expecting a payload with userId. Example: { "userId": "123", "orderId": "..." }
                    using var doc = JsonDocument.Parse(json);
                    if (doc.RootElement.TryGetProperty("userId", out var userIdElem))
                    {
                        var userId = userIdElem.GetString();
                        if (!string.IsNullOrWhiteSpace(userId))
                        {
                            await _cartService.SetPaymentConfirmedAsync(userId!);
                            _logger.LogInformation("Set cart TTL to indefinite for user {UserId} after payment success", userId);
                        }
                    }
                    _ch?.BasicAck(ea.DeliveryTag, multiple: false);
                }
                catch (Exception ex)
                {
                    _logger.LogWarning(ex, "Error handling payment success message");
                    _ch?.BasicNack(ea.DeliveryTag, multiple: false, requeue: true);
                }
            };

            _ch.BasicConsume(_opt.PaymentSuccessQueue, autoAck: false, consumer);

            while (!stoppingToken.IsCancellationRequested)
            {
                await Task.Delay(1000, stoppingToken);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "RabbitMQ not available for payment listener. Will not subscribe.");
        }
    }
}
