using CommunityToolkit.Mvvm.ComponentModel;
using System.Threading.Tasks;
using System;

namespace DigitalSensor.ViewModels;

public class ViewModelBase : ObservableObject
{
    protected bool IsUiThread => UiDispatcherHelper.IsOnUiThread;

    protected void RunOnUi(Action action)
    {
        UiDispatcherHelper.RunOnUiThread(action);
    }

    protected T RunOnUi<T>(Func<T> func)
    {
        return UiDispatcherHelper.RunOnUiThread(func);
    }

    protected Task RunOnUiAsync(Func<Task> action)
    {
        return UiDispatcherHelper.RunOnUiThreadAsync(action);
    }

    protected Task<T> RunOnUiAsync<T>(Func<Task<T>> action)
    {
        return UiDispatcherHelper.RunOnUiThreadAsync(action);
    }

}
