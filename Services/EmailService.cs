using System;
using System.Net.Mail;
using System.Threading.Tasks;
using Microsoft.Extensions.Options;
using ProjetoPrisma.Models;

namespace ProjetoPrisma.Services
{
    public class EmailService : IEmailService
    {
        private readonly SmtpSettings _smtpSettings;

        public EmailService(IOptions<SmtpSettings> smtpSettings)
        {
            _smtpSettings = smtpSettings.Value;
        }

        public async Task<bool> SendEmailAsync(string toEmail, string subject, string body, bool isHtml = true)
        {
            try
            {
                using (SmtpClient smtp = new SmtpClient(_smtpSettings.Host, _smtpSettings.Port))
                {
                    smtp.Credentials = new System.Net.NetworkCredential(_smtpSettings.User, _smtpSettings.Password);
                    smtp.EnableSsl = _smtpSettings.EnableSsl;

                    MailMessage mail = new MailMessage
                    {
                        From = new MailAddress(_smtpSettings.User),
                        Subject = subject,
                        Body = body,
                        IsBodyHtml = isHtml,
                    };
                    mail.To.Add(toEmail);

                    await smtp.SendMailAsync(mail);
                    return true;
                }
            }
            catch (Exception)
            {
                // Log the exception here if needed
                return false;
            }
        }
    }
}
