using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;

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
        private async Task<string> ValidarIdsReservaAsync(Guid salaId, Guid usuarioId)
        {
            var salaExists = await _appDbContext.Salas.AnyAsync(s => s.Id == salaId);
            var usuarioExists = await _appDbContext.Usuarios.AnyAsync(u => u.Id == usuarioId);

            if (!salaExists || !usuarioExists)
            {
                return "SalaId ou UsuarioId inválido.";
            }



            // Retorna null (nada) se os dois IDs existirem, significando que a validação passou.
            return null;
        }




        // POST: api/Reserva
        [HttpPost]
        public async Task<IActionResult> CreateReserva(Reserva reserva)
        {
            // Chama a função reutilizável
            var erroValidacao = await ValidarIdsReservaAsync(reserva.Id_Sala, reserva.Id_Usuario);

            // O "if" agora só verifica se a função retornou algum erro
            if (erroValidacao != null)
            {
                return BadRequest(erroValidacao);
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
        public async Task<IActionResult> GetReserva(Guid id)
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
        public async Task<IActionResult> UpdateReserva(Guid id, Reserva reserva)
        {
            var erroValidacao = await ValidarIdsReservaAsync(reserva.Id_Sala, reserva.Id_Usuario);

            // O "if" agora só verifica se a função retornou algum erro
            if (erroValidacao != null)
            {
                return BadRequest(erroValidacao);
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
        public async Task<IActionResult> DeleteReserva(Guid id)
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