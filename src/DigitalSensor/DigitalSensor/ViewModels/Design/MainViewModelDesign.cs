using CommunityToolkit.Mvvm.Messaging;
using DigitalSensor.Services;

namespace DigitalSensor.ViewModels.Design;

public class MainViewModelDesign : MainViewModel
{
    public MainViewModelDesign() : base( 
        new NotificationService(), 
        new FakeUsbService())
    {
    }
}
