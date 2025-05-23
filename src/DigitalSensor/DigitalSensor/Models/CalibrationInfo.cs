﻿using Avalonia.Media;
using CommunityToolkit.Mvvm.ComponentModel;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;


namespace DigitalSensor.Models;

public partial class CalibInfo : ObservableObject
{
    [ObservableProperty] private bool _isRun = false;
    [ObservableProperty] private CalibrationStatus _CalStatus = CalibrationStatus.NoSensorCalibration;
}
