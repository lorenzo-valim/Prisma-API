using Microsoft.AspNetCore.Mvc;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
using Microsoft.EntityFrameworkCore;
using GuerrillaNtp;
namespace ProjetoPrisma.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReservaController : ControllerBase
    {
        private readonly AppDbContext _appDbContext;

        public ReservaController(AppDbContext appDbContext)
        {
            _appDbContext = appDbContext;
        }
        // Código para criar, ler, atualizar e deletar reservas. O final do código para cada operação é indicado por um comentário.

        // POST: api/Reserva
        [HttpPost]
        public async Task<IActionResult> CreateReserva(Reserva reserva)
        {
            var salaExists = await _appDbContext.Salas.AnyAsync(s => s.Id == reserva.SalaId);
            var usuarioExists = await _appDbContext.Usuarios.AnyAsync(u => u.Id == reserva.UsuarioId);

            if (!salaExists || !usuarioExists)
            {
                return BadRequest("SalaId ou UsuarioId inválido.");
            }
 
           //Início da verificação da data da reserva utilizando o horário oficial do NTP
            try
            {
                var ntp = new NtpClient("a.st1.ntp.br");
                var ntpTime = await ntp.QueryAsync();
                DateTime dataConfiavel = ntpTime.UtcNow.DateTime; 

                if (reserva.DataReserva < dataConfiavel)
                {
                    return BadRequest("A data da reserva não pode ser no passado.");
                }
            }
            catch (Exception)
            {
                return StatusCode(503, "Não foi possível validar o horário oficial.");
            }
            //Fim da verificação da data da reserva utilizando o horário oficial do NTP
           
            _appDbContext.Reservas.Add(reserva);
            await _appDbContext.SaveChangesAsync();
            return Ok(reserva);
        }
        // final do POST

        // GET: api/Reserva
        [HttpGet]
        public async Task<IActionResult> GetReservas()
        {
            var reservas = await _appDbContext.Reservas.ToListAsync();
            return Ok(reservas);
        }
        // final do GET todas reservas

        // GET: api/Reserva/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetReserva(int id)
        {
            var reserva = await _appDbContext.Reservas.FindAsync(id);
            if (reserva == null)
            {
                return NotFound();
            }
            return Ok(reserva);
        }
        // final do GET por ID reserva

        // PUT: api/Reserva/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateReserva(int id, Reserva reserva)
        {
            var salaExists = await _appDbContext.Salas.AnyAsync(s => s.Id == reserva.SalaId);
            var usuarioExists = await _appDbContext.Usuarios.AnyAsync(u => u.Id == reserva.UsuarioId);
            if (!salaExists || !usuarioExists)
            {
                return BadRequest("SalaId ou UsuarioId inválido.");
            }
            if (id != reserva.Id)
            {
                return BadRequest("O ID da reserva não pode ser modificado.");
            }
            //Início da verificação da data da reserva utilizando o horário oficial do NTP
            try
            {
                var ntp = new NtpClient("a.st1.ntp.br");
                var ntpTime = await ntp.QueryAsync();
                DateTime dataConfiavel = ntpTime.UtcNow.DateTime; 

                if (reserva.DataReserva < dataConfiavel)
                {
                    return BadRequest("A data da reserva não pode ser no passado.");
                }
            }
            catch (Exception)
            {
                return StatusCode(503, "Não foi possível validar o horário oficial.");
            }
            //Fim da verificação da data da reserva utilizando o horário oficial do NTP

            _appDbContext.Entry(reserva).State = Microsoft.EntityFrameworkCore.EntityState.Modified;
            await _appDbContext.SaveChangesAsync();
            return NoContent();
        }
        // final do PUT

        // DELETE: api/Reserva/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteReserva(int id)
        {
            var reserva = await _appDbContext.Reservas.FindAsync(id);
            if (reserva == null)
            {
                return NotFound();
            }

            _appDbContext.Reservas.Remove(reserva);
            await _appDbContext.SaveChangesAsync();

            return Ok(reserva);
        }
        // final do DELETE

        /* GET: api/Reserva/relatorio?dataInicio=2023-01-01&dataFim=2023-12-31
        http://localhost:5263/api/Reserva/relatorio?dataInicio=2024-01-01&dataFim=2024-12-31
        */
        [HttpGet("relatorio")]
        public async Task<IActionResult> GetRelatorioReservas([FromQuery] DateTime dataInicio, [FromQuery] DateTime dataFim)
        {
            var reservas = await _appDbContext.Reservas
                .Include(r => r.UsuarioId)
                .Include(r => r.SalaId)
                .Where(r => r.DataReserva >= dataInicio && r.DataReserva <= dataFim)
                .ToListAsync();

            return Ok(reservas);
        }
        // final do GET relatório
    }
}