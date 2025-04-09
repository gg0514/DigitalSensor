using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using NavigationView.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;


public partial class HomeViewModel : ViewModelBase
{
    private readonly IDataService _dataService;

    [ObservableProperty]
    private ObservableCollection<string> _items;

    [ObservableProperty]
    private int _selectedTabIndex;

    [ObservableProperty]
    private ObservableCollection<TabItemViewModel> _tabItems;


    public HomeViewModel()
    {

    }


    public HomeViewModel(IDataService dataService,
                         Tab1ViewModel tab1ViewModel,
                         Tab2ViewModel tab2ViewModel,
                         Tab3ViewModel tab3ViewModel)
    {
        _dataService = dataService;
        Items = new ObservableCollection<string>();

        TabItems = new ObservableCollection<TabItemViewModel>
        {
            new TabItemViewModel { Header = "Tab 1", Content = tab1ViewModel },
            new TabItemViewModel { Header = "Tab 2", Content = tab2ViewModel },
            new TabItemViewModel { Header = "Tab 3", Content = tab3ViewModel }
        };

        LoadDataAsync();
    }

    private async Task LoadDataAsync()
    {
        var data = await _dataService.GetItemsAsync();
        Items.Clear();
        foreach (var item in data)
        {
            Items.Add(item);
        }
    }
}