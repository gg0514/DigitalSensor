using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Markup.Xaml;
using AvaloniaUsbSerial.Services;
using AvaloniaUsbSerial.ViewModels;
using AvaloniaUsbSerial.Views;
using Microsoft.Extensions.DependencyInjection;
using System;

namespace AvaloniaUsbSerial;

public partial class App : Application
{
    public IServiceProvider Services { get; private set; }

    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
    }

    public override void OnFrameworkInitializationCompleted()
    {
        // ���� �����̳� ����
        var services = new ServiceCollection();

        // ViewModels ���
        services.AddTransient<MainViewModel>();

        // ���� ���� ������ �÷����� ������Ʈ���� ����

        // ���� ���ι��̴� ����
        Services = services.BuildServiceProvider();

        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            desktop.MainWindow = new MainWindow
            {
                DataContext = Services.GetRequiredService<MainViewModel>()
            };
        }

        base.OnFrameworkInitializationCompleted();
    }
}