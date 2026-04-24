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

            // Verificar se há conflito de horário
            var conflito = await _appDbContext.Reservas.AnyAsync(r =>
                r.SalaId == reserva.SalaId &&
                r.DataReserva == reserva.DataReserva &&
                r.Status == StatusReserva.Ativa &&
                ((reserva.HorarioInicio < r.HorarioFim && reserva.HorarioFim > r.HorarioInicio) ||
                 (r.HorarioInicio < reserva.HorarioFim && r.HorarioFim > reserva.HorarioInicio)));

            if (conflito)
            {
                // Adicionar à lista de espera
                var waitlist = new Waitlist
                {
                    UsuarioId = reserva.UsuarioId,
                    SalaId = reserva.SalaId,
                    DataReserva = reserva.DataReserva,
                    HorarioInicio = reserva.HorarioInicio,
                    HorarioFim = reserva.HorarioFim,
                    DataSolicitacao = DateTime.UtcNow
                };
                _appDbContext.Waitlists.Add(waitlist);
                await _appDbContext.SaveChangesAsync();
                return Ok(new { Message = "Horário ocupado. Adicionado à lista de espera.", WaitlistId = waitlist.Id });
            }

            reserva.Status = StatusReserva.Ativa;
            _appDbContext.Reservas.Add(reserva);
            await _appDbContext.SaveChangesAsync();
            return Ok(reserva);
        }
        // final do POST

        // GET: api/Reserva
        [HttpGet]
        public async Task<IActionResult> GetReservas([FromQuery] bool incluirCanceladas = false)
        {
            var reservas = await _appDbContext.Reservas
                .Where(r => incluirCanceladas || r.Status == StatusReserva.Ativa)
                .ToListAsync();
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

            reserva.Status = StatusReserva.Cancelada;
            _appDbContext.Entry(reserva).State = EntityState.Modified;
            await _appDbContext.SaveChangesAsync();

            // Promover da lista de espera
            var waitlistEntry = await _appDbContext.Waitlists
                .Where(w => w.SalaId == reserva.SalaId &&
                            w.DataReserva == reserva.DataReserva &&
                            w.HorarioInicio == reserva.HorarioInicio &&
                            w.HorarioFim == reserva.HorarioFim)
                .OrderBy(w => w.DataSolicitacao)
                .FirstOrDefaultAsync();

            if (waitlistEntry != null)
            {
                // Criar nova reserva para o primeiro da lista
                var novaReserva = new Reserva
                {
                    UsuarioId = waitlistEntry.UsuarioId,
                    SalaId = waitlistEntry.SalaId,
                    DataReserva = waitlistEntry.DataReserva,
                    HorarioInicio = waitlistEntry.HorarioInicio,
                    HorarioFim = waitlistEntry.HorarioFim,
                    Status = StatusReserva.Ativa
                };
                _appDbContext.Reservas.Add(novaReserva);
                _appDbContext.Waitlists.Remove(waitlistEntry);
                await _appDbContext.SaveChangesAsync();
            }

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

        // GET: api/Reserva/waitlist
        [HttpGet("waitlist")]
        public async Task<IActionResult> GetWaitlist()
        {
            var waitlist = await _appDbContext.Waitlists
                .OrderBy(w => w.DataSolicitacao)
                .ToListAsync();
            return Ok(waitlist);
        }
        // final do GET waitlist
    }
}