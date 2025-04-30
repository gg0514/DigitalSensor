using Avalonia.Controls;
using DigitalSensor.ViewModels;

namespace DigitalSensor.Views;

public partial class Calib_2PBufferView : UserControl
{
    public Calib_2PBufferView()
    {
        InitializeComponent();

        // BreadcrumbBar를 사용하면, 메뉴이동 기능도 같이 만들어야 한다.
        //BreadcrumbBar1.ItemsSource = new string[] { "Setting", "Comm", };
        this.AttachedToVisualTree += (_, _) =>
        {
            if (DataContext is Calib_2PBufferViewModel vm)
            {
                vm.OnViewLoaded(); // ViewModel에서 정의한 메서드 호출
            }
        };
    }
}
