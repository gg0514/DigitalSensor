﻿using Avalonia.Controls;
using FluentAvalonia.UI.Controls;
using NavigationView.ViewModels;

namespace NavigationView.Views;

public partial class MainView : UserControl
{
    public MainView()
    {
        InitializeComponent();
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
}
