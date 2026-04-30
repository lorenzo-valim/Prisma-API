using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public enum TipoUsuario
    {
        Admin = 2,
        User = 1
    }

    public class Usuario
    {
        public Guid Id { get; set; }
        public string Nome { get; set; }
        public string Email { get; set; }
        [JsonIgnore]
        public string PasswordHash { get; set; }
        public TipoUsuario Tipo { get; set; }
        public DateTime Criacao { get; set; }

        public bool IsEmailVerified { get; set; } = false;
        public string VerificationToken { get; set; }
        public DateTime? TokenExpiration { get; set; }
    }
}