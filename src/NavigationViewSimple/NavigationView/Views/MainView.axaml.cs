using Avalonia;
using Avalonia.Controls;
using Avalonia.VisualTree;
using FluentAvalonia.UI.Controls;
using NavigationView.ViewModels;
using System.Linq;

namespace NavigationView.Views;

public partial class MainView : UserControl
{
    public MainView()
    {
        InitializeComponent();
        this.AttachedToVisualTree += OnAttachedToVisualTree;

        //var navView = this.FindControl<FluentAvalonia.UI.Controls.NavigationView>("Nav");
        //if (navView != null)
        //{
        //    var paneToggleButton = navView.GetTemplatePart("PaneToggleButton") as Button;
        //    if (paneToggleButton != null)
        //    {
        //        // TextBlock 찾기
        //        var textBlock = paneToggleButton.FindControl<TextBlock>();
        //        if (textBlock != null)
        //        {
        //            textBlock.FontSize = 30; // 아이콘 크기 조정
        //        }
        //        else
        //        {
        //            // Viewbox 내부에서 TextBlock 찾기
        //            var viewbox = paneToggleButton.FindControl<Viewbox>();
        //            if (viewbox != null)
        //            {
        //                textBlock = viewbox.FindControl<TextBlock>();
        //                if (textBlock != null)
        //                {
        //                    textBlock.FontSize = 30;
        //                }
        //            }
        //        }
        //    }
        //}
    }

    private void NavView_SelectionChanged(object? sender, NavigationViewSelectionChangedEventArgs e)
    {
        if (e.SelectedItem is NavigationViewItem item && item.Tag is string tag)
        {
            if (DataContext is MainViewModel vm)
            {
                vm.NavigateToCommand.Execute(tag);
            }
        }
    }

    private void OnAttachedToVisualTree(object? sender, VisualTreeAttachmentEventArgs e)
    {
        var toggleButton = this.GetVisualDescendants()
            .OfType<Button>()
            .FirstOrDefault(b => b.Name == "PaneToggleButton");

        if (toggleButton != null)
        {
            var iconText = toggleButton.GetVisualDescendants()
                .OfType<TextBlock>()
                .FirstOrDefault();

            if (iconText != null)
                iconText.FontSize = 28;
        }
    }


}
