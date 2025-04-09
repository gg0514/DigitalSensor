using NavigationView.ViewModels;
using System;
using System.Collections.Generic;

namespace NavigationView.Services;

public interface INavigationService
{
    void Navigate<TViewModel>() where TViewModel : ViewModelBase;
    object? CurrentViewModel { get; }
    event EventHandler<object>? NavigationChanged;
}