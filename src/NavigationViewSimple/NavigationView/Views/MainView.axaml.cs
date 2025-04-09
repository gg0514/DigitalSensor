using Avalonia.Controls;
using FluentAvalonia.UI.Controls;
using NavigationView.ViewModels;

namespace NavigationView.Views;

public partial class MainView : UserControl
{
    public MainView()
    {
        InitializeComponent();
        DataContext = new MainViewModel();
    }

    private void OnNavigationSelectionChanged(object? sender, NavigationViewSelectionChangedEventArgs e)
    {
        if (DataContext is MainViewModel vm)
        {
            switch (e.SelectedItem)
            {
                case NavigationViewItem item when item.Tag?.ToString() == "home":
                    vm.NavigateHomeCommand.Execute(null);
                    break;

                case NavigationViewItem item when item.Tag?.ToString() == "settings":
                    vm.NavigateSettingsCommand.Execute(null);
                    break;
            }
        }
    }

}
