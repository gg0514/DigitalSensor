using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Models
{

    public class SerialConn
    {
        public string BaudRate { get; set; } = "9600";        // 9600, 115200
        public string DataBits { get; set; } = "8";        // 8
        public string StopBits { get; set; } = "1";        // 1    
        public string Parity { get; set; } = "0";          // 0
    }
}


