using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingCalibViewModel : ViewModelBase
{
    private readonly IModbusService _modbusService;

    [ObservableProperty]
    public ModbusInfo _modbusInfo;

    [ObservableProperty]
    public CalibrationAdjust _calibAdjust;


    public SettingCalibViewModel()
    {
        _modbusInfo = new ModbusInfo();
        _calibAdjust = new CalibrationAdjust();
    }

    public SettingCalibViewModel(IModbusService modbusService, AppSettings settings)
    {
        _modbusService = modbusService;
        _modbusInfo = settings.ModbusInfo;
        _calibAdjust = settings.CalibAdjust;
    }


    [RelayCommand]
    private async void Apply()
    {
        // Save settings logic would go here
    }
}