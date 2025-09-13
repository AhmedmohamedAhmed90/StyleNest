using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Options;
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
    private readonly IConnection? _conn;
    private readonly IModel? _ch;

    public PaymentEventPublisher(IConfiguration cfg)
    {
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

        try
        {
            var cf = new ConnectionFactory
            {
                HostName = _opt.HostName,
                Port = _opt.Port,
                UserName = _opt.UserName,
                Password = _opt.Password
            };
            _conn = cf.CreateConnection("payment-publisher");
            _ch = _conn.CreateModel();
            _ch.ExchangeDeclare(_opt.Exchange, ExchangeType.Topic, durable: true, autoDelete: false);
        }
        catch
        {
        }
    }

    public void PublishSuccess(object payload)
        => Publish(_opt.SuccessRoutingKey, payload);

    public void PublishFailure(object payload)
        => Publish(_opt.FailureRoutingKey, payload);

    private void Publish(string routingKey, object payload)
    {
        if (_ch == null) return;
        var bytes = JsonSerializer.SerializeToUtf8Bytes(payload, new JsonSerializerOptions(JsonSerializerDefaults.Web));
        var props = _ch.CreateBasicProperties();
        props.ContentType = "application/json";
        props.DeliveryMode = 2;
        _ch.BasicPublish(_opt.Exchange, routingKey, props, bytes);
    }

    public void Dispose()
    {
        try { _ch?.Close(); } catch { }
        try { _conn?.Close(); } catch { }
    }
}

