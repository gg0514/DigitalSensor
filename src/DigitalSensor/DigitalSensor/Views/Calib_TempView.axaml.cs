using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Interactivity;
using DigitalSensor.ViewModels;

namespace DigitalSensor.Views;

public partial class Calib_TempView : UserControl
{
    public Calib_TempView()
    {
        InitializeComponent();

        // BreadcrumbBar를 사용하면, 메뉴이동 기능도 같이 만들어야 한다.
        // BreadcrumbBar1.ItemsSource = new string[] { "Setting", "Comm", };

        this.AttachedToVisualTree += (_, _) =>
        {
            if (DataContext is Calib_TempViewModel vm)
            {
                vm.OnViewLoaded(); // ViewModel에서 정의한 메서드 호출
            }
        };

        // 화면에서 사라질때
        this.DetachedFromVisualTree += (s, e) =>
        {
            if (DataContext is Calib_TempViewModel vm)
            {
                vm.OnViewUnloaded();
            }
        };

    }


    private void OnBackgroundPointerPressed(object? sender, PointerPressedEventArgs e)
    {
        ValueTextBlock.Focus();
    }

    private void OnTextBlockClick(object sender, PointerPressedEventArgs e)
    {
        if (DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StartEditing();
        }

        ValueTextBox.Focus();
        ValueTextBox.CaretIndex = ValueTextBox.Text?.Length ?? 0;
    }

    private void OnTextBoxKeyUp(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Enter && DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StopEditing();
        }
    }

    private void OnTextBoxLostFocus(object sender, RoutedEventArgs e)
    {
        if (DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StopEditing();
        }
    }



}
