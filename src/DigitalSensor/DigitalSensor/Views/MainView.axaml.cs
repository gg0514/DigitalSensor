using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using Avalonia.VisualTree;
using FluentAvalonia.UI.Controls;
using DigitalSensor.Extensions;
using DigitalSensor.Services;
using DigitalSensor.ViewModels;
using Avalonia;
using System.Linq;

namespace DigitalSensor.Views;

public partial class MainView : UserControl
{
    public MainView()
    {
        InitializeComponent();
        this.AttachedToVisualTree += OnAttachedToVisualTree;

        if (Design.IsDesignMode)
        {
            return;
        }
        
        DataContext = App.GlobalHost.GetService<MainViewModel>();
    }

    protected override void OnApplyTemplate(TemplateAppliedEventArgs e)
    {
        base.OnApplyTemplate(e);
        var topLevel = TopLevel.GetTopLevel(this);
        var notificationService = App.GlobalHost.GetService<NotificationService>();
        notificationService?.SetTopLevel(topLevel);
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
