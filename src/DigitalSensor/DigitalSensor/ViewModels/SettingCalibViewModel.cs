using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingCalibViewModel : ViewModelBase
{

    [ObservableProperty]
    public CalibrationAdjust _calibAdjust = new();


    public SettingCalibViewModel()
    {
    }


    [RelayCommand]
    private async void Apply()
    {
        // Save settings logic would go here
    }
}