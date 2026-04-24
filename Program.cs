// Arquivo: Program.cs — substitua o seu pelo este (adiciona CORS para o React)

using Microsoft.EntityFrameworkCore;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
using ProjetoPrisma.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

// Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure SMTP Settings from appsettings.json
builder.Services.Configure<SmtpSettings>(builder.Configuration.GetSection("SmtpSettings"));

// Configure Email Verification Settings from appsettings.json
builder.Services.Configure<EmailVerificationSettings>(builder.Configuration.GetSection("EmailVerification"));

// Register Email Service
builder.Services.AddScoped<IEmailService, EmailService>();


builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowReact", policy =>
    {
        policy
            .WithOrigins("http://localhost:5173") 
            .AllowAnyHeader()
            .AllowAnyMethod();
    });
});

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseForwardedHeaders();


app.UseCors("AllowReact");

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();