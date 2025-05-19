using Avalonia.Controls;
using Avalonia.Input;
using DigitalSensor.Extensions;
using System.Diagnostics;
using System.Security.Principal;
using System;
using DigitalSensor.Resources;

namespace DigitalSensor.Views;

public partial class HomeView : UserControl
{
    private int _tapCount = 0;
    private DateTime _lastTapTime = DateTime.MinValue;
    private const int TripleTapThresholdMs = 600; // 세 번 터치 간 최대 허용 시간

    public HomeView()
    {
        InitializeComponent();


        string appVersion = " Ver.1.0";
        string Greeting = LocalizationManager.GetString("Greeting");

        Debug.WriteLine(Greeting+ appVersion); // "안녕하세요" 출력

    }

    private void OnBackgroundPointerPressed(object? sender, PointerPressedEventArgs e)
    {
        var now = DateTime.Now;
        var diff = (now - _lastTapTime).TotalMilliseconds;

        if (diff < TripleTapThresholdMs)
        {
            _tapCount++;
        }
        else
        {
            _tapCount = 1; // 시간 초과 → 첫 번째 터치로 간주
        }

        _lastTapTime = now;

        if (_tapCount == 3)
        {
            _tapCount = 0;

            Debug.WriteLine("Triple tap detected!");

            // 원하는 로직 실행
            MainView mainView = App.GlobalHost.GetService<MainView>();
            mainView.OnNavigateTo("pH");
        }
    }



}
