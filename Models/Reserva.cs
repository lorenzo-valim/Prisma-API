using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public class Reserva
    {
        public Guid Id { get; set; }
        public Guid Id_Usuario { get; set; }
        public Guid Id_Sala { get; set; }
        public DateTime DataReserva { get; set; }
        public TimeSpan HorarioInicio { get; set; }
        public TimeSpan HorarioFim { get; set; }
    }
}