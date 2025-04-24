using Avalonia.Threading;
using System;
using System.Threading.Tasks;

namespace DigitalSensor;

public static class UiDispatcherHelper
{
    /// <summary>
    /// 현재 스레드가 UI 스레드인지 여부
    /// </summary>
    public static bool IsOnUiThread => Dispatcher.UIThread.CheckAccess();

    /// <summary>
    /// UI 스레드에서 액션 실행 (자동 전환)
    /// </summary>
    public static void RunOnUiThread(Action action)
    {
        if (IsOnUiThread)
            action();
        else
            Dispatcher.UIThread.Post(action);
    }

    /// <summary>
    /// UI 스레드에서 비동기 액션 실행 (자동 전환)
    /// </summary>
    public static async Task RunOnUiThreadAsync(Func<Task> action)
    {
        if (IsOnUiThread)
            await action();
        else
            await Dispatcher.UIThread.InvokeAsync(action);
    }

    /// <summary>
    /// UI 스레드에서 반환값이 있는 비동기 실행
    /// </summary>
    public static async Task<T> RunOnUiThreadAsync<T>(Func<Task<T>> action)
    {
        if (IsOnUiThread)
            return await action();
        else
            return await Dispatcher.UIThread.InvokeAsync(action);
    }

    /// <summary>
    /// UI 스레드에서 반환값이 있는 동기 실행
    /// </summary>
    public static T RunOnUiThread<T>(Func<T> action)
    {
        if (IsOnUiThread)
            return action();
        else
            return Dispatcher.UIThread.InvokeAsync(action).Result;
    }
}
