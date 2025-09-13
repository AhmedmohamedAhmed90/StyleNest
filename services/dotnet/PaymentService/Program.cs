using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using PaymentService.Rabbit;

var builder = WebApplication.CreateBuilder(args);

// RabbitMQ publisher
builder.Services.AddSingleton<PaymentEventPublisher>();
builder.Services.AddHttpClient("eureka");
builder.Services.AddHostedService<PaymentService.Services.EurekaRegistration>();

// Controllers + Swagger
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// JWT Authentication (use literal secret bytes for compatibility)
var jwtSecret = builder.Configuration.GetValue<string>("Jwt:Secret") ?? throw new InvalidOperationException("Jwt:Secret required");
var secretBytes = Encoding.UTF8.GetBytes(jwtSecret);
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

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Simple health endpoint for Eureka
app.MapGet("/health", () => Results.Ok(new { status = "UP" }));

app.Run();
