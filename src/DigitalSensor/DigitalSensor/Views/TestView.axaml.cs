using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using DigitalSensor.Extensions;
using DigitalSensor.Services;
using DigitalSensor.ViewModels;

namespace DigitalSensor.Views;

public partial class TestView : UserControl
{
    public TestView()
    {
        InitializeComponent();

        if (Design.IsDesignMode)
        {
            return;
        }
        
        DataContext = App.GlobalHost.GetService<TestViewModel>();
    }

    protected override void OnApplyTemplate(TemplateAppliedEventArgs e)
    {
        base.OnApplyTemplate(e);
        var topLevel = TopLevel.GetTopLevel(this);
        var notificationService = App.GlobalHost.GetService<NotificationService>();
        notificationService?.SetTopLevel(topLevel);
    }
}
