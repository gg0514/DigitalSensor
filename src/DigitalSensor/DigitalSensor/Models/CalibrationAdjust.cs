using Avalonia.Media;
using CommunityToolkit.Mvvm.ComponentModel;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;



namespace DigitalSensor.Models;

public partial class CalibrationAdjust : ObservableObject
{
    [ObservableProperty] private float factor = 1;          // slope
    [ObservableProperty] private float offset = 0;           
}
