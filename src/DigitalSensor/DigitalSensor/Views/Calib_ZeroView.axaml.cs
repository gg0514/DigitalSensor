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

        // 화면에 보일 때 
        this.AttachedToVisualTree += (_, _) =>
        {
            if (DataContext is Calib_ZeroViewModel vm)
            {
                vm.OnViewLoaded(); 
                vm.IsVisible = true;
            }
        };

        // 화면에서 사라질때
        this.DetachedFromVisualTree += (s, e) =>
        {
            if (DataContext is Calib_ZeroViewModel vm)
            {
                vm.OnViewUnloaded();
                vm.IsVisible = false;
            }
        };

    }
}
