using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
using ProjetoPrisma.Services;

namespace ProjetoPrisma.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _appDbContext;
        private readonly IEmailService _emailService;
        private readonly IOptions<EmailVerificationSettings> _emailVerificationSettings;

        public AuthController(
            AppDbContext appDbContext, 
            IEmailService emailService,
            IOptions<EmailVerificationSettings> emailVerificationSettings)
        {
            _appDbContext = appDbContext;
            _emailService = emailService;
            _emailVerificationSettings = emailVerificationSettings;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto loginDto)
        {
            // 1. Prevenção básica de nulos
            if (string.IsNullOrEmpty(loginDto.Email) || string.IsNullOrEmpty(loginDto.Password))
            {
                return BadRequest(new { message = "E-mail e senha são obrigatórios." });
            }

            // 2. Busca o usuário
            var usuario = await _appDbContext.Usuarios
                .FirstOrDefaultAsync(u => u.Email == loginDto.Email);

            if (usuario == null || string.IsNullOrEmpty(usuario.PasswordHash))
            {
                return Unauthorized(new { message = "E-mail ou senha incorretos." });
            }

            // 3. Validação segura com Try-Catch
            try
            {
                bool senhaValida = BCrypt.Net.BCrypt.Verify(loginDto.Password, usuario.PasswordHash);

                if (!senhaValida)
                {
                    return Unauthorized(new { message = "E-mail ou senha incorretos." });
                }
            }
            catch (Exception)
            {
                // Se cair aqui, a senha no banco não é um Hash BCrypt válido (é uma senha antiga)
                return StatusCode(500, new { message = "O formato da senha salva no banco é incompatível. Crie um novo usuário para testar." });
            }

            return Ok(new
            {
                message = $"Bem-vindo, {usuario.Nome}!",
                id = usuario.Id,
                email = usuario.Email
            });
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register(UsuarioCreateDto usuarioDto)
        {
            // 1. Verifies if the email already exists
            var existingUser = await _appDbContext.Usuarios
                .FirstOrDefaultAsync(u => u.Email == usuarioDto.Email);
            if (existingUser != null)
            {
                return BadRequest("O e-mail já está em uso.");
            }
            
            // Generate secure OTP code
            string otpCode = GenerateSecureOtp();
            int otpExpirationMinutes = _emailVerificationSettings.Value.OtpExpirationMinutes;
            
            // 2. Creates the user with the generated token
            var newUser = new Usuario
            {
                Id = Guid.NewGuid(),
                Nome = usuarioDto.Nome,
                Email = usuarioDto.Email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(usuarioDto.Password),
                Tipo = usuarioDto.Tipo,
                Criacao = DateTime.UtcNow,
                VerificationToken = otpCode,
                TokenExpiration = DateTime.UtcNow.AddMinutes(otpExpirationMinutes),
                IsEmailVerified = false
            };

            // 3. Saves to the database
            _appDbContext.Usuarios.Add(newUser);
            await _appDbContext.SaveChangesAsync();

            // 4. Prepares and sends the email using the injected email service
            string subject = "Seu código de verificação Prisma";
            string body = $"<h1>Bem-vindo!</h1><p>Seu código de verificação é: <strong>{otpCode}</strong></p>";

            bool emailSent = await _emailService.SendEmailAsync(newUser.Email, subject, body);

            if (!emailSent)
            {
                return StatusCode(500, "Usuário criado, mas houve um erro ao enviar o email de verificação.");
            }

            return Ok("Usuário registrado com sucesso! Por favor, verifique sua caixa de entrada.");
        }

        private string GenerateSecureOtp()
        {
            byte[] tokenData = new byte[4];
            RandomNumberGenerator.Fill(tokenData);
            int randomNumber = BitConverter.ToInt32(tokenData, 0) & int.MaxValue;
            return (randomNumber % 900000 + 100000).ToString();
        }

      [HttpPost("verify-otp")]
public async Task<IActionResult> VerifyOtp([FromBody] VerifyOtpDto dto)
{
    // Busca o usuário pelo email
    var user = await _appDbContext.Usuarios.FirstOrDefaultAsync(u => u.Email == dto.Email);

    if (user == null) return BadRequest("Usuário não encontrado.");
    
    if (user.VerificationToken != dto.Code) return BadRequest("Código inválido.");
    
    if (user.TokenExpiration < DateTime.UtcNow) return BadRequest("O código expirou.");

    // Sucesso!
    user.IsEmailVerified = true;
    user.VerificationToken = null;
    user.TokenExpiration = null;

    await _appDbContext.SaveChangesAsync();

    return Ok("Conta verificada com sucesso!");
}

    }
}