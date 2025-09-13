using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.Extensions.Hosting;

namespace CartService.Services;

public class EurekaOptions
{
    public string DefaultZone { get; set; } = "http://localhost:8761/eureka/";
    public string AppName { get; set; } = "CartService";
    public int Port { get; set; } = 5025;
}

public class EurekaRegistration : IHostedService, IDisposable
{
    private readonly IHttpClientFactory _httpFactory;
    private readonly ILogger<EurekaRegistration> _logger;
    private readonly EurekaOptions _opt;
    private Timer? _timer;
    private string _instanceId = $"cartservice-{Guid.NewGuid():N}";

    public EurekaRegistration(IHttpClientFactory httpFactory, ILogger<EurekaRegistration> logger, IConfiguration cfg)
    {
        _httpFactory = httpFactory;
        _logger = logger;
        var zone = cfg["eureka:client:serviceUrl:defaultZone"] ?? cfg["Eureka:Client:ServiceUrl:DefaultZone"] ?? "http://localhost:8761/eureka/";
        var app = cfg["spring:application:name"] ?? cfg["Spring:Application:Name"] ?? "CartService";
        var port = cfg.GetValue<int?>("eureka:instance:nonSecurePort") ?? 5025;
        _opt = new EurekaOptions { DefaultZone = zone, AppName = app, Port = port };
    }

    public Task StartAsync(CancellationToken cancellationToken)
    {
        _ = RegisterAsync(cancellationToken);
        // send heartbeat every 30s
        _timer = new Timer(async _ => await HeartbeatAsync(), null, TimeSpan.FromSeconds(30), TimeSpan.FromSeconds(30));
        return Task.CompletedTask;
    }

    private async Task RegisterAsync(CancellationToken ct)
    {
        try
        {
            var client = _httpFactory.CreateClient("eureka");
            client.BaseAddress = new Uri(_opt.DefaultZone.TrimEnd('/') + "/");
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

            var host = Environment.MachineName;
            var ip = "127.0.0.1";

            var payload = new
            {
                instance = new Dictionary<string, object>
                {
                    ["instanceId"] = _instanceId,
                    ["hostName"] = host,
                    ["app"] = _opt.AppName.ToUpperInvariant(),
                    ["vipAddress"] = _opt.AppName.ToUpperInvariant(),
                    ["ipAddr"] = ip,
                    ["status"] = "UP",
                    ["port"] = new Dictionary<string, object>{{"$", _opt.Port}, {"@enabled", true}},
                    ["dataCenterInfo"] = new Dictionary<string, object>{{"@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo"}, {"name", "MyOwn"}},
                    ["healthCheckUrl"] = $"http://localhost:{_opt.Port}/health",
                    ["statusPageUrl"] = $"http://localhost:{_opt.Port}/swagger/index.html"
                }
            };

            var resp = await client.PostAsJsonAsync($"apps/{_opt.AppName}", payload, new JsonSerializerOptions(JsonSerializerDefaults.Web), ct);
            if (!resp.IsSuccessStatusCode && resp.StatusCode != System.Net.HttpStatusCode.NoContent)
            {
                _logger.LogWarning("Eureka register returned {Status}", resp.StatusCode);
            }
            else
            {
                _logger.LogInformation("Registered {App} with Eureka as {InstanceId}", _opt.AppName, _instanceId);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Eureka registration failed");
        }
    }

    private async Task HeartbeatAsync()
    {
        try
        {
            var client = _httpFactory.CreateClient("eureka");
            client.BaseAddress = new Uri(_opt.DefaultZone.TrimEnd('/') + "/");
            var uri = $"apps/{_opt.AppName}/{_instanceId}";
            var req = new HttpRequestMessage(HttpMethod.Put, uri);
            var resp = await client.SendAsync(req);
            if (!resp.IsSuccessStatusCode && resp.StatusCode != System.Net.HttpStatusCode.NoContent)
            {
                _logger.LogWarning("Eureka heartbeat returned {Status}", resp.StatusCode);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Eureka heartbeat failed");
        }
    }

    public Task StopAsync(CancellationToken cancellationToken)
    {
        _timer?.Change(Timeout.Infinite, 0);
        _ = DeregisterAsync();
        return Task.CompletedTask;
    }

    private async Task DeregisterAsync()
    {
        try
        {
            var client = _httpFactory.CreateClient("eureka");
            client.BaseAddress = new Uri(_opt.DefaultZone.TrimEnd('/') + "/");
            var uri = $"apps/{_opt.AppName}/{_instanceId}";
            await client.DeleteAsync(uri);
        }
        catch { }
    }

    public void Dispose()
    {
        _timer?.Dispose();
    }
}

