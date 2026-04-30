using System.Threading.Tasks;

namespace ProjetoPrisma.Services
{
    public interface IEmailService
    {
        Task<bool> SendEmailAsync(string toEmail, string subject, string body, bool isHtml = true);
    }
}
