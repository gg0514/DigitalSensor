using NavigationView.ViewModels;
using System;
using System.Collections.Generic;

namespace NavigationView.Services;

public interface INavigationService
{
    void NavigateTo(object viewModel);
    object CurrentViewModel { get; }
}

public class NavigationService : INavigationService
{
    public object CurrentViewModel { get; private set; }

    public void NavigateTo(object viewModel)
    {
        CurrentViewModel = viewModel;
    }
}