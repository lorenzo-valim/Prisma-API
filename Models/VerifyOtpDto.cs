using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ProjetoPrisma.Models
{
    public class VerifyOtpDto
    {
        public string Email { get; set; }
        public string Code { get; set; }
    }
}