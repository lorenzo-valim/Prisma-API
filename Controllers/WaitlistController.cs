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
    public class WaitlistController : ControllerBase
    {
         private readonly AppDbContext _appDbContext;

        public WaitlistController(AppDbContext appDbContext)
        {
            _appDbContext = appDbContext;
        }

        private async Task<string> ValidarIdsWaitlist(Guid salaId, Guid usuarioId)
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

        [HttpDelete("del/{id}")]
        public async Task<IActionResult> DeleteWaitlist(Guid id)
        {
            var waitlist = await _appDbContext.Waitlist.FindAsync(id);
            if (waitlist == null)
            {
                return NotFound();
            }

            _appDbContext.Waitlist.Remove(waitlist);
            await _appDbContext.SaveChangesAsync();

            return Ok(waitlist);
        }

    }
}