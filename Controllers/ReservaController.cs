using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
using Microsoft.EntityFrameworkCore;
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
    }
}