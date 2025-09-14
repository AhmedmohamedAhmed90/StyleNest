using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Logging;
using RabbitMQ.Client;

namespace PaymentService.Rabbit;

public class RabbitOptions
{
    public string HostName { get; set; } = "localhost";
    public int Port { get; set; } = 5672;
    public string UserName { get; set; } = "guest";
    public string Password { get; set; } = "guest";
    public string Exchange { get; set; } = "payment.exchange";
    public string SuccessRoutingKey { get; set; } = "payment.success";
    public string FailureRoutingKey { get; set; } = "payment.failure";
}

public class PaymentEventPublisher : IDisposable
{
    private readonly RabbitOptions _opt;
    private IConnection? _conn;
    private IModel? _ch;
    private readonly ILogger<PaymentEventPublisher> _logger;
    private readonly object _sync = new();

    public PaymentEventPublisher(IConfiguration cfg, ILogger<PaymentEventPublisher> logger)
    {
        _logger = logger;
        _opt = new RabbitOptions
        {
            HostName = cfg["RabbitMQ:HostName"] ?? "localhost",
            Port = int.TryParse(cfg["RabbitMQ:Port"], out var p) ? p : 5672,
            UserName = cfg["RabbitMQ:UserName"] ?? "guest",
            Password = cfg["RabbitMQ:Password"] ?? "guest",
            Exchange = cfg["RabbitMQ:Exchange"] ?? "payment.exchange",
            SuccessRoutingKey = cfg["RabbitMQ:SuccessRoutingKey"] ?? "payment.success",
            FailureRoutingKey = cfg["RabbitMQ:FailureRoutingKey"] ?? "payment.failure",
        };

        // Try initial connect (non-fatal). We will also retry lazily on publish.
        TryEnsureChannel();
    }

    private void TryEnsureChannel()
    {
        if (_ch != null && _ch.IsOpen && _conn != null && _conn.IsOpen) return;
        lock (_sync)
        {
            if (_ch != null && _ch.IsOpen && _conn != null && _conn.IsOpen) return;
            try
            {
                _conn?.Dispose();
                _ch?.Dispose();
                var cf = new ConnectionFactory
                {
                    HostName = _opt.HostName,
                    Port = _opt.Port,
                    UserName = _opt.UserName,
                    Password = _opt.Password,
                    AutomaticRecoveryEnabled = true,
                    TopologyRecoveryEnabled = true,
                    NetworkRecoveryInterval = TimeSpan.FromSeconds(5)
                };
                _conn = cf.CreateConnection("payment-publisher");
                _ch = _conn.CreateModel();
                _ch.ExchangeDeclare(_opt.Exchange, ExchangeType.Topic, durable: true, autoDelete: false);
                _logger.LogInformation("Connected to RabbitMQ at {Host}:{Port} and declared exchange {Exchange}", _opt.HostName, _opt.Port, _opt.Exchange);
            }
            catch (Exception ex)
            {
                _conn = null;
                _ch = null;
                _logger.LogWarning(ex, "RabbitMQ connection unavailable for PaymentEventPublisher; events will be skipped.");
            }
        }
    }

    public void PublishSuccess(object payload)
        => Publish(_opt.SuccessRoutingKey, payload);

    public void PublishFailure(object payload)
        => Publish(_opt.FailureRoutingKey, payload);

    private void Publish(string routingKey, object payload)
    {
        // Lazy connect/reconnect on demand
        TryEnsureChannel();
        if (_ch == null)
        {
            _logger.LogWarning("Skipping publish to {RoutingKey} because RabbitMQ channel is not available.", routingKey);
            return;
        }
        var bytes = JsonSerializer.SerializeToUtf8Bytes(payload, new JsonSerializerOptions(JsonSerializerDefaults.Web));
        var props = _ch.CreateBasicProperties();
        props.ContentType = "application/json";
        props.DeliveryMode = 2;
        _ch.BasicPublish(_opt.Exchange, routingKey, props, bytes);
        try
        {
            // Log minimal details for traceability
            _logger.LogInformation("Published payment event to {Exchange} with {RoutingKey}", _opt.Exchange, routingKey);
        }
        catch { }
    }

    public void Dispose()
    {
        try { _ch?.Close(); } catch { }
        try { _conn?.Close(); } catch { }
    }
}
