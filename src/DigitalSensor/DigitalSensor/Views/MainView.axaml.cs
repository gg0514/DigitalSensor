using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using DigitalSensor.Extensions;
using DigitalSensor.Services;
using DigitalSensor.ViewModels;

namespace DigitalSensor.Views;

public partial class MainView : UserControl
{
    public MainView()
    {
        InitializeComponent();

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
}
