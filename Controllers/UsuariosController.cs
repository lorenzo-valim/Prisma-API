using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using BCrypt.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ProjetoPrisma.Data;
using ProjetoPrisma.Models;
namespace ProjetoPrisma.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsuariosController : ControllerBase
    {
        private readonly AppDbContext _appDbContext;

        public UsuariosController(AppDbContext appDbContext)
        {
            _appDbContext = appDbContext;
        }
        // Código para criar, ler, atualizar e deletar usuários. O final do código para cada operação é indicado por um comentário.

        // POST: api/Usuarios
        
        // final do POST

        // GET: api/Usuarios
        [HttpGet]
        public async Task<IActionResult> GetUsuarios()
        {
            var usuarios = await _appDbContext.Usuarios.ToListAsync();
            return Ok(usuarios);
        }
        // final do GET todos usuarios

        // GET: api/Usuarios/{id}
        [HttpGet("{id}")]
        public async Task<IActionResult> GetUsuario(Guid id)
        {
            var usuario = await _appDbContext.Usuarios.FindAsync(id);
            if (usuario == null)
            {
                return NotFound();
            }
            return Ok(usuario);
        }
        // final do GET por ID usuario

        // PUT: api/Usuarios/{id}
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateUsuario(Guid id, Usuario usuario)
        {
            if (id != usuario.Id)
            {
            return BadRequest("O ID do usuário não pode ser modificado.");
            }

            usuario.Id = id;

            _appDbContext.Entry(usuario).State = EntityState.Modified;

            try
            {
            await _appDbContext.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
            if (!UsuarioExists(id))
            {
                return NotFound();
            }
            else
            {
                throw;
            }
            }

            return NoContent();
        }

        private bool UsuarioExists(Guid id)
        {
            return _appDbContext.Usuarios.Any(e => e.Id == id);
        }
        // final do PUT

        // DELETE: api/Usuarios/{id}
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteUsuario(Guid id)
        {
            var usuario = await _appDbContext.Usuarios.FindAsync(id);
            if (usuario == null)
            {
                return NotFound();
            }

            _appDbContext.Usuarios.Remove(usuario);
            await _appDbContext.SaveChangesAsync();

            return Ok(usuario);
        }
        // final do DELETE


        // POST: api/Usuarios/login





    }
}