using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingModbusViewModel : ViewModelBase
{
    [ObservableProperty]
    public ModbusInfo _modbusInfo;



    public SettingModbusViewModel(AppSettings settings)
    {
        ModbusInfo = settings.ModbusInfo;
    }


    [RelayCommand]
    private async void Load()
    {
        // Save settings logic would go here
    }

    [RelayCommand]
    private async void Apply()
    {
        // Save settings logic would go here
    }
}