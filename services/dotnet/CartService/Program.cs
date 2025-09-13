using System.Text;
using CartService.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

// Options binding
builder.Services.Configure<RabbitOptions>(builder.Configuration.GetSection("RabbitMQ"));

// Redis connection
var redisCfg = builder.Configuration.GetValue<string>("Redis:Configuration") ?? "localhost:6379";
var redisDb = builder.Configuration.GetValue<int>("Redis:Database");
builder.Services.AddSingleton<IConnectionMultiplexer>(_ => ConnectionMultiplexer.Connect(redisCfg));
builder.Services.AddSingleton<ICartRepository>(sp => new RedisCartRepository(sp.GetRequiredService<IConnectionMultiplexer>(), redisDb));

// Cart services and RabbitMQ
builder.Services.AddSingleton<ICheckoutPublisher, CheckoutPublisher>();
builder.Services.AddSingleton<ICartService, CartServiceImpl>();
builder.Services.AddHostedService<PaymentSuccessListener>();

// Controllers + Swagger
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// JWT Authentication
var jwtSecretValue = builder.Configuration.GetValue<string>("Jwt:Secret");
if (string.IsNullOrWhiteSpace(jwtSecretValue))
{
    throw new InvalidOperationException("Jwt:Secret is required");
}
// Important: AuthService signs using the literal bytes of the configured string
// (not Base64-decoded). Use the same here to validate tokens consistently.
byte[] secretBytes = Encoding.UTF8.GetBytes(jwtSecretValue);

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(secretBytes),
            NameClaimType = "email",
        };
    });

// Custom Eureka registration (HostedService) configured below
builder.Services.AddHttpClient("eureka");
builder.Services.AddHostedService<CartService.Services.EurekaRegistration>();

var app = builder.Build();

// Swagger
app.UseSwagger();
app.UseSwaggerUI();

// Disable HTTPS redirection for local dev behind the gateway

app.UseAuthentication();
app.UseAuthorization();

// No additional middleware required for registration

app.MapControllers();

// Health endpoint for Eureka
app.MapGet("/health", () => Results.Ok(new { status = "UP" }));

app.Run();
