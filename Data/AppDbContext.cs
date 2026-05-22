using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using ProjetoPrisma.Models;
namespace ProjetoPrisma.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }
        public DbSet<Usuario> Usuarios { get; set; }
        public DbSet<Sala> Salas { get; set; }
        public DbSet<Reserva> Reservas { get; set; }
        public DbSet<Waitlist> Waitlists { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder); // Boa prática manter a chamada base

            // Aqui você coloca as configurações que discutimos:
            modelBuilder.Entity<Usuario>(entity =>
            {
                // Configuração do ID binary(16)
                entity.Property(e => e.Id)
                    .HasColumnType("binary(16)")
                    .HasValueGenerator<Microsoft.EntityFrameworkCore.ValueGeneration.SequentialGuidValueGenerator>();
                entity.Property(u => u.Tipo)
                .HasConversion<String>();
                
                entity.Property(e => e.VerificationTokenResetPassword)
        .HasMaxLength(255)
        .IsRequired(false); // Permite NULL

                entity.Property(e => e.TokenExpirationResetPassword)
                    .HasColumnType("datetime")
                    .IsRequired(false);

            });

            modelBuilder.Entity<Sala>(entity =>
            {
                // Configuração do ID binary(16)
                entity.Property(e => e.Id)
                    .HasColumnType("binary(16)")
                    .HasValueGenerator<Microsoft.EntityFrameworkCore.ValueGeneration.SequentialGuidValueGenerator>();
                entity.Property(e => e.Disponibilidade)
            .HasConversion<string>();

            });

            modelBuilder.Entity<Reserva>(entity =>
            {
                // Configuração do ID binary(16)
                entity.Property(e => e.Id)
                    .HasColumnType("binary(16)")
                    .HasValueGenerator<Microsoft.EntityFrameworkCore.ValueGeneration.SequentialGuidValueGenerator>();
                entity.Property(e => e.UsuarioId).HasColumnType("binary(16)");
                entity.Property(e => e.SalaId).HasColumnType("binary(16)");


            });
        }

    }
}