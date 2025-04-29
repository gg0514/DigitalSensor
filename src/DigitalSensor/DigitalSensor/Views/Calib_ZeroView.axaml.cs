using Avalonia.Controls;
using DigitalSensor.ViewModels;

namespace DigitalSensor.Views;

public partial class Calib_ZeroView : UserControl
{
    public Calib_ZeroView()
    {
        InitializeComponent();

        // BreadcrumbBar를 사용하면, 메뉴이동 기능도 같이 만들어야 한다.
        //BreadcrumbBar1.ItemsSource = new string[] { "Setting", "Comm", };

        this.AttachedToVisualTree += (_, _) =>
        {
            if (DataContext is Calib_ZeroViewModel vm)
            {
                vm.OnViewLoaded(); // ViewModel에서 정의한 메서드 호출
            }
        };
    }
}
