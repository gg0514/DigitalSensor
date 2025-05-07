using Avalonia.Controls;
using Avalonia.Input;
using DigitalSensor.Extensions;
using DigitalSensor.Services;
using FluentAvalonia.UI.Controls;

namespace DigitalSensor.Views;

public partial class SettingView : UserControl
{
    public SettingView()
    {
        InitializeComponent();

        // BreadcrumbBar를 사용하면, 메뉴이동 기능도 같이 만들어야 한다.
        //BreadcrumbBar1.ItemsSource = new string[] { "Setting", "Comm", };

    }

    private void OnBackgroundPointerPressed(object? sender, PointerPressedEventArgs e)
    {
        MainView mainView = App.GlobalHost.GetService<MainView>();
        mainView.OnNavigateTo("Setting_Modbus");

    }
}
