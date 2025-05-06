using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;



namespace DigitalSensor.Models;

public class CalibrationAdjust
{
    public float Factor { get; set; } = 1;          // slope
    public float Offset { get; set; } = 0;           
}
