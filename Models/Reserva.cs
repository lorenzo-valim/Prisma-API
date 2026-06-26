using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public enum StatusReserva
    {
        Ativa,
        Cancelada
    }

    public class Reserva
    {
        public Guid Id { get; set; }
        public Guid UsuarioId { get; set; }
        public Guid SalaId { get; set; }
        public DateTime DataReserva { get; set; }
        public TimeSpan HorarioInicio { get; set; }
        public TimeSpan HorarioFim { get; set; }
        public int StatusReserva { get; set; }
        public string Motivo { get; set; } // Adicionando a propriedade Motivo
        public string NomeUsuario { get; set; } // Adicionando a propriedade NomeUsuario
    }
}