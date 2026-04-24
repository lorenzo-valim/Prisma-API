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
    public class SalasController : ControllerBase
    {
        private readonly AppDbContext _appDbContext;

        public SalasController(AppDbContext appDbContext)
        {
            _appDbContext = appDbContext;
        }
        // Código para criar, ler, atualizar e deletar salas. O final do código para cada operação é indicado por um comentário.

        // POST: api/Salas
        [HttpPost]
        public async Task<IActionResult> CreateSala(Sala sala)
        {
            _appDbContext.Salas.Add(sala);
            await _appDbContext.SaveChangesAsync();
            return Ok(sala);
        }
        // final do POST

        // GET: api/Salas
        [HttpGet]
        public async Task<IActionResult> GetSalas()
        {
            var salas = await _appDbContext.Salas.ToListAsync();
            return Ok(salas);
        }
        // final do GET todas salas

        // GET: api/Salas/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetSala(Guid id)
        {
            var sala = await _appDbContext.Salas.FindAsync(id);
            if (sala == null)
            {
                return NotFound();
            }
            return Ok(sala);
        }
        // final do GET por ID sala

        // PUT: api/Salas/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateSala(Guid id, Sala sala)
        {
            if (id != sala.Id)
            {
                return BadRequest("O ID da sala não pode ser modificado.");
            }
            _appDbContext.Entry(sala).State = Microsoft.EntityFrameworkCore.EntityState.Modified;
            await _appDbContext.SaveChangesAsync();
            return NoContent();
        }
        // final do PUT

        // DELETE: api/Salas/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteSala(Guid id)
        {
            var sala = await _appDbContext.Salas.FindAsync(id);
            if (sala == null)
            {
                return NotFound();
            }

            _appDbContext.Salas.Remove(sala);
            await _appDbContext.SaveChangesAsync();

            return Ok(sala);
        }
        // final do DELETE
    }
}