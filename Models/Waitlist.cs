using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public class Waitlist
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public int SalaId { get; set; }
        public DateTime DataReserva { get; set; }
        public TimeSpan HorarioInicio { get; set; }
        public TimeSpan HorarioFim { get; set; }
        public DateTime DataSolicitacao { get; set; } // Para ordenar por quem pediu primeiro
    }
}