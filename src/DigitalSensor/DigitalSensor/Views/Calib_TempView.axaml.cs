using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Interactivity;
using Avalonia.Threading;
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
        if (DataContext is Calib_TempViewModel viewModel && viewModel.IsEditing)
        {
            viewModel.StopEditing();
        }
    }

    private void OnTextBlockClick(object sender, PointerPressedEventArgs e)
    {
        if (DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StartEditing();
        }

        ValueTextBox.Focus();
        ValueTextBox.CaretIndex = ValueTextBox.Text?.Length ?? 0;

        //// TextBox에 포커스 강제 지정 (로드 이후에)
        //Dispatcher.UIThread.Post(() =>
        //{
        //    ValueTextBox.Focus();
        //    ValueTextBox.CaretIndex = ValueTextBox.Text?.Length ?? 0;
        //}, DispatcherPriority.Background);

        SpacerBorder.IsVisible = true;

        // 이벤트 딜레이를 방지하기 위해 UI 스레드에서 스크롤을 업데이트  
        Dispatcher.UIThread.InvokeAsync(() => MainScrollViewer.ScrollToEnd());

        // 이벤트가 부모로 버블링되지 않게 함
        e.Handled = true;
        //Console.WriteLine("OnTextBlockClick called");
    }

    private void OnTextBoxKeyUp(object sender, KeyEventArgs e)
    {
       if (e.Key == Key.Enter && DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StopEditing();
            SpacerBorder.IsVisible = false;
        }
    }

    private void OnTextBoxLostFocus(object sender, RoutedEventArgs e)
    {
        if (DataContext is Calib_TempViewModel viewModel)
        {
            viewModel.StopEditing();
            SpacerBorder.IsVisible = false;
        }
    }
}
