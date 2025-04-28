using Avalonia.Controls;

namespace DigitalSensor.Views;

public partial class Calib_1PSampleView : UserControl
{
    public Calib_1PSampleView()
    {
        InitializeComponent();

        // BreadcrumbBar를 사용하면, 메뉴이동 기능도 같이 만들어야 한다.
        //BreadcrumbBar1.ItemsSource = new string[] { "Setting", "Comm", };

    }
}
