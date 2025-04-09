using Microsoft.Extensions.DependencyInjection;
using NavigationView.ViewModels;
using System;

namespace NavigationView.Services;

public class NavigationService : INavigationService
{
    private readonly IServiceProvider _serviceProvider;
    private object? _currentView;

    public object? CurrentViewModel => _currentView;

    public event EventHandler<object>? NavigationChanged;

    public NavigationService(IServiceProvider serviceProvider)
    {
        _serviceProvider = serviceProvider;
    }

    public void Navigate<TViewModel>() where TViewModel : ViewModelBase
    {
        var viewModel = _serviceProvider.GetRequiredService<TViewModel>();
        _currentView = viewModel;
        NavigationChanged?.Invoke(this, viewModel);
    }
}