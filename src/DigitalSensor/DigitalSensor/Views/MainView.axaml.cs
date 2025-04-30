using Avalonia.Controls;
using Avalonia.Controls.Primitives;
using Avalonia.VisualTree;
using FluentAvalonia.UI.Controls;
using FluentIcons.Avalonia.Fluent;
using DigitalSensor.Extensions;
using DigitalSensor.Services;
using DigitalSensor.ViewModels;
using Avalonia;
using System.Linq;
using DigitalSensor.Models;
using System;

namespace DigitalSensor.Views;

public partial class MainView : UserControl
{
    private readonly IMonitoringService _monitoringService;

    public MainView()
    {
        InitializeComponent();
        AddSubMenuToExistingMenu();

        this.AttachedToVisualTree += OnAttachedToVisualTree;

        if (Design.IsDesignMode)
        {
            return;
        }
        
        DataContext = App.GlobalHost.GetService<MainViewModel>();


        _monitoringService = App.GlobalHost.GetService<IMonitoringService>();
        _monitoringService.SensorTypeReceived += OnSensorTypeReceived;

    }

    private void AddSubMenuToExistingMenu()
    {
        string parentTag = "Calib"; // 부모 메뉴의 Tag 값

        foreach (var item in NavView.MenuItems)
        {
            if (item is NavigationViewItem navItem && navItem.Tag?.ToString() == parentTag)
            {
                var Item1 = new NavigationViewItem
                {
                    Content = "Zero Calibration",
                    Tag = "Calib_Zero"
                };
                var Item2 = new NavigationViewItem
                {
                    Content = "1Point Sample",
                    Tag = "Calib_1PSample"
                };
                //var Item3 = new NavigationViewItem
                //{
                //    Content = "1Point Buffer",
                //    Tag = "Calib_1PBuffer"
                //};
                //var Item4 = new NavigationViewItem
                //{
                //    Content = "2Point Sample",
                //    Tag = "Calib_2PSample"
                //};
                var Item5 = new NavigationViewItem
                {
                    Content = "2Point Buffer",
                    Tag = "Calib_2PBuffer"
                };


                //Item3.IsEnabled = false; // 비활성화
                //Item4.IsEnabled = false; // 비활성화
                //Item5.IsEnabled = false; // 비활성화

                navItem.MenuItems.Add(Item1);
                navItem.MenuItems.Add(Item2);

                // 실질적으로 필요없다고 함.
                //navItem.MenuItems.Add(Item3);
                //navItem.MenuItems.Add(Item4);
                navItem.MenuItems.Add(Item5);
                break; // 찾았으면 루프 종료
            }
        }
    }

    private async void OnSensorTypeReceived(int type)
    {
        SensorType sensorType = (SensorType)type;

        string disableMenuTag = GetDisableMenuTag(sensorType);

        DisableMenuItem(disableMenuTag);
    }

    private string GetDisableMenuTag(SensorType type)
    {
        string disableMenuTag = string.Empty;
        switch (type)
        {
            case SensorType.PH:
                disableMenuTag = "Calib_Zero";
                break;
            case SensorType.Conductivity:
            case SensorType.Chlorine:
            case SensorType.TurbidityLow:
            case SensorType.TurbidityHighColor:
            case SensorType.TurbidityHighIR:
                disableMenuTag = "Calib_2PBuffer";
                break;
            default:
                break;
        }
        return disableMenuTag;
    }


    private void DisableMenuItem(string tagname)
    {
        string parentTag = "Calib"; // 부모 메뉴의 Tag 값

        foreach (var item in NavView.MenuItems)
        {
            if (item is NavigationViewItem navItem && navItem.Tag?.ToString() == parentTag)
            {
                foreach (var subItem in navItem.MenuItems)
                {
                    if (subItem is NavigationViewItem subNavItem && subNavItem.Tag?.ToString() == tagname)
                    {
                        subNavItem.IsEnabled = false;
                        break; // 찾았으면 루프 종료
                    }
                }
            }
        }
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
