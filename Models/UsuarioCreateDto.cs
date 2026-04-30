using System;

namespace ProjetoPrisma.Models
{

    public class UsuarioCreateDto
    {
        public string Nome { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public TipoUsuario Tipo { get; set; }
     
    }
}
