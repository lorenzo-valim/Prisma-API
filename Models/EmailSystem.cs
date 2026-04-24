using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Mail;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public class EmailSystem
    {
        private readonly string _smtpServer;
        private readonly int _smtpPort;
        private readonly string _smtpUser;
        private readonly string _smtpPassword;
        private readonly bool _enableSsl;

        public EmailSystem(string host, int port, string user, string password, bool ssl = true)
        {
            _smtpServer = host;
            _smtpPort = port;
            _smtpUser = user;
            _smtpPassword = password;
            _enableSsl = ssl;
        }

        public async Task<bool> SendEmailAsync(string toEmail, string subject, string body, bool isHtml = true)
        {
            try
            {
                using (SmtpClient smtp = new SmtpClient(_smtpServer, _smtpPort))
                {
                    smtp.Credentials = new System.Net.NetworkCredential(_smtpUser, _smtpPassword);
                    smtp.EnableSsl = _enableSsl;

                    MailMessage mail = new MailMessage
                    {
                        From = new MailAddress(_smtpUser),
                        Subject = subject,
                        Body = body,
                        IsBodyHtml = true,
                    };
                    mail.To.Add(toEmail);

                    await smtp.SendMailAsync(mail);
                    return true;
                }
                
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error sending email: {ex.Message}");
                return false;
            }
        }
    }
}