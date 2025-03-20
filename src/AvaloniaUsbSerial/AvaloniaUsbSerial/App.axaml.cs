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
        // 서비스 컨테이너 설정
        var services = new ServiceCollection();

        // ViewModels 등록
        services.AddTransient<MainViewModel>();

        // 실제 서비스 구현은 플랫폼별 프로젝트에서 제공

        // 서비스 프로바이더 생성
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