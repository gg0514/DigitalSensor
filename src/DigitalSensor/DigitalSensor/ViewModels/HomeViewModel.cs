using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;


public partial class HomeViewModel : ViewModelBase
{

    private readonly DataService _dataService;

    [ObservableProperty]
    private string receivedData;

    public HomeViewModel()
    {
        _dataService = new DataService();
        _dataService.DataReceived += data => ReceivedData = data;
    }


    public HomeViewModel(DataService dataService)
    {
        _dataService = dataService;
        _dataService.DataReceived += data => ReceivedData = data;
    }

}